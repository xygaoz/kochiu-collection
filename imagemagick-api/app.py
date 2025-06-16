from flask import Flask, request, send_file
import subprocess
import tempfile
import shlex
import logging
import os
from datetime import datetime
from PIL import Image
from psd_tools import PSDImage
import rawpy
import imageio

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = 2 * 1024 * 1024 * 1024  # 2GB limit

# ==================== 日志配置 ====================
app.logger.handlers.clear()
formatter = logging.Formatter('%(asctime)s - %(levelname)s - %(message)s')
stream_handler = logging.StreamHandler()
stream_handler.setFormatter(formatter)
file_handler = logging.FileHandler('conversion.log')
file_handler.setFormatter(formatter)
app.logger.addHandler(stream_handler)
app.logger.addHandler(file_handler)
app.logger.setLevel(logging.INFO)
app.logger.info("Starting image conversion server with fallback support")

# ==================== 文件处理配置 ====================
LAYERED_FORMATS = {'psd', 'psb', 'tif', 'tiff'}
RAW_FORMATS = {
    'arw', 'cr2', 'cr3', 'nef', 'nrw', 'orf',
    'raf', 'rw2', 'pef', 'srf', 'sr2', 'kdc',
    'dng', 'erf', 'mef', 'mos', 'mrw', 'raw'
}

def get_file_extension(filename):
    """更健壮的文件扩展名获取函数"""
    # 处理文件对象和路径字符串两种情况
    if hasattr(filename, 'filename'):  # 如果是文件上传对象
        filename = filename.filename
    elif not isinstance(filename, str):  # 如果不是字符串
        return ''

    # 安全处理文件名
    filename = filename.strip()
    if '.' not in filename:
        return ''

    # 正确处理多后缀情况（如.tar.gz）
    parts = filename.lower().rsplit('.', 1)
    return parts[-1] if len(parts) > 1 else ''

def is_raw_file(filename):
    """增强的RAW文件检测"""
    ext = get_file_extension(filename)
    if not ext:
        return False

    # 完整的RAW格式集合
    RAW_EXTENSIONS = {
        '3fr', 'ari', 'arw', 'bay', 'crw', 'cr2', 'cr3',
        'cap', 'dcs', 'dcr', 'dng', 'drf', 'eip', 'erf',
        'fff', 'iiq', 'k25', 'kdc', 'mdc', 'mef', 'mos',
        'mrw', 'nef', 'nrw', 'obm', 'orf', 'pef', 'ptx',
        'pxn', 'r3d', 'raf', 'raw', 'rwl', 'rw2', 'rwz',
        'sr2', 'srf', 'srw', 'x3f'
    }
    return ext in RAW_EXTENSIONS

def needs_layer_merge(filename):
    return get_file_extension(filename) in LAYERED_FORMATS

# ==================== 转换方法 ====================
def convert_with_imagemagick(input_path, output_path, original_filename=None):
    """增强调试的ImageMagick转换函数"""
    try:
        ext = get_file_extension(original_filename or input_path)
        cmd = [
            "convert",
            "-verbose",  # 添加详细输出
            input_path,
            "-auto-orient",
            "-auto-gamma",
            "-auto-level",
            "-quality", "95",
            "-sharpen", "0x0.5",
            "-colorspace", "sRGB",
            output_path
        ]

        app.logger.info(f"Executing: {' '.join(shlex.quote(arg) for arg in cmd)}")

        # 捕获标准错误输出
        result = subprocess.run(
            cmd,
            check=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            timeout=60,
            text=True
        )

        # 记录完整输出
        app.logger.debug(f"ImageMagick stdout: {result.stdout}")
        app.logger.debug(f"ImageMagick stderr: {result.stderr}")

        return True
    except subprocess.CalledProcessError as e:
        app.logger.error(f"ImageMagick failed with code {e.returncode}:")
        app.logger.error(f"STDOUT: {e.stdout}")
        app.logger.error(f"STDERR: {e.stderr}")
        return False
    except Exception as e:
        app.logger.error(f"Unexpected error: {str(e)}")
        return False

def convert_with_pillow(input_path, output_path):
    """Pillow回退方案"""
    try:
        with Image.open(input_path) as img:
            img.convert('RGB').save(output_path)
        return True
    except Exception as e:
        app.logger.error(f"Pillow conversion failed: {str(e)}")
        return False

