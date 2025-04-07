<template>
    <div class="category-file-container">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="files.length === 0" class="empty">暂无文件</div>
        <template v-else>
            <div class="main-layout">
                <el-main class="image-container">
                    <div
                        class="waterfall-container"
                        ref="waterfallContainer"
                    >
                        <div
                            v-for="(row, rowIndex) in computedRows"
                            :key="rowIndex"
                            class="waterfall-row"
                        >
                            <div
                                v-for="(item, index) in row.items"
                                :key="index"
                                class="waterfall-item"
                                :style="getItemStyle(item)"
                            >
                                <el-card
                                    shadow="hover"
                                    :body-style="{ padding: '0px' }"
                                    :class="{ 'selected-card': selectedImage?.resourceId === item.image.resourceId }"
                                >
                                    <div
                                        class="image-wrapper"
                                        @click="handlePreview(item.image)"
                                        :style="{ height: `${item.displayHeight}px` }"
                                    >
                                        <el-image
                                            :src="item.image.thumbnailUrl"
                                            fit="contain"
                                            loading="lazy"
                                            class="waterfall-image"
                                            :style="getImageStyle(item)"
                                        >
                                            <template #error>
                                                <div class="image-error">
                                                    <img
                                                        src="/images/default-thumbnail.jpg"
                                                        style="width:100%;height:100%;object-fit:contain;background-color:#f5f5f5;"
                                                        alt=""
                                                    >
                                                </div>
                                            </template>
                                        </el-image>
                                    </div>
                                    <div class="image-info">
                                        <div class="image-title">{{ item.image.title || item.image.sourceFileName }}</div>
                                    </div>
                                </el-card>
                            </div>
                        </div>
                    </div>
                </el-main>
                <el-aside class="detail-aside">
                    <FileDetailView
                        v-if="selectedImage"
                        :file="selectedImage"
                        @download="handleDownload"
                        @preview-doc="handleShowDoc"
                    />
                    <el-empty v-else description="请选择文件查看详情" />
                </el-aside>
            </div>
        </template>
    </div>

    <PdfPreviewDialog
        v-model="pdfDialogVisible"
        :url="pdfPreviewUrl"
    />
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch, nextTick, computed } from "vue";
import { listCategoryFiles } from "@/apis/services";
import { useRoute } from "vue-router";
import { Resource } from "@/apis/interface";
import PdfPreviewDialog from "@/components/category/PdfPreviewDialog.vue";
import FileDetailView from "@/components/category/FileDetailView.vue";

const route = useRoute();
const cateId = route.params.cateId as string;
const files = ref<Resource[]>([]);
const selectedImage = ref<Resource | null>(null);
const loading = ref(true);
const currentPage = ref(1);
const pageSize = ref(500);
const total = ref(0);
const pdfDialogVisible = ref(false);
const pdfPreviewUrl = ref('');
const waterfallContainer = ref<HTMLElement | null>(null);
const containerWidth = ref(0);
const baseWidth = ref(200); // 竖幅图片初始宽度
const maxHeight = ref(200); // 最大高度

interface LayoutItem {
    image: Resource;
    displayWidth: number;
    displayHeight: number;
    aspectRatio: number;
}

interface ComputedRow {
    items: LayoutItem[];
}

// 计算最优的行布局
const computedRows = computed<ComputedRow[]>(() => {
    if (!files.value.length || !containerWidth.value) return [];

    const gap = 16;
    const rows: ComputedRow[] = [];
    let currentRow: LayoutItem[] = [];
    let currentRowWidth = 0;
    let i = 0;

    while (i < files.value.length) {
        const image = files.value[i];
        if (!image.width || !image.height) {
            i++;
            continue;
        }

        const aspectRatio = image.width / image.height;
        const isPortrait = aspectRatio < 1; // 竖幅图片

        // 计算初始显示尺寸
        let displayWidth = isPortrait ? baseWidth.value : maxHeight.value * aspectRatio;
        let displayHeight = isPortrait ? baseWidth.value / aspectRatio : maxHeight.value;

        // 限制最大高度
        if (displayHeight > maxHeight.value) {
            displayHeight = maxHeight.value;
            displayWidth = displayHeight * aspectRatio;
        }

        const itemWidth = displayWidth;
        const totalWidth = currentRowWidth + (currentRow.length > 0 ? gap : 0) + itemWidth;

        // 如果能放入当前行
        if (totalWidth <= containerWidth.value + 1) { // 加1px容差
            currentRow.push({
                image,
                displayWidth,
                displayHeight,
                aspectRatio
            });
            currentRowWidth = totalWidth;
            i++;
        }
        // 如果不能放入当前行
        else {
            // 调整当前行以完美填满容器
            if (currentRow.length > 0) {
                const adjustedRow = adjustRowToFit(currentRow, containerWidth.value);
                if (adjustedRow) {
                    rows.push({ items: adjustedRow });
                }
                currentRow = [];
                currentRowWidth = 0;
            } else {
                // 单个项目太宽，强制显示并缩小
                const adjustedWidth = containerWidth.value;
                const adjustedHeight = adjustedWidth / aspectRatio;
                currentRow.push({
                    image,
                    displayWidth: adjustedWidth,
                    displayHeight: adjustedHeight,
                    aspectRatio
                });
                rows.push({ items: [...currentRow] });
                currentRow = [];
                currentRowWidth = 0;
                i++;
            }
        }
    }

    // 添加最后一行
    if (currentRow.length > 0) {
        const adjustedRow = adjustRowToFit(currentRow, containerWidth.value);
        if (adjustedRow) {
            rows.push({ items: adjustedRow });
        }
    }

    return rows;
});

