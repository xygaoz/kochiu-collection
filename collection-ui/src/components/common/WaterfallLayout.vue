<template>
    <!-- 模板部分保持不变 -->
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
                    :class="{ 'selected-card': selectedImage?.resourceId === item.image.resourceId
                         || isMultipleSelect(item.image),
                         'show-checkbox': hasAnySelection || isMultipleSelect(item.image)}"
                >
                    <div class="image-select">
                        <el-checkbox
                            :key="'checkbox-' + item.image.resourceId + forceRender"
                            :checked="isMultipleSelect(item.image)"
                            @change="(val: boolean) => handleMultipleSelect(val, item.image)"
                        />
                    </div>
                    <div class="resource-wrapper"
                         @click="handlePreview(item.image)"
                    >
                        <div
                            class="image-wrapper"
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
                                        <img class="default-thumbnail"
                                            :src="getDefaultThumbnail(item.image)"
                                            alt=""
                                        >
                                    </div>
                                </template>
                            </el-image>
                            <div v-if="isVideoOrAudio(item.image)" class="play-icon-overlay">
                                <el-icon class="play-icon">
                                    <VideoPlay v-if="isVideoType(item.image)" />
                                    <Headset v-else />
                                </el-icon>
                            </div>
                        </div>
                        <div class="image-info">
                            <div class="image-title">{{ item.image.title || item.image.sourceFileName }}</div>
                            <div class="image-actions">
                                <el-icon class="action-icon" @click.stop="handleDownload(item.image)" title="下载">
                                    <Download />
                                </el-icon>
                                <el-icon class="action-icon" @click.stop="handleToRecycle(item.image)" title="删除">
                                    <Delete />
                                </el-icon>
                                <el-icon class="action-icon"
                                         v-if="props.dataType !== 'recycle'"
                                         @click.stop="handleMove(item.image)" title="移动">
                                    <Connection />
                                </el-icon>
                                <i v-if="props.dataType === 'recycle'"
                                   class="action-icon iconfont icon-col-huanyuan"
                                   title="还原"
                                   @click.stop="handleRestore(item.image)"
                                />
                            </div>
                        </div>
                    </div>
                </el-card>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import {
    computed,
    defineEmits,
    defineProps,
    nextTick,
    onBeforeUnmount,
    onMounted,
    ref,
    defineExpose,
    watch
} from "vue";
import type { Resource } from "@/apis/interface";
import { Connection, Delete, Download, Headset, VideoPlay } from "@element-plus/icons-vue";
import { downloadFile } from "@/apis/utils";
import imageThumbnail from '@/assets/imgs/type/image.png';
import videoThumbnail from '@/assets/imgs/type/video.png';
import documentThumbnail from '@/assets/imgs/type/document.png';
import audioThumbnail from '@/assets/imgs/type/audio.png';
import unknownThumbnail from '@/assets/imgs/type/unknown.png';

// 使用 TypeScript 类型定义 props
const props = defineProps<{
    files: Resource[];
    dataType: string;
}>();

// 定义并实际使用 emit
const emit = defineEmits<{
    (e: 'preview', image: Resource): void;
    (e: 'multiple-selected', images: Resource[]): void;
    (e: 'move-to-category', images: Resource[]): void;
    (e: 'move-to-recycle', images: Resource[], deleted: boolean): void;
    (e: 'restore', images: Resource[]): void;
}>();

const waterfallContainer = ref<HTMLElement | null>(null);
const containerWidth = ref(0);
const baseWidth = ref(200);
const maxHeight = ref(200);
const selectedImage = ref<Resource>();
const multipleSelected = ref<Resource[]>([]);
const forceRender = ref(0);

interface LayoutItem {
    image: Resource;
    displayWidth: number;
    displayHeight: number;
    aspectRatio: number;
}

interface ComputedRow {
    items: LayoutItem[];
}

// 判断是否是视频或音频
const isVideoOrAudio = (file: Resource) => {
    return isVideoType(file) || isAudioType(file);
};

const isVideoType = (file: Resource) => {
    return file.typeName === 'video' ||
        (file.mimeType && file.mimeType.startsWith('video/'));
};

