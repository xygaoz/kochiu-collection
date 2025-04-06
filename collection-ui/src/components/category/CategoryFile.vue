<template>
    <div class="category-file-container">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="files.length === 0" class="empty">暂无文件</div>
        <template v-else>
            <el-container>
                <el-container style="margin: 0">
                    <el-main class="image-container">
                        <div class="grid-container">
                            <div v-for="(image, index) in files" :key="index" class="grid-item">
                                <el-card
                                    shadow="hover"
                                    :body-style="{ padding: '0px' }"
                                    :class="{ 'selected-card': selectedImage?.resourceId === image.resourceId }"
                                >
                                    <div class="image-wrapper" @click="handlePreview(image)">
                                        <el-image
                                            :src="image.thumbnailUrl"
                                            fit="cover"
                                            loading="lazy"
                                            class="waterfall-image"
                                            :style="{ height: `${image.height * (columnWidth / image.width)}px` }"
                                        >
                                            <template #error>
                                                <div class="image-error">
                                                    <img
                                                        src="/images/default-thumbnail.jpg"
                                                        style="height:100%;object-fit: contain;background-color: #f5f5f5;"
                                                        alt=""
                                                    >
                                                </div>
                                            </template>
                                        </el-image>
                                    </div>
                                    <div class="image-info">
                                        <div class="image-title">{{ image.title || image.sourceFileName }}</div>
                                    </div>
                                </el-card>
                            </div>
                        </div>
                    </el-main>
                    <el-aside width="280px" class="detail-aside">
                        <div class="image-details" v-if="selectedImage">
                            <div class="image-preview">
                                <!-- 图片预览 -->
                                <el-image
                                    v-if="isImage(selectedImage)"
                                    :src="selectedImage.resourceUrl"
                                    :preview-src-list="[selectedImage.resourceUrl]"
                                    fit="contain"
                                    class="preview-content"
                                    hide-on-click-modal
                                >
                                    <template #error>
                                        <div class="preview-error">
                                            <el-icon><Picture /></el-icon>
                                            <span>图片加载失败</span>
                                        </div>
                                    </template>
                                </el-image>

                                <!-- 视频预览 -->
                                <video
                                    v-else-if="isVideo(selectedImage)"
                                    controls
                                    class="preview-content"
                                    :poster="selectedImage.thumbnailUrl"
                                    @click.stop="playVideo(selectedImage)"
                                >
                                    <source :src="selectedImage.resourceUrl" :type="selectedImage.fileType">
                                    您的浏览器不支持视频播放
                                </video>

                                <!-- 音频预览 -->
                                <audio
                                    v-else-if="isAudio(selectedImage)"
                                    controls
                                    class="preview-content audio-preview"
                                >
                                    <source :src="selectedImage.resourceUrl" :type="selectedImage.fileType">
                                    您的浏览器不支持音频播放
                                </audio>

                                <!-- Office文件预览和PDF预览 -->
                                <div v-else-if="isOfficeFile(selectedImage) || isPdf(selectedImage)" class="office-preview">
                                    <el-image
                                        :src="selectedImage.thumbnailUrl"
                                        fit="contain"
                                        class="preview-content"
                                        @click="handleShowDoc(selectedImage)"
                                    >
                                        <template #error>
                                            <el-icon><Document /></el-icon>
                                            <span>点击预览按钮查看文档</span>
                                            <el-button
                                                type="primary"
                                                size="small"
                                                @click="handleShowDoc(selectedImage)"
                                            >
                                                预览文档
                                            </el-button>
                                        </template>
                                    </el-image>
                                </div>

                                <!-- 其他文件 -->
                                <div v-else class="unsupported-file">
                                    <el-image
                                        :src="selectedImage.thumbnailUrl"
                                        fit="contain"
                                        class="preview-content"
                                    >
                                        <template #error>
                                            <el-icon><Document /></el-icon>
                                            <span>不支持预览此文件类型</span>
                                        </template>
                                    </el-image>
                                </div>
                            </div>

                            <!-- 视频弹窗 -->
                            <el-dialog
                                v-model="videoDialogVisible"
                                title="视频播放"
                                width="70%"
                                top="5vh"
                                destroy-on-close
                            >
                                <video
                                    controls
                                    autoplay
                                    style="width: 100%"
                                    :src="currentVideoUrl"
                                ></video>
                            </el-dialog>
                            <div class="detail-header">
                                <div>文件详情</div>
                                <el-button
                                    type="primary"
                                    size="small"
                                    @click="handleDownload(selectedImage)"
                                    :icon="Download"
                                >
                                    下载
                                </el-button>
                            </div>

                            <el-divider />

                            <div class="detail-content">
                                <div class="detail-row">
                                    <div class="detail-label">文件名</div>
                                    <div class="detail-value">{{ selectedImage.sourceFileName }}</div>
                                </div>
                                <div class="detail-row">
                                    <div class="detail-label">文件类型</div>
                                    <div class="detail-value">{{ selectedImage.fileType }}</div>
                                </div>
                                <div class="detail-row" v-if="selectedImage.resolutionRatio">
                                    <div class="detail-label">分辨率</div>
                                    <div class="detail-value">{{ selectedImage.resolutionRatio }}</div>
                                </div>
                                <div class="detail-row">
                                    <div class="detail-label">文件大小</div>
                                    <div class="detail-value">{{ formatSize(selectedImage.size) }}</div>
                                </div>
                                <div class="detail-row">
                                    <div class="detail-label">创建时间</div>
                                    <div class="detail-value">{{ formatTime(selectedImage.createTime) }}</div>
                                </div>
                                <div class="detail-row" v-if="selectedImage.updateTime">
                                    <div class="detail-label">修改时间</div>
                                    <div class="detail-value">{{ formatTime(selectedImage.updateTime) }}</div>
                                </div>
                                <div class="detail-row">
                                    <div class="detail-label">评分</div>
                                    <div class="detail-value">
                                        <el-rate
                                            v-model="selectedImage.star"
                                            disabled
                                            show-score
                                            text-color="#ff9900"
                                            score-template="{value} 分"
                                        />
                                    </div>
                                </div>

                                <div class="tag-section" v-if="selectedImage.tags?.length">
                                    <el-divider>标签</el-divider>
                                    <div class="tags">
                                        <el-tag
                                            v-for="tag in selectedImage.tags"
                                            :key="tag"
                                            size="small"
                                            type="info"
                                            class="tag-item"
                                        >
                                            {{ tag }}
                                        </el-tag>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div v-else class="empty-aside">
                            <el-empty description="请选择图片查看详情" :image-size="100" />
                        </div>
                    </el-aside>
                </el-container>
                <el-footer>
                    <!-- 分页 -->
                    <el-pagination
                        v-if="files.length > 0"
                        class="pagination"
                        :current-page="currentPage"
                        :page-size="pageSize"
                        :total="total"
                        layout="prev, pager, next"
                        @current-change="handlePageChange"
                    />
                </el-footer>
            </el-container>
        </template>
    </div>

    <!-- PDF预览弹窗 -->
    <el-dialog
        v-model="pdfDialogVisible"
        title="Office和PDF预览"
        width="80%"
        top="5vh"
        destroy-on-close
        class="pdf-dialog"
        :fullscreen="isMobile"
    >
    <div class="pdf-container">
        <iframe
            v-if="pdfDialogVisible"
            :src="`/pdfjs/web/viewer.html?file=${encodeURIComponent(pdfPreviewUrl)}`"
            frameborder="0"
            class="pdf-iframe"
        ></iframe>
    </div>
    </el-dialog>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from "vue";
