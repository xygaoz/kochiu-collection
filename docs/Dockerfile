# 使用linuxserver/ffmpeg作为基础镜像
FROM linuxserver/ffmpeg:latest

# 安装OpenJDK 17（以下二选一）
# RUN apt-get update && \
#    apt-get install -y ca-certificates-java && \
#    apt-get install -y openjdk-17-jdk && \
#    rm -rf /var/lib/apt/lists/*
RUN apt-get update && \
    apt-get install -y wget && \
    wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | apt-key add - && \
    echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list && \
    apt-get update && \
    apt-get install -y temurin-17-jdk && \
    rm -rf /var/lib/apt/lists/*

# 创建工作目录
RUN mkdir -p /app && \
    chown -R abc:abc /app && \
    chmod -R 755 /app

# 设置工作目录
WORKDIR /app

# 设置环境变量
ENV DB_PATH=/app/db \
    username=admin \
    password=admin \
    JAVA_OPTS="--enable-native-access=ALL-UNNAMED -Xms512m -Xmx4g -XX:MaxDirectMemorySize=2G"

# 暴露端口
EXPOSE 9000

# 创建挂载点
VOLUME ["/app/db", "/app/logs", "/app/resources", "/config"]

# 使用非root用户运行
USER abc

# 重置基础镜像的ENTRYPOINT
ENTRYPOINT []

# 启动命令
CMD ["java", "-jar", "/app/app.jar", "--spring.profiles.active=prod", "--spring.config.additional-location=file:/config/"]