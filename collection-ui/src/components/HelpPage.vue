<template>
    <div class="help-container">
        <div class="main-layout">
            <el-aside class="side-menu-area">
                <el-menu
                    default-active="1"
                    class="side-menu"
                    @select="handleMenuSelect"
                >
                    <el-menu-item index="1">
                        <el-icon><icon-menu /></el-icon>
                        <span>系统简介</span>
                    </el-menu-item>
                    <el-menu-item index="2">
                        <el-icon><setting /></el-icon>
                        <span>安装指南</span>
                    </el-menu-item>
                    <el-menu-item index="3">
                        <el-icon><upload /></el-icon>
                        <span>客户端配置</span>
                    </el-menu-item>
                    <el-menu-item index="4">
                        <el-icon><question-filled /></el-icon>
                        <span>常见问题</span>
                    </el-menu-item>
                </el-menu>
            </el-aside>

            <div class="content-wrapper">
                <el-main class="content-area">
                    <div v-show="activeTab === '1'">
                        <h2>系统简介</h2>
                        <el-card shadow="hover">
                            <p>KoChiu Collection 是一个多功能资源管理系统，支持管理图片、视频、文档、音频等多种类型的资源。</p>

                            <el-carousel :interval="4000" type="card" height="300px">
                                <el-carousel-item v-for="(img, index) in screenshots" :key="index">
                                    <el-image :src="img" fit="contain" style="height: 100%;" />
                                </el-carousel-item>
                            </el-carousel>

                            <el-divider />

                            <h3>主要功能</h3>
                            <el-row :gutter="20">
                                <el-col :span="8" v-for="(feature, index) in features" :key="index">
                                    <el-card shadow="hover" class="feature-card">
                                        <template #header>
                                            <div class="feature-header">
                                                <el-icon :size="24" :color="feature.color">
                                                    <component :is="feature.icon" />
                                                </el-icon>
                                                <span>{{ feature.title }}</span>
                                            </div>
                                        </template>
                                        <div class="feature-content">
                                            {{ feature.desc }}
                                        </div>
                                    </el-card>
                                </el-col>
                            </el-row>
                        </el-card>
                    </div>

                    <!-- 安装指南 -->
                    <div v-show="activeTab === '2'">
                        <h2>安装指南</h2>
                        <el-card shadow="hover">
                            <h3>环境要求</h3>
                            <el-tag type="success">Java v17 或更高版本</el-tag>

                            <el-divider />

                            <h3>安装步骤</h3>
                            <el-steps :active="4" align-center>
                                <el-step title="下载" description="下载已打包好的jar或clone代码执行maven打包"/>
                                <el-step title="配置" description="创建运行环境配置文件 application-prod.yml"/>
                                <el-step title="运行" description="根据系统选择运行方式"/>
                                <el-step title="访问" description="访问系统默认地址 http://127.0.0.1:9000/"/>
                            </el-steps>

                            <el-divider />

                            <h3>运行方式</h3>
                            <el-tabs type="border-card">
                                <el-tab-pane label="Windows">
                          <pre><code>双击 run.bat
# 或
在命令窗口运行 run.sh</code></pre>
                                </el-tab-pane>

                                <el-tab-pane label="Linux/MacOS">
                                    <pre><code>sh run.sh</code></pre>
                                </el-tab-pane>

                                <el-tab-pane label="Docker部署">
                                    <el-radio-group v-model="dockerOption">
                                        <el-radio label="1">方案1: 独立容器</el-radio>
                                        <el-radio label="2">方案2: 合并容器</el-radio>
                                    </el-radio-group>

                                    <div v-if="dockerOption === '1'">
                                        <h4>方案1: 独立容器</h4>
                                        <el-alert title="注意" type="warning" :closable="false">
                                            如果需要高清缩略图，请安装jodconverter和ffmpeg容器
                                        </el-alert>
                                        <br>
                                        <span>修改application-prod.yml</span>
                                        <pre>
                                            <code>
collection:
  jodconverter:
    enabled: true
    mode: remote  # 如果要使用jodconverter，必须设置为远程模式，参考下面docker安装jodconverter
    remote:
      username: admin
      password: 123456
      api-url: http://192.168.1.100:8080 #远程服务地址，ip为docker容器的机器ip，端口为docker jodconverter的映射端口
      timeout: 300000 #单位：毫秒(5分钟)
  ffmpeg:
    enabled: true
    mode: remote
    remote:
      api-url: http://localhost:8000/capture #远程服务接口地址