// 调整行内项目以精确填满指定宽度
const adjustRowToFit = (items: LayoutItem[], targetWidth: number): LayoutItem[] | null => {
    if (items.length === 0) return null;

    const gap = 16;
    const totalGaps = (items.length - 1) * gap;
    const totalContentWidth = targetWidth - totalGaps;

    // 计算当前总宽度和缩放比例
    const currentTotalWidth = items.reduce((sum, item) => sum + item.displayWidth, 0);
    const scale = totalContentWidth / currentTotalWidth;

    // 应用缩放比例
    return items.map(item => ({
        ...item,
        displayWidth: item.displayWidth * scale,
        displayHeight: item.displayHeight * scale
    }));
};

// 计算项目样式
const getItemStyle = (item: LayoutItem) => {
    return {
        width: `${item.displayWidth}px`,
        flexShrink: 0
    };
};

// 计算图片样式
const getImageStyle = (item: LayoutItem) => {
    return {
        width: `${item.displayWidth}px`,
        height: `${item.displayHeight}px`
    };
};

// 更新容器宽度
const updateContainerWidth = () => {
    if (waterfallContainer.value) {
        containerWidth.value = waterfallContainer.value.clientWidth;
    }
};

// 加载数据
onMounted(async () => {
    try {
        const data = await listCategoryFiles(cateId, currentPage.value, pageSize.value);
        files.value = data.list;
        total.value = data.total;
        currentPage.value = data.pageNum;

        nextTick(() => {
            updateContainerWidth();
            window.addEventListener('resize', updateContainerWidth);
        });
    } catch (error) {
        console.error("加载失败:", error);
    } finally {
        loading.value = false;
    }
});

// 监听数据变化重新计算布局
watch(files, () => {
    nextTick(updateContainerWidth);
});

const handlePreview = (image: Resource) => {
    selectedImage.value = image;
};

const handleShowDoc = (image: Resource) => {
    pdfPreviewUrl.value = image.previewUrl;
    pdfDialogVisible.value = true;
};

const handleDownload = (image: Resource) => {
    console.log('下载文件:', image.sourceFileName);
};

onBeforeUnmount(() => {
    window.removeEventListener('resize', updateContainerWidth);
});
</script>

<style scoped>
.loading {
    padding: 20px;
    text-align: center;
    color: #666;
}

.empty {
    padding: 100px;
    text-align: center;
    color: #666;
    font-size: 21px;
    width: 100%;
}

.category-file-container {
    display: flex;
    flex-direction: column;
    height: calc(100vh - 60px);
    overflow: hidden;
}

.main-layout {
    display: flex;
    height: 100%;
    overflow: hidden;
}

.image-container {
    flex: 1;
    padding: 15px;
    overflow-y: auto;
    height: 100%;
}

.waterfall-container {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.waterfall-row {
    display: flex;
    gap: 16px;
    width: 100%;
    justify-content: flex-start;
}

.waterfall-item {
    flex-shrink: 0;
    transition: all 0.3s ease;
}

.el-card {
    width: 100%;
    height: 100%;
    transition: all 0.3s ease;
    border-radius: 5px;
    overflow: hidden;
    border: 1px solid #ebeef5;
}

.image-wrapper {
    width: 100%;
    height: 100%;
    overflow: hidden;
    border-radius: 4px 4px 0 0;
    background-color: #f5f5f5;
    position: relative;
    cursor: pointer;
    display: flex;
    justify-content: center;
    align-items: center;
}

.waterfall-image {
    object-fit: contain;
    transition: transform 0.3s ease;
}

.waterfall-image:hover {
    transform: scale(1.05);
}

.image-error {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: #f5f5f5;
}

.image-info {
    text-align: center;
    font-size: 14px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    background-color: #f1f1f1;
    padding: 12px;
}

.selected-card {
    border: 1px solid #409EFF !important;
}

.selected-card:hover {
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3) !important;
}

.detail-aside {
    width: 280px;
    background-color: #fff;
    border-left: 1px solid #e6e6e6;
    overflow-y: auto;
    height: 100%;
    flex-shrink: 0;
}

/* 响应式调整 */
@media (max-width: 768px) {
    .detail-aside {
        display: none;
    }

    .waterfall-row {
        flex-wrap: wrap;
    }

    .waterfall-item {
        width: calc(50% - 8px) !important;
    }

    .baseWidth, .maxHeight {
        width: auto !important;
        height: 180px !important;
    }
}

@media (max-width: 480px) {
    .waterfall-item {
        width: 100% !important;
    }

    .baseWidth, .maxHeight {
        height: 150px !important;
    }
}
</style>