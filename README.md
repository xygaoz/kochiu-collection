# KoChiu Collection
一个类似兰空图床的资源管理系统，但是不仅仅是管理图片，还有视频，文档，音频等资源。<br>
## 安装
- 安装 Java v17 或更高版本。
- 下载或执行maven打包
- 创建运行环境配置文件 `application-prod.yml`，参考 `application.yml.example`。
- 运行 `java -jar kochiu-collection-1.0.0.jar -active=prod`。
- 访问 `http://localhost:8080`。
### 后端依赖
  - 安装 LibreOffice(可选)<br>
  如不安装LibreOffice，office文件生成缩略图可能会比较粗糙，且不能预览。
    - Windows: 从 [LibreOffice](https://www.libreoffice.org/)官网 下载安装，默认路径为 C:\Program Files\LibreOffice。
    - Linux: sudo apt install libreoffice (Ubuntu/Debian) 或 sudo yum install libreoffice (CentOS)。
    - macOS: 通过 Homebrew 安装：brew install --cask libreoffice。
    - docker:
      - arm64架构<br>
        docker run -d \
        -p 8080:8080 \
        --name jodconverter \
        -v /path/fonts:/usr/share/fonts/chinese \ # 替换为实际中文字体路径
        -e SPRING_SECURITY_USER_NAME=admin \  # 替换为实际用户名
        -e SPRING_SECURITY_USER_PASSWORD=123456 \ # 替换为实际密码
        -e SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=500MB \ # 最大文件大小
        -e SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=500MB \ # 最大请求大小
        -e JODCONVERTER_TASK_TIMEOUT=300000 \     # 5分钟超时
        -e JAVA_OPTS="-Xmx2g -Xms1g" \           # 增加JVM内存
        -e JODCONVERTER_OFFICE_PROCESS_COUNT=2 \  # 启动多个Office进程
        -e JODCONVERTER_STARTPROCESS_MAXRETRIES=5 \
        --memory="3g" \                          # 容器内存限制
        --restart always \
        eugenmayer/jodconverter:rest-0.2.0<br><br>
        文档类型基准测试：
      
        | 文档类型 | 推荐超时 | 内存需求 |
        |-------|-------|-------|
        | 普通DOCX | 2分钟 | 1GB |
        | 含图PPTX | 5分钟 | 2GB |
        | 大型XLSX | 10分钟 | 4GB |

      - x86_64架构<br>
      

    - 
      <br><br>
- 安装ffmpeg(可选)<br>
  从 [FFmpeg](https://ffmpeg.org/)官网下载安装。安装之后可以从视频截帧生成缩略图，否则会使用默认的图标代替缩略图。<br>

### 客户端
- PicGo
  1. 下载并安装[PicGo](https://github.com/Molunerfinn/PicGo)。
  2. 插件设置，搜索"web-uploader-byzh 1.1.1"安装
  3. 图床设置->自定义Web图床，填入相关信息，点击保存。
![img.png](docs/img.png)
  4. 上传区，选择设置好的图床
  ![img_1.png](docs/img_1.png)