import { listCategoryFiles } from "@/apis/services";
import { useRoute } from "vue-router";
import { Resource } from "@/apis/interface";
import { Document, Download } from "@element-plus/icons-vue";

const route = useRoute();
const cateId = route.params.cateId as string;
const files = ref<Resource[]>([]);
const selectedImage = ref<Resource | null>(null);
const loading = ref(true);
const columnWidth = ref(180)
const currentPage = ref(1)
const pageSize = ref(500)
const total = ref(0)
const videoDialogVisible = ref(false)
const currentVideoUrl = ref('')
const pdfDialogVisible = ref(false);
const pdfPreviewUrl = ref('');

// 加载数据
onMounted(async () => {
    try {
        const data = await listCategoryFiles(cateId, currentPage.value, pageSize.value);
        console.log("API响应数据:", data);
        files.value = data.list;
        total.value = data.total;
        currentPage.value = data.pageNum;
    } catch (error) {
        console.error("加载失败:", error);
    } finally {
        loading.value = false;
    }

    checkMobile()
    window.addEventListener('resize', checkMobile)
});


// 辅助方法：格式化文件大小
const formatSize = (bytes?: number) => {
    if (!bytes) return "未知";
    const units = ["B", "KB", "MB", "GB"];
    let size = bytes;
    let unitIndex = 0;
    while (size >= 1024 && unitIndex < units.length - 1) {
        size /= 1024;
        unitIndex++;
    }
    return `${size.toFixed(2)} ${units[unitIndex]}`;
};