#设置日志文件路径
logging:
  file:
    path: /home/logs
                                            </code>
                                        </pre>
                                        <span>运行容器</span>
                                        <pre>
                                            <code>
docker run -d \
  -p 9000:9000 \
  --name kochiu-collection \
  -v /path/kochiu-collection-0.1.0.jar:/app/app.jar \ # 替换为实际jar包路径
  -v /path/application-prod.yml:/config/application-prod.yml \ # 替换为实际配置文件路径
  -v /path/logs:/app/logs \ # 替换为实际日志文件路径
  -v /path/db:/app/db \ # 替换为实际数据库文件存放路径
  -v /path/resources:/app/resources \ # 替换为实际资源文件存放路径
  -e DB_PATH=/app/db \
  -e username=admin \ # 登录用户名（可选，默认admin）
  -e password=admin \ # 登录密码（可选，默认admin）
  -e JAVA_OPTS="--enable-native-access=ALL-UNNAMED -Xms512m -Xmx2048m" \
  openjdk:17-jdk \
  sh -c "mkdir -p /app/db && chmod -R 777 /app/db && java \${JAVA_OPTS} -jar /app/app.jar --spring.profiles.active=prod --spring.config.additional-location=file:/config/"
                                            </code>
                                        </pre>
                                    </div>

                                    <div v-else>
                                        <h4>方案2: 合并容器</h4>
                                        <el-alert title="注意" type="warning" :closable="false">
                                            如果需要高清缩略图，请安装jodconverter容器
                                        </el-alert>
                                        <br>
                                        <span>修改application-prod.yml</span>
                                        <pre>
                                            <code>
collection:
  jodconverter:
    enabled: true
    mode: remote  # 如果要使用jodconverter，必须设置为远程模式，参考下面docker安装jodconverter
    remote:
      username: admin
      password: 123456
      api-url: http://192.168.1.100:8080 #远程服务地址，ip为docker容器的机器ip，端口为docker jodconverter的映射端口
      timeout: 300000 #单位：毫秒(5分钟)
  ffmpeg:
    enabled: true
    mode: local
#设置日志文件路径
logging:
  file:
    path: /home/logs
                                            </code>
                                        </pre>
                                        <span>运行容器</span>
                                        <pre><code>cd docs

# 构建
docker build -t kochiu-collection .

# 运行容器
docker run -d \
-p 9000:9000 \
--name kochiu-collection \
-v /path/to/kochiu-collection-0.1.0.jar:/app/app.jar \
-v /path/to/application-prod.yml:/config/application-prod.yml \
-v /path/logs:/app/logs \
-v /path/db:/app/db \
-v /path/resources:/app/resources \
-v /path/to/resources:/app/resources \
kochiu-collection</code></pre>
                                    </div>
                                </el-tab-pane>
                            </el-tabs>

                            <el-divider />
                            <h3>应用</h3>
                            <span>
                                访问 `http(https)://ip(域名):port/`，默认端口9000，如：`http://127.0.0.1:9000/`,  登录账号密码默认为admin/admin，或使用docker设置的初始值。
                            </span>
                            <el-divider />

                            <h3>后端依赖</h3>
                            <el-collapse accordion>
                                <el-collapse-item title="LibreOffice安装">
                                    <el-tabs>
                                        <el-tab-pane label="Windows">
                                            <p>从 <el-link type="primary" href="https://www.libreoffice.org/" target="_blank">LibreOffice官网</el-link> 下载安装，默认路径为 C:\Program Files\LibreOffice。</p>
                                        </el-tab-pane>
                                        <el-tab-pane label="Linux">
                          <pre><code>sudo apt install libreoffice  # Ubuntu/Debian