const isAudioType = (file: Resource) => {
    return file.typeName === 'audio' ||
        (file.mimeType && file.mimeType.startsWith('audio/'));
};
const isImageType = (file: Resource) => {
    return file.typeName === 'image' ||
        (file.mimeType && file.mimeType.startsWith('image/'));
};
const isDocumentType = (file: Resource) => {
    const officeTypes = [
        'application/msword',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        'application/vnd.ms-excel',
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        'application/vnd.ms-powerpoint',
        'application/vnd.openxmlformats-officedocument.presentationml.presentation',
        'application/pdf',
        'text/plain'
    ];
    return file.typeName === 'document' ||
        (file.mimeType && officeTypes.includes(file.mimeType));
};

const getDefaultThumbnail = (file: Resource) => {
    if (isVideoType(file)) {
        return videoThumbnail;
    } else if (isAudioType(file)) {
        return audioThumbnail;
    } else if (isImageType(file)) {
        return imageThumbnail;
    } else if (isDocumentType(file)) {
        return documentThumbnail;
    } else {
        return unknownThumbnail;
    }
};

const handlePreview = async (image: Resource) => {
    forceRender.value++;

    // 清空多选数组
    multipleSelected.value = [];

    // 设置当前选中项
    selectedImage.value = image;

    // 确保UI更新
    await nextTick();

    emit('preview', image);

    //使用 multipleSelected.value 传递实际值
    emit('multiple-selected', multipleSelected.value);
};