// 辅助方法：格式化时间
const formatTime = (timeStr?: string) => {
    return timeStr ? new Date(timeStr).toLocaleString() : "未知";
};

const handlePreview = (image: Resource) => {
    selectedImage.value = image;
}

const handlePageChange = (page: number) => {
    currentPage.value = page
    // 实际项目中这里应该调用API获取对应页面的数据
}

const handleShowDoc = (image: Resource) => {
    pdfPreviewUrl.value = image.previewUrl;
    pdfDialogVisible.value = true;
};

const handleDownload = (image: Resource) => {
    // 实现下载逻辑
    console.log('下载文件:', image.sourceFileName);
};

// 文件类型判断方法
const isImage = (file: Resource) => {
    return file.typeName == 'image'
}

const isVideo = (file: Resource) => {
    return file.typeName == 'video'
}

const isAudio = (file: Resource) => {
    return file.typeName == 'audio'
}

// 播放视频方法
const playVideo = (file: Resource) => {
    currentVideoUrl.value = file.resourceUrl;
    videoDialogVisible.value = true
}

// 新增文件类型判断方法
const isOfficeFile = (file: Resource) => {
    const officeTypes = [
        'application/msword',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        'application/vnd.ms-excel',
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        'application/vnd.ms-powerpoint',
        'application/vnd.openxmlformats-officedocument.presentationml.presentation'
    ];
    return officeTypes.includes(file.mimeType) && file.previewUrl;
};

const isPdf = (file: Resource) => {
    return file.fileType === 'application/pdf' && file.previewUrl;
};

const isMobile = ref(false)

const checkMobile = () => {
    isMobile.value = window.innerWidth <= 768
}

onBeforeUnmount(() => {
    window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped>
.loading{
    padding: 20px;
    text-align: center;
    color: #666;
}

.empty{
    padding: 100px;
    text-align: center;
    color: #666;
    font-size: 21px;
    width: 100%;
}

.category-file-container {
    display: flex;
    height: 100%;
}

.grid-container {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    grid-auto-flow: dense;
    gap: 16px;
}

.grid-item {
    break-inside: avoid;
}

/* 图片容器 */
.image-wrapper {
    cursor: pointer;
    overflow: hidden;
    border-radius: 4px 4px 0 0;
    max-height: 300px;  /* 新增：限制最大高度 */
    display: flex;       /* 新增：确保图片居中 */
    align-items: center; /* 新增：垂直居中 */
    justify-content: center; /* 新增：水平居中 */
    background-color: #f5f5f5; /* 可选：添加背景色 */
}

/* 图片本身 */
.waterfall-image {
    width: 100%;
    max-height: 300px;  /* 关键：限制图片最大高度 */
    object-fit: contain; /* 保持比例，完整显示 */
    display: block;
    transition: transform 0.3s ease;
}

.waterfall-image:hover {
    transform: scale(1.05);
}

.image-error {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center; /* 水平居中 */
    align-items: center;     /* 垂直居中（可选） */
    overflow: hidden;        /* 防止图片溢出 */
}

/* 图片加载错误时的占位图 */
.image-error img {
    max-width: 100%;
    max-height: 300px;  /* 与主图保持一致 */
    object-fit: contain;
}

.image-info {
    text-align: center;
    font-size: 14px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    background-color: #f1f1f1;
}

.image-title{
    padding: 12px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

/* 响应式调整 */
@media (max-width: 768px) {
    .image-gallery {
        grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    }

    .image-container {
        height: 150px; /* 移动端减小高度 */
        margin: 0!important;
        padding: 15px;
    }
}

.image-details {
    width: 100%;
    height: 100%;
    background-color: #f9f9f9;
}

.tags span {
    display: inline-block;
    background-color: #e0e0e0;
    padding: 2px 8px;
    margin-right: 6px;
    border-radius: 4px;
    font-size: 12px;
}

.el-footer{
    height: 36px;
    background: white;
}

.el-card {
    width: 100%;
    transition: all 0.3s ease;
    border-radius: 5px;
    overflow: hidden;
    border: 1px solid #ebeef5;
    margin-bottom: 0; /* 由 masonry-item 控制间距 */
}

/* 普通卡片悬停效果 */
.el-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

/* 选中卡片样式 */
.selected-card {
    border: 1px solid #409EFF !important;
}

/* 选中卡片悬停效果 */
.selected-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3) !important;
    border-color: #409EFF !important;
}

@keyframes card-selected {
    from { border-width: 0; opacity: 0; }
    to { border-width: 2px; opacity: 1; }
}

.image-container{
    margin: 0;
    padding: 15px;
}

/* 详情侧边栏样式 */
.detail-aside {
    background-color: #fff;
    border-left: 1px solid #e6e6e6;
    display: flex;
    flex-direction: column;
    overflow: hidden; /* 禁止滚动条 */
}

.detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 16px 0;
    flex-shrink: 0; /* 防止压缩 */
}