sudo yum install libreoffice  # CentOS</code></pre>
                                        </el-tab-pane>
                                        <el-tab-pane label="macOS">
                                            <pre><code>brew install --cask libreoffice</code></pre>
                                        </el-tab-pane>
                                        <el-tab-pane label="Docker">
                          <pre><code>docker run -d \
  -p 8080:8080 \
  --name jodconverter \
  -v /path/fonts:/usr/share/fonts/chinese \
  -e SPRING_SECURITY_USER_NAME=admin \
  -e SPRING_SECURITY_USER_PASSWORD=123456 \
  -e SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=500MB \
  -e SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=500MB \
  -e JODCONVERTER_TASK_TIMEOUT=300000 \
  -e JAVA_OPTS="-Xmx2g -Xms1g" \
  -e JODCONVERTER_OFFICE_PROCESS_COUNT=2 \
  -e JODCONVERTER_STARTPROCESS_MAXRETRIES=5 \
  --memory="3g" \
  --restart always \
  eugenmayer/jodconverter:rest-0.2.0</code></pre>
                                        </el-tab-pane>
                                    </el-tabs>
                                </el-collapse-item>

                                <el-collapse-item title="FFmpeg安装">
                                    <el-tabs>
                                        <el-tab-pane label="Linux">
                                            <pre><code>sudo apt install ffmpeg</code></pre>
                                            <p>查找安装目录：<code>which ffmpeg</code></p>
                                        </el-tab-pane>
                                        <el-tab-pane label="macOS">
                                            <pre><code>brew install ffmpeg</code></pre>
                                            <p>查找安装目录：<code>which ffmpeg</code></p>
                                        </el-tab-pane>
                                        <el-tab-pane label="Windows">
                                            <p>从 <el-link type="primary" href="https://ffmpeg.org/" target="_blank">FFmpeg官网</el-link> 下载安装，默认路径为 C:\Program Files\FFmpeg。</p>
                                        </el-tab-pane>
                                        <el-tab-pane label="Docker">
                          <pre><code>cd ffmpeg-api

# 构建
docker build -t ffmpeg-api .

# 运行容器
docker run -d \
-p 8000:8000 \
-v /path/Temp:/app/uploads \
--name ffmpeg-api \
ffmpeg-api</code></pre>
                                        </el-tab-pane>
                                    </el-tabs>
                                </el-collapse-item>
                            </el-collapse>
                        </el-card>
                    </div>

                    <!-- 客户端配置 -->
                    <div v-show="activeTab === '3'">
                        <h2>PicGo客户端配置</h2>
                        <el-card shadow="hover">
                            <el-steps :active="5" align-center>
                                <el-step title="安装PicGo" description="下载并安装PicGo"/>
                                <el-step title="安装插件" description="搜索并安装web-uploader-byzh 1.1.1插件"/>
                                <el-step title="图床设置" description="配置自定义Web图床"/>
                                <el-step title="上传测试" description="选择设置好的图床进行上传"/>
                                <el-step title="验证结果" description="查看上传结果确认配置正确"/>
                            </el-steps>

                            <el-divider />

                            <h3>详细配置步骤</h3>
                            <el-collapse accordion>
                                <el-collapse-item title="1. 安装PicGo">
                                    <p>下载并安装 <el-link type="primary" href="https://github.com/Molunerfinn/PicGo" target="_blank">PicGo</el-link>。</p>
                                    <el-image :src="img1" fit="contain" />
                                </el-collapse-item>

                                <el-collapse-item title="2. 安装插件">
                                    <p>插件设置，搜索"web-uploader-byzh 1.1.1"安装</p>
                                    <el-image :src="imgDefault" fit="contain" />
                                </el-collapse-item>

                                <el-collapse-item title="3. 图床设置">
                                    <p>图床设置->自定义Web图床，填入相关信息：</p>
                                    <el-descriptions :column="1" border>
                                        <el-descriptions-item label="API地址">
                                            http(https)://ip(域名):port/collection/api/v1/resource/upload
                                        </el-descriptions-item>
                                        <el-descriptions-item label="图床域名">
                                            http(https)://ip(域名):port/collection/resource
                                        </el-descriptions-item>
                                        <el-descriptions-item label="POST参数名">
                                            file（不能改）
                                        </el-descriptions-item>
                                        <el-descriptions-item label="JSON路径">
                                            model.thumbnailUrl（不能改）
                                        </el-descriptions-item>
                                        <el-descriptions-item label="自定义请求头">
                                            用json格式填入token，例如：{"Authorization":"xxxx"}，必填项
                                        </el-descriptions-item>
                                    </el-descriptions>
                                    <el-image :src="img3" fit="contain" />
                                </el-collapse-item>

                                <el-collapse-item title="4. 自定义Body">
                                    <p>可以用json格式，例如：{"categoryId":1}，上传是默认传到某个分类下</p>
                                    <el-image :src="img2" fit="contain" />
                                </el-collapse-item>

                                <el-collapse-item title="5. 上传验证">
                                    <p>上传区，选择设置好的图床</p>
                                    <el-image :src="img1" fit="contain" />
                                    <p>上传后，如PicGo能看到缩略图，即配置无误</p>
                                    <el-image :src="img4" fit="contain" />
                                </el-collapse-item>
                            </el-collapse>
                        </el-card>
                    </div>

                    <!-- 常见问题 -->
                    <div v-show="activeTab === '4'">
                        <h2>常见问题</h2>
                        <el-card shadow="hover">
                            <el-collapse accordion>
                                <el-collapse-item title="如何获取API token?">
                                    <p>登录系统后，在用户设置中可以获取API token。</p>
                                </el-collapse-item>

                                <el-collapse-item title="上传文件大小限制是多少?">
                                    <p>默认上传限制为500MB，可以在application-prod.yml中修改配置。</p>
                                </el-collapse-item>

                                <el-collapse-item title="为什么office文件预览效果不好?">
                                    <p>请确保已安装LibreOffice并正确配置jodconverter。</p>
                                </el-collapse-item>

                                <el-collapse-item title="视频缩略图无法生成?">
                                    <p>请检查ffmpeg是否安装并正确配置。</p>
                                </el-collapse-item>

                                <el-collapse-item title="如何修改默认管理员密码?">
                                    <p>可以通过环境变量或配置文件修改默认管理员密码。</p>
                                </el-collapse-item>
                            </el-collapse>
                        </el-card>
                    </div>
                </el-main>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import {
    Menu as IconMenu,
    Setting,
    Upload,
    QuestionFilled
} from '@element-plus/icons-vue'