// 计算最优的行布局
const computedRows = computed<ComputedRow[]>(() => {
    if (!props.files.length || !containerWidth.value) return [];

    const gap = 10;
    const rows: ComputedRow[] = [];
    let currentRow: LayoutItem[] = [];
    let currentRowWidth = 0;
    let i = 0;

    while (i < props.files.length) {
        const image = props.files[i];
        if (!image.width || !image.height) {
            i++;
            continue;
        }

        const aspectRatio = image.width / image.height;
        const isPortrait = aspectRatio < 1;

        // 计算初始显示尺寸（优先使用baseWidth）
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
        if (totalWidth <= containerWidth.value + 1) {
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
            // 对于非最后一行，调整行内项目以填满宽度
            if (currentRow.length > 0) {
                const adjustedRow = adjustRowToFit(currentRow, containerWidth.value);
                if (adjustedRow) {
                    rows.push({ items: adjustedRow });
                }
                currentRow = [];
                currentRowWidth = 0;
            } else {
                // 单个项目太宽的情况
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

    // 添加最后一行（不调整宽度，保持原始尺寸）
    if (currentRow.length > 0) {
        rows.push({ items: [...currentRow] });
    }

    return rows;
});

// 调整行内项目以精确填满指定宽度（仅用于非最后一行）
const adjustRowToFit = (items: LayoutItem[], targetWidth: number): LayoutItem[] | null => {
    if (items.length === 0) return null;

    const gap = 10;
    const totalGaps = (items.length - 1) * gap;
    const totalContentWidth = targetWidth - totalGaps;

    const currentTotalWidth = items.reduce((sum, item) => sum + item.displayWidth, 0);
    const scale = totalContentWidth / currentTotalWidth;

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

// 添加计算属性
const hasAnySelection = computed(() => {
    return multipleSelected.value.length > 0;
});

//判断是否多选
const isMultipleSelect = (image: Resource) => {
    return multipleSelected.value.some(item => item.resourceId === image.resourceId);
}

const handleMultipleSelect = (checked: boolean, resource: Resource) => {
    if (checked) {
        if (!isMultipleSelect(resource)) {
            multipleSelected.value = [...multipleSelected.value, resource];
        }
    } else {
        multipleSelected.value = multipleSelected.value.filter(
            item => item.resourceId !== resource.resourceId
        );
    }

    //使用 multipleSelected.value 传递实际值
    emit('multiple-selected', multipleSelected.value);
}

const clearSelection = async () => {
    forceRender.value++;
    // 清空多选数组
    multipleSelected.value = [];
    // 确保UI更新
    await nextTick();
}

const selectAll = async () => {
    multipleSelected.value = [...props.files];
    forceRender.value++;
    await nextTick();
    emit('multiple-selected', multipleSelected.value);
}

const handleDownload = (image: Resource) => {
    downloadFile(image.resourceUrl, {})
};

const handleToRecycle = (image: Resource) => {
    emit('move-to-recycle', [image], props.dataType === 'recycle');
};

const handleMove = (image: Resource) => {
    emit('move-to-category', [image]);
};

const handleRestore = (image: Resource) => {
    emit('restore', [image])
};

defineExpose({
    clearSelection, selectAll
})

// 添加对 files 的监听
watch(() => props.files, (newFiles, oldFiles) => {
    // 当 files 发生变化时清空多选
    if (newFiles !== oldFiles) {
        clearSelection();
    }
});

onMounted(() => {
    updateContainerWidth();
    window.addEventListener('resize', updateContainerWidth);
});

onBeforeUnmount(() => {
    window.removeEventListener('resize', updateContainerWidth);
});
</script>

<style scoped>
.waterfall-container {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.waterfall-row {
    display: flex;
    gap: 10px;
    width: 100%;
    justify-content: flex-start;
}

.waterfall-item {
    flex-shrink: 0;
    transition: all 0.3s ease;
}

.waterfall-image {
    object-fit: contain;
    transition: transform 0.3s ease;
    border-bottom: 1px solid var(--el-border-color-light);
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
    background-color: var(--el-bg-color);
}

.el-card {
    width: 100%;
    height: 100%;
    transition: all 0.3s ease;
    border-radius: 5px;
    overflow: hidden;
    border: 1px solid var(--el-card-border-color);
    background-color: var(--el-bg-color);
    display: flex;
    flex-direction: column;
    position: relative;
}

.el-card, .el-card * {
    box-sizing: border-box;
}

.resource-wrapper{
    cursor: pointer;
    position: relative;
}

.image-wrapper {
    width: 100%;
    flex-grow: 1; /* 确保图片区域占据剩余空间 */
    overflow: hidden;
    border-radius: 4px 4px 0 0;
    background-color: var(--el-color-primary-light-9);
    position: relative;
    display: flex;
    justify-content: center;
    align-items: center;
}

.selected-card {
    border: 1px solid var(--el-color-primary) !important;
}

.selected-card:hover {
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3) !important;
}

.image-select{
    position: absolute;
    top: 2px;
    right: 10px;
    opacity: 0;
    transition: opacity 0.2s;
}

.el-card:hover .image-select,
.el-card.show-checkbox .image-select {
    opacity: 1;
}

.image-info {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px;
    background: linear-gradient(
        to top,
        rgba(0, 0, 0, 0.3),
        rgba(0, 0, 0, 0.1) 70%,
        transparent
    );
    color: var(--el-item-text-color);
    backdrop-filter: blur(2px); /* 可选：添加毛玻璃效果 */
}

/* 暗黑主题微调 */
html.dark .image-info {
    background: linear-gradient(
        to top,
        rgba(0, 0, 0, 0.6),  /* 暗黑模式下减少黑色浓度 */
        rgba(0, 0, 0, 0.2) 70%,
        transparent
    );
}

.image-title {
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    font-size: 12px;
}

.image-actions {
    flex: 1;
    display: flex;
    justify-content: flex-end;
    gap: 8px;
}

.action-icon {
    cursor: pointer;
    font-size: 14px;
    transition: all 0.2s;
    opacity: 0.7;
}

.action-icon:hover {
    opacity: 1;
    color: var(--el-color-primary);
}

.default-thumbnail{
    width:100%;
    height:100%;
    object-fit:contain;
    background-color: var(--el-color-primary-light-9);
}

.play-icon-overlay {
    position: absolute;
    display: flex;
    justify-content: center;
    align-items: center;
    border-radius: 22px;
    background-color: rgba(0, 0, 0, 0.3);
    opacity: 0.5;
    transition: opacity 0.3s ease;
}

.resource-wrapper:hover .play-icon-overlay {
    opacity: 1;
}

.play-icon {
    font-size: 36px;
    color: white;
    filter: drop-shadow(0 0 4px rgba(0, 0, 0, 0.5));
}

/* 响应式调整 */
@media (max-width: 768px) {
    .waterfall-row {
        flex-wrap: wrap;
    }

    .waterfall-item {
        width: calc(50% - 8px) !important;
    }
}

@media (max-width: 480px) {
    .waterfall-item {
        width: 100% !important;
    }
}
</style>