def convert_psd_with_python(input_path, output_path):
    """PSD专用回退方案"""
    try:
        import warnings
        from psd_tools import PSDImage

        # 临时忽略未知标签块警告
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")

            with tempfile.NamedTemporaryFile(suffix=".png") as tmp:
                psd = PSDImage.open(input_path)
                if not hasattr(psd, 'composite'):
                    raise ValueError("Invalid PSD file")

                # 尝试提取第一图层作为回退
                if len(psd) > 0:
                    psd[0].compose().save(tmp.name)
                else:
                    psd.composite().save(tmp.name)

                tmp.flush()
                os.fsync(tmp.fileno())
                with open(tmp.name, 'rb') as src, open(output_path, 'wb') as dst:
                    dst.write(src.read())
        return True
    except Exception as e:
        app.logger.error(f"PSD conversion failed: {str(e)}")
        return False

def convert_raw_file(input_path, output_path):
    """使用dcraw直接处理RAW文件，完全绕过ImageMagick的委托系统"""
    try:
        # 第一步：使用dcraw转换为PPM格式
        ppm_temp = tempfile.NamedTemporaryFile(suffix=".ppm")
        dcraw_cmd = [
            "dcraw",
            "-v",
            "-w",  # 使用相机白平衡
            "-6",  # 16位输出
            "-o", "0",  # 输出sRGB色彩空间
            "-c", input_path
        ]

        with open(ppm_temp.name, 'wb') as f:
            result = subprocess.run(
                dcraw_cmd,
                check=True,
                stdout=f,
                stderr=subprocess.PIPE,
                timeout=60
            )
            app.logger.debug(f"dcraw stdout: {result.stdout}")
            app.logger.debug(f"dcraw stderr: {result.stderr}")

        # 第二步：使用Pillow转换为目标格式
        with Image.open(ppm_temp.name) as img:
            # 如果是16位图像，转换为8位
            if img.mode == 'I;16':
                img = img.point(lambda i: i*(1./256)).convert('L')
            elif img.mode == 'RGB;16':
                img = img.point(lambda i: i*(1./256)).convert('RGB')
            img.save(output_path)

        ppm_temp.close()
        return True

    except subprocess.CalledProcessError as e:
        app.logger.error(f"dcraw conversion failed with code {e.returncode}:")
        app.logger.error(f"STDOUT: {e.stdout}")
        app.logger.error(f"STDERR: {e.stderr}")
        return False
    except Exception as e:
        app.logger.error(f"RAW conversion failed: {str(e)}")
        return False
    finally:
        if 'ppm_temp' in locals():
            ppm_temp.close()

# ==================== 路由逻辑 ====================
@app.route("/convert", methods=["POST"])
def convert():
    file = request.files.get('file')
    if not file:
        app.logger.warning("No file uploaded")
        return "No file uploaded", 400

    output_format = request.args.get("format", "png")
    app.logger.info(f"Processing {file.filename} -> {output_format}")

    original_ext = get_file_extension(file.filename)
    input_temp = tempfile.NamedTemporaryFile(suffix=f".{original_ext}" if original_ext else "")
    output_temp = tempfile.NamedTemporaryFile(suffix=f".{output_format}")

    try:
        file.save(input_temp.name)
        app.logger.info(f"Saved temp file: {input_temp.name}")

        start_time = datetime.now()
        success = False

        # 根据文件类型选择转换方法
        if original_ext in {'psd', 'psb'}:
            success = convert_psd_with_python(input_temp.name, output_temp.name)
        elif is_raw_file(file.filename):  # RAW文件处理流程
            # 先尝试dcraw+Pillow方法
            success = convert_raw_file(input_temp.name, output_temp.name)

            # 如果失败，尝试ImageMagick
            if not success:
                app.logger.info("dcraw+Pillow failed, trying ImageMagick")
                success = convert_with_imagemagick(
                    input_path=input_temp.name,
                    output_path=output_temp.name,
                    original_filename=file.filename
                )

            # 如果还是失败，尝试直接使用Pillow
            if not success:
                app.logger.info("ImageMagick failed, trying direct Pillow")
                success = convert_with_pillow(input_temp.name, output_temp.name)
        else:
            # 非RAW文件处理流程
            # 先尝试ImageMagick
            success = convert_with_imagemagick(
                input_path=input_temp.name,
                output_path=output_temp.name,
                original_filename=file.filename
            )
            if not success:
                app.logger.info("ImageMagick failed, trying Pillow fallback")
                success = convert_with_pillow(input_temp.name, output_temp.name)

        if not success:
            raise Exception("All conversion methods failed")

        duration = (datetime.now() - start_time).total_seconds()
        app.logger.info(f"Conversion completed in {duration:.2f}s")
        return send_file(
            output_temp.name,
            as_attachment=True,
            download_name=f"converted.{output_format}"
        )

    except Exception as e:
        app.logger.error(f"Conversion failed: {str(e)}")
        return "Conversion failed", 500
    finally:
        input_temp.close()
        output_temp.close()

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8190)