import img1 from '@/assets/help/img_1.png'
import img2 from '@/assets/help/img_2.png'
import img3 from '@/assets/help/img_3.png'
import img4 from '@/assets/help/img_4.png'
import img5 from '@/assets/help/img_5.png'
import img6 from '@/assets/help/img_6.png'
import img7 from '@/assets/help/img_7.png'
import imgDefault from '@/assets/help/img.png'

// 响应式变量
const activeTab = ref('1')
const dockerOption = ref('1')

// 轮播图图片数组
const screenshots = [img5, img6, img7]

const features = [
    {
        icon: 'Picture',
        title: '图片管理',
        desc: '支持多种图片格式上传、预览和管理',
        color: '#67C23A'
    },
    {
        icon: 'VideoPlay',
        title: '视频管理',
        desc: '支持视频上传、截帧预览和分类管理',
        color: '#409EFF'
    },
    {
        icon: 'Document',
        title: '文档管理',
        desc: '支持Office文档预览和缩略图生成',
        color: '#E6A23C'
    },
    {
        icon: 'Headset',
        title: '音频管理',
        desc: '支持音频文件上传和在线播放',
        color: '#F56C6C'
    },
    {
        icon: 'Folder',
        title: '分类管理',
        desc: '支持分类和多级目录，以及标签管理',
        color: '#909399'
    },
    {
        icon: 'User',
        title: '用户权限',
        desc: '支持多用户和权限控制',
        color: '#B37FEB'
    }
]

const handleMenuSelect = (index) => {
    activeTab.value = index
}

</script>

<style scoped>
/* 移除fixed定位，改用常规布局 */
.help-container {
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

/* 主容器设置 */
.main-layout {
    display: flex;
    width: 100%;
    height: 100%;
    min-height: 0;
}

/* 侧边菜单区域 */
.side-menu-area {
    width: 250px;
    height: 100%;
    overflow: hidden;
    border-right: 1px solid var(--el-border-color);
    flex-shrink: 0;
}

.side-menu {
    height: 100%;
    overflow-y: auto;
}

/* 主内容区域 */
.content-wrapper {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;
    overflow: hidden;
}

/* 内容滚动区域 */
.content-area {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
    overflow-x: hidden;
    min-height: 0;
}

/* 卡片内容限制 */
.el-card {
    width: 100%;
    max-width: 100%;
    overflow: hidden;
}

/* 预格式文本处理 */
pre {
    max-width: 100%;
    overflow-x: auto;
    white-space: pre-wrap;
    word-wrap: break-word;
}

/* 其他样式保持不变 */
.feature-card {
    margin-bottom: 20px;
    min-height: 150px;
}

.feature-header {
    display: flex;
    align-items: center;
    gap: 10px;
}

.feature-content {
    padding: 10px;
    font-size: 14px;
    color: #666;
}

h2{
    margin: 0 0 10px 0;
}
</style>