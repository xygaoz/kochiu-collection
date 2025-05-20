import subprocess
from pathlib import Path
import logging

logger = logging.getLogger(__name__)

def get_video_duration(input_path: str) -> float:
    """获取视频时长(秒)"""
    cmd = [
        "ffprobe",
        "-v", "error",
        "-show_entries", "format=duration",
        "-of", "default=noprint_wrappers=1:nokey=1",
        input_path
    ]
    try:
        result = subprocess.run(
            cmd,
            check=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True
        )
        return float(result.stdout.strip())
    except subprocess.CalledProcessError as e:
        logger.error(f"获取视频时长失败: {e.stderr}")
        return 0

def calculate_default_timestamp(duration: float) -> str:
    """计算默认截取时间点"""
    # 取5秒或10%处，取较小值
    target_sec = max(5, duration * 0.1)
    hours = int(target_sec // 3600)
    minutes = int((target_sec % 3600) // 60)
    seconds = int(target_sec % 60)
    return f"{hours:02d}:{minutes:02d}:{seconds:02d}"

def capture_frame(input_path: str, output_path: str, timestamp: str = None) -> bool:
    """使用FFmpeg截取视频帧"""
    # 1. 预检查
    if not Path(input_path).exists():
        logger.error(f"输入文件不存在: {input_path}")
        return False

    # 2. 处理时间点参数
    if timestamp is None or timestamp.strip() == "":
        duration = get_video_duration(input_path)
        logger.info(f"视频时长: {duration}")
        if duration <= 0:
            logger.error("无法获取视频时长，使用默认时间点00:00:05")
            timestamp = "00:00:05"
        else:
            timestamp = calculate_default_timestamp(duration)
        logger.info(f"自动计算截帧时间点: {timestamp}")
    else:
        logger.info(f"使用用户指定时间点: {timestamp}")

    # 3. 准备命令
    cmd = [
        "ffmpeg",
        "-hide_banner",
        "-loglevel", "error",
        "-i", input_path,
        "-ss", timestamp,
        "-vframes", "1",
        "-q:v", "2",
        "-f", "image2",
        "-y",
        "-vf", "scale='min(640,iw)':-2",
        output_path
    ]

    # 4. 执行命令
    try:
        logger.info(f"执行命令: {' '.join(cmd)}")
        result = subprocess.run(
            cmd,
            check=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            timeout=30,
            encoding='utf-8',
            errors='ignore'
        )

        # 5. 结果验证
        if not Path(output_path).exists():
            error_msg = f"FFmpeg未生成输出文件\nSTDERR:\n{result.stderr}"
            logger.error(error_msg)
            return False

        return True

    except subprocess.CalledProcessError as e:
        logger.error(f"FFmpeg失败: {e.stderr}")
        return False
    except Exception as e:
        logger.error(f"未知错误: {str(e)}")
        return False