.detail-content {
    padding: 0 16px;
    flex-grow: 1;
    overflow-y: auto; /* 内容区域可滚动 */
    margin-bottom: 16px;
    background: #fafafa;
}

.detail-table {
    margin-bottom: 16px;
}

:deep(.detail-table .el-descriptions__label) {
    width: 80px;
    color: #666;
}

.tag-section {
    margin-top: 8px;
}

.tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.empty-aside {
    display: flex;
    flex-direction: column;
    justify-content: center;
    height: 100%;
    padding: 20px;
}

/* 响应式调整 */
@media (max-width: 1200px) {
    .masonry-grid {
        padding: 0 10px;
    }
}

@media (max-width: 768px) {
    .masonry-item {
        margin-bottom: 12px;
    }
}

.detail-row {
    display: flex;
    margin-bottom: 12px;
    font-size: 12px;
}
.detail-label {
    width: 80px;
    color: #666;
    padding-right: 12px;
}
.detail-value {
    flex: 1;
}

.el-rate{
    height: 18px!important;
}

.image-preview {
    height: 300px; /* 增大高度以适应文档预览 */
    padding: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f5f5f5;
    border-bottom: 1px solid #eee;
    overflow: hidden;
}

/* 图片/视频预览内容 */
.preview-content {
    max-width: 100%;      /* 宽度不超过容器 */
    max-height: 100%;     /* 高度不超过容器 */
    object-fit: contain;  /* 保持比例，完整显示 */
    display: block;       /* 避免图片底部间隙 */
    margin: 0 auto;       /* 水平居中 */
    cursor: pointer;
}

.audio-preview {
    width: 100%;
    padding: 0 20px;
}

.preview-error,
.unsupported-file {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #999;
    font-size: 14px;
    width: 100%;
    height: 100%;
}

.preview-error .el-icon,
.unsupported-file .el-icon {
    font-size: 48px;
    margin-bottom: 10px;
}

.unsupported-file .el-image {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
}

/* 未支持文件的图片 */
.unsupported-file .el-image__inner {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
}

.office-preview, .pdf-preview {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: #f5f5f5;
}

.office-placeholder .el-icon {
    font-size: 48px;
    color: #409eff;
}

/* PDF 弹窗容器 */
.pdf-dialog {
    display: flex;
    flex-direction: column;
}

/* 响应式判断（可选） */
@media (max-width: 768px) {
    .pdf-dialog {
        width: 95% !important;
        top: 2vh !important;
    }
}

/* PDF 容器（关键样式） */
.pdf-container {
    position: relative;
    width: 100%;
    height: calc(100vh - 150px); /* 动态高度 */
    min-height: 500px; /* 最小高度保证 */
    overflow: hidden;
    background-color: #f5f5f5;
    border-radius: 4px;
}

/* PDF iframe（关键样式） */
.pdf-iframe {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    border: none;
}

/* 移动端适配 */
@media (max-width: 768px) {
    .pdf-container {
        height: calc(100vh - 120px); /* 移动端更大高度 */
        min-height: 300px;
    }
}

</style>