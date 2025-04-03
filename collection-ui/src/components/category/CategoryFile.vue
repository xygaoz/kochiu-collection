<template>
    <div class="category-file-container">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="files.length === 0" class="empty">暂无文件</div>
        <template v-else>
            <el-container>
                <el-container>
                    <el-main class="image-container">
                        <div
                            v-for="(image, index) in files"
                            :key="index"
                            class="waterfall-item"
                            :style="{ width: `${columnWidth}px` }"
                        >
                            <el-card shadow="hover" :body-style="{ padding: '0px' }">
                                <div class="image-wrapper" @click="handlePreview(image)">
                                    <el-image
                                        :src="image.thumbnailUrl"
                                        fit="cover"
                                        loading="lazy"
                                        class="waterfall-image"
                                        :style="{ height: `${image.height * (columnWidth / image.width)}px` }"
                                        @error="handleImageError"
                                    />
                                </div>
                                <div class="image-info">
                                    <div class="image-title">{{ image.title || image.sourceFileName }}</div>
                                </div>
                            </el-card>
                        </div>
                    </el-main>
                    <el-aside width="200px">
                        <div class="image-details" v-if="selectedImage">
                            <p><strong>名称:</strong> {{ selectedImage.sourceFileName }}</p>
                            <p><strong>评分:</strong> {{ selectedImage.star || "无" }}</p>
                            <p><strong>大小:</strong> {{ formatSize(selectedImage.size) }}</p>
                            <p><strong>创建时间:</strong> {{ formatTime(selectedImage.createdTime) }}</p>
                            <p><strong>修改时间:</strong> {{ formatTime(selectedImage.updateTime) }}</p>
                            <div class="tags" v-if="selectedImage.tags?.length">
                                <span v-for="tag in selectedImage.tags" :key="tag">{{ tag }}</span>
                            </div>
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
import defaultThumbnail from "@/assets/imgs/default-thumbnail.jpg";

const route = useRoute();
const cateId = route.params.cateId as string;
const files = ref<Resource[]>([]);
const selectedImage = ref<Resource | null>(null);
const loading = ref(true);
const columnWidth = ref(240)
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

// 图片加载失败处理
const handleImageError = (e: Event) => {
    const img = e.target as HTMLImageElement;
    img.src = defaultThumbnail;
};

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

</script>

<style scoped>
/* 原有样式基础上补充 */
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

.waterfall-item {
    transition: all 0.3s ease;
    margin-bottom: 16px;

    &:hover {
        transform: translateY(-5px);
        box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
    }
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
    padding: 12px;
    text-align: center;
    font-size: 14px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    background: white;
}

/* 响应式调整 */
@media (max-width: 768px) {
    .image-gallery {
        grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    }

    .image-container {
        height: 150px; /* 移动端减小高度 */
    }
}

.image-details {
    width: 30%;
    padding: 24px;
    background-color: #f9f9f9;
    border-left: 1px solid #ddd;
}

.tags span {
    display: inline-block;
    background-color: #e0e0e0;
    padding: 2px 8px;
    margin-right: 6px;
    border-radius: 4px;
    font-size: 12px;
}

.el-main{
    margin: 0;
}

.el-footer{
    height: 36px;
    background: white;
}

.image-container{
    margin: 10px;
}
</style>