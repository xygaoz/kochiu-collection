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
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { listCategoryFiles } from "@/apis/services";
import { useRoute } from "vue-router";
import { Resource } from "@/apis/interface";
import { Download } from "@element-plus/icons-vue";

const route = useRoute();
const cateId = route.params.cateId as string;
const files = ref<Resource[]>([]);
const selectedImage = ref<Resource | null>(null);
const loading = ref(true);
const columnWidth = ref(180)
const currentPage = ref(1)
const pageSize = ref(500)
const total = ref(0)

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

const handleDownload = (image: Resource) => {
    // 实现下载逻辑
    console.log('下载文件:', image.sourceFileName);
};
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

.image-wrapper {
    cursor: pointer;
    overflow: hidden;
    border-radius: 4px 4px 0 0;
}

.waterfall-image {
    width: 100%;
    display: block;
    transition: transform 0.3s ease;

    &:hover {
        transform: scale(1.05);
    }
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

.image-error {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center; /* 水平居中 */
    align-items: center;     /* 垂直居中（可选） */
    overflow: hidden;        /* 防止图片溢出 */
}

.image-error img {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;     /* 保持比例完整显示 */
    display: block;          /* 消除图片底部间隙 */
    margin: 0 auto;          /* 水平居中备用方案 */
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
</style>