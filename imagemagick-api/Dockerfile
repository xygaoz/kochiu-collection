# 第一阶段：构建阶段
FROM --platform=$BUILDPLATFORM python:3.9-slim AS builder

# 1. 安装构建依赖
RUN echo "deb http://mirrors.aliyun.com/debian/ bookworm main non-free contrib" > /etc/apt/sources.list && \
    echo "deb http://mirrors.aliyun.com/debian-security/ bookworm-security main" >> /etc/apt/sources.list && \
    echo "deb http://mirrors.aliyun.com/debian/ bookworm-updates main non-free contrib" >> /etc/apt/sources.list && \
    apt-get update && \
    apt-get install -y \
    build-essential \
    python3-dev \
    libjpeg-dev \
    zlib1g-dev \
    && rm -rf /var/lib/apt/lists/*

# 2. 创建虚拟环境
RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

# 3. 安装Python依赖
COPY requirements.txt .
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir numpy==1.23.5 && \
    pip install --no-cache-dir -r requirements.txt

# ==============================================
# 第二阶段：运行时阶段
FROM python:3.9-slim

# 1. 安装系统依赖
RUN echo "deb http://mirrors.aliyun.com/debian/ bookworm main non-free contrib" > /etc/apt/sources.list && \
    echo "deb http://mirrors.aliyun.com/debian-security/ bookworm-security main" >> /etc/apt/sources.list && \
    echo "deb http://mirrors.aliyun.com/debian/ bookworm-updates main non-free contrib" >> /etc/apt/sources.list && \
    apt-get update && \
    apt-get install -y \
    imagemagick \
        dcraw \
        libraw-bin \
        ghostscript \
        libopenjp2-7 \
        libtiff6 \
        libjpeg62-turbo \
        zlib1g \
        && rm -rf /var/lib/apt/lists/*

# 2. 配置ImageMagick策略和委托
RUN if [ -f /etc/ImageMagick-6/policy.xml ]; then \
      mv /etc/ImageMagick-6/policy.xml /etc/ImageMagick-6/policy.xml.bak && \
      echo '<policymap>\
        <policy domain="coder" rights="read|write" pattern="PDF" />\
        <policy domain="coder" rights="read|write" pattern="PSD" />\
        <policy domain="coder" rights="read|write" pattern="NEF" />\
        <policy domain="coder" rights="read|write" pattern="ARW" />\
        <policy domain="coder" rights="read|write" pattern="CR2" />\
        <policy domain="coder" rights="read|write" pattern="DNG" />\
      </policymap>' > /etc/ImageMagick-6/policy.xml; \
    elif [ -f /etc/ImageMagick-7/policy.xml ]; then \
      mv /etc/ImageMagick-7/policy.xml /etc/ImageMagick-7/policy.xml.bak && \
      echo '<policymap>\
        <policy domain="coder" rights="read|write" pattern="PDF" />\
        <policy domain="coder" rights="read|write" pattern="PSD" />\
        <policy domain="coder" rights="read|write" pattern="NEF" />\
        <policy domain="coder" rights="read|write" pattern="ARW" />\
        <policy domain="coder" rights="read|write" pattern="CR2" />\
        <policy domain="coder" rights="read|write" pattern="DNG" />\
      </policymap>' > /etc/ImageMagick-7/policy.xml; \
    fi

# 更新ImageMagick的委托配置，使用dcraw替代ufraw
RUN if [ -f /etc/ImageMagick-6/delegates.xml ]; then \
      sed -i 's|<delegate decode="dng" command=".*"/>|<delegate decode="dng" command="dcraw -6 -w -O "%u.ppm" "%i""/>|' /etc/ImageMagick-6/delegates.xml && \
      sed -i 's|<delegate decode="nef" command=".*"/>|<delegate decode="nef" command="dcraw -6 -w -O "%u.ppm" "%i""/>|' /etc/ImageMagick-6/delegates.xml && \
      sed -i 's|<delegate decode="arw" command=".*"/>|<delegate decode="arw" command="dcraw -6 -w -O "%u.ppm" "%i""/>|' /etc/ImageMagick-6/delegates.xml && \
      sed -i 's|<delegate decode="cr2" command=".*"/>|<delegate decode="cr2" command="dcraw -6 -w -O "%u.ppm" "%i""/>|' /etc/ImageMagick-6/delegates.xml; \
    elif [ -f /etc/ImageMagick-7/delegates.xml ]; then \
      sed -i 's|<delegate decode="dng" command=".*"/>|<delegate decode="dng" command="dcraw -6 -w -O "%u.ppm" "%i""/>|' /etc/ImageMagick-7/delegates.xml && \
      sed -i 's|<delegate decode="nef" command=".*"/>|<delegate decode="nef" command="dcraw -6 -w -O "%u.ppm" "%i""/>|' /etc/ImageMagick-7/delegates.xml && \
      sed -i 's|<delegate decode="arw" command=".*"/>|<delegate decode="arw" command="dcraw -6 -w -O "%u.ppm" "%i""/>|' /etc/ImageMagick-7/delegates.xml && \
      sed -i 's|<delegate decode="cr2" command=".*"/>|<delegate decode="cr2" command="dcraw -6 -w -O "%u.ppm" "%i""/>|' /etc/ImageMagick-7/delegates.xml; \
    fi

# 3. 从构建阶段复制虚拟环境
COPY --from=builder /opt/venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

# 4. 确保命令可用
RUN ln -s /usr/bin/convert /usr/bin/magick

# 5. 最终验证步骤
RUN echo "===== 开始验证依赖安装 =====" && \
    echo "1. 验证ImageMagick支持NEF格式:" && \
    if convert -list format | grep -i nef; then \
        echo "✅ ImageMagick支持NEF格式"; \
    else \
        echo "❌ ImageMagick不支持NEF格式"; \
        exit 1; \
    fi && \
    \
    echo "2. 验证dcraw可用性:" && \
    if (dcraw -i 2>&1 | grep -q "usage:") || dcraw --version; then \
        echo "✅ dcraw可用"; \
    else \
        echo "⚠️  dcraw可能不可用，但继续构建"; \
    fi && \
    \
    echo "3. 验证libraw可用性:" && \
    if ldconfig -p | grep libraw; then \
        echo "✅ libraw已安装"; \
    else \
        echo "⚠️  未找到libraw，但继续构建"; \
    fi && \
    echo "===== 依赖验证完成 ====="

# 6. 复制应用代码
COPY app.py /app.py

# 7. 设置工作目录
WORKDIR /

# 8. 启动命令
CMD ["gunicorn", "-b", "0.0.0.0:8190", "--workers=4", "--threads=2", "--timeout", "120", "--access-logfile", "-", "--error-logfile", "-", "app:app"]
