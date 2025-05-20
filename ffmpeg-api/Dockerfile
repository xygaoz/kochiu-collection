FROM linuxserver/ffmpeg:latest

# 1. 修复软件源并安装系统依赖
RUN sed -i 's/deb.debian.org/mirrors.tuna.tsinghua.edu.cn/g' /etc/apt/sources.list && \
    sed -i 's/security.debian.org/mirrors.tuna.tsinghua.edu.cn/g' /etc/apt/sources.list && \
    apt-get update -o Acquire::Check-Valid-Until=false && \
    apt-get install -y --no-install-recommends \
    python3-pip \
    python3-dev \
    gcc \
    libffi-dev \
    && rm -rf /var/lib/apt/lists/*

# 2. 升级pip工具
RUN python3 -m pip install --upgrade --ignore-installed pip setuptools wheel --break-system-packages && \
    pip --version

# 3. 安装应用依赖
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt --break-system-packages

# 4. 复制代码
WORKDIR /app
COPY . .

# 5. 创建上传目录
RUN mkdir -p /app/uploads

# 6. 清除默认ENTRYPOINT
ENTRYPOINT []

# 7. 设置启动命令
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]

EXPOSE 8000

