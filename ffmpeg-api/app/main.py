from fastapi import FastAPI, UploadFile, File, HTTPException, BackgroundTasks, Form
from fastapi.responses import FileResponse
from pathlib import Path
import uuid
from .utils import capture_frame, get_video_duration, calculate_default_timestamp
import logging
from logging.handlers import RotatingFileHandler
import sys

# 配置日志系统
def setup_logging():
    logger = logging.getLogger()
    logger.setLevel(logging.INFO)  # 设置日志级别

    # 控制台输出
    console_handler = logging.StreamHandler(sys.stdout)
    console_handler.setLevel(logging.INFO)
    console_formatter = logging.Formatter(
        '%(asctime)s - %(name)s - %(levelname)s - %(message)s')
    console_handler.setFormatter(console_formatter)

    # 文件输出（可选）
    file_handler = RotatingFileHandler(
        'app.log', maxBytes=1024*1024, backupCount=5)
    file_handler.setLevel(logging.INFO)
    file_formatter = logging.Formatter(
        '%(asctime)s - %(name)s - %(levelname)s - %(message)s')
    file_handler.setFormatter(file_formatter)

    # 添加处理器
    logger.addHandler(console_handler)
    logger.addHandler(file_handler)  # 可选

# 在创建app前初始化日志
setup_logging()

app = FastAPI()
logger = logging.getLogger(__name__)

UPLOAD_DIR = Path("uploads")
UPLOAD_DIR.mkdir(exist_ok=True)

def cleanup_files(*paths):
    """清理临时文件"""
    for path in paths:
        try:
            if path and Path(path).exists():
                Path(path).unlink()
                logger.info(f"已清理文件: {path}")
        except Exception as e:
            logger.error(f"清理文件失败 {path}: {str(e)}")

@app.post("/capture")
async def capture_frame_api(
        background_tasks: BackgroundTasks,
        file: UploadFile = File(...),
        timestamp: str = Form(None)
):
    """视频截图API"""
    file_id = uuid.uuid4().hex
    input_path = output_path = None

    try:
        # 1. 文件验证
        file_ext = Path(file.filename).suffix.lower()
        if file_ext not in {'.mp4', '.mov', '.avi'}:
            raise HTTPException(400, detail="仅支持MP4/MOV/AVI格式")

        # 2. 准备路径
        input_path = UPLOAD_DIR / f"{file_id}_input{file_ext}"
        output_path = UPLOAD_DIR / f"{file_id}_frame.png"

        # 3. 保存文件
        logger.info(f"保存文件到 {input_path}")
        with open(input_path, "wb") as f:
            content = await file.read()
            f.write(content)
            logger.info(f"已写入 {len(content)/1024/1024:.2f}MB")

        # 5. 执行截图
        if not capture_frame(str(input_path), str(output_path), timestamp):
            raise HTTPException(500, detail="截图失败，请检查视频格式")

        # 6. 返回响应
        return FileResponse(
            output_path,
            media_type="image/png",
            filename="capture.png"
        )

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"处理出错: {str(e)}", exc_info=True)
        raise HTTPException(500, detail="内部服务器错误")
    finally:
        # 确保清理文件（background_tasks会异步处理）
        if input_path:
            background_tasks.add_task(cleanup_files, input_path)
        if output_path:
            background_tasks.add_task(cleanup_files, output_path)