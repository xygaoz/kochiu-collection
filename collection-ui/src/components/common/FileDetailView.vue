<template>
    <div class="file-detail">
        <div class="preview-container">
            <!-- 图片预览 -->
            <el-image
                v-if="isImageType"
                :src="file.resourceUrl"
                :preview-src-list="[file.resourceUrl]"
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
            <div v-else-if="isVideoType" class="video-preview-container">
                <VideoPlayer
                    ref="videoPlayer"
                    :src="videoFileUrl"
                    :poster="videoThumbnailUrl"
                    :mimeType="videoMimeType"
                    @play="handleVideoPlay"
                    @pause="handleVideoPause"
                />
            </div>

            <!-- 音频预览 -->
            <div v-else-if="isAudioType" class="audio-preview-container">
                <AudioPlayer
                    :src="audioFileUrl"
                    :autoplay="false"
                    :loop="false"
                    @error="handleAudioError"
                />
            </div>

            <!-- 文档预览 -->
            <div v-else-if="isOfficeType || isPdfType" class="office-preview">
                <el-image
                    :src="file.thumbnailUrl"
                    fit="contain"
                    class="preview-content"
                    @click="emit('preview-doc', file)"
                >
                    <template #error>
                        <el-icon><Document /></el-icon>
                        <span>点击预览按钮查看文档</span>
                        <el-button
                            type="primary"
                            size="small"
                            @click="emit('preview-doc', file)"
                        >
                            预览文档
                        </el-button>
                    </template>
                </el-image>
            </div>

            <!-- 其他文件 -->
            <div v-else class="unsupported-file">
                <el-image
                    :src="file.thumbnailUrl"
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

        <div class="detail-content">
            <div class="detail-row">
                <div class="detail-label">文件标题</div>
                <div class="detail-value">
                    <el-input
                        v-if="editingField === 'title'"
                        ref="titleInput"
                        v-model="localFile.title"
                        size="small"
                        @blur="saveField('title')"
                        @keyup.enter="saveField('title')"
                        @keyup.esc="cancelEditing"
                        @click.stop
                        :disabled="saving"
                        autofocus
                    >
                        <template #suffix>
                            <el-icon v-if="saving" class="is-loading">
                                <Loading />
                            </el-icon>
                        </template>
                    </el-input>
                    <div v-else @click.stop="startEditing('title')" class="editable-field" :class="{ 'empty-value': !localFile.title }">
                        {{ localFile.title || '无标题' }}
                    </div>
                </div>
            </div>
            <div class="detail-row">
                <div class="detail-label">文件描述</div>
                <div class="detail-value">
                    <el-input
                        v-if="editingField === 'description'"
                        ref="descInput"
                        v-model="localFile.description"
                        type="textarea"
                        :rows="2"
                        size="small"
                        @blur="saveField('description')"
                        @keyup.enter="saveField('description')"
                        @keyup.esc="cancelEditing"
                        @click.stop
                        :disabled="saving"
                        autofocus
                    >
                        <template #suffix>
                            <el-icon v-if="saving" class="is-loading">
                                <Loading />
                            </el-icon>
                        </template>
                    </el-input>
                    <div v-else @click.stop="startEditing('description')" class="editable-field" :class="{ 'empty-value': !localFile.description }">
                        {{ localFile.description || '无描述' }}
                    </div>
                </div>
            </div>
            <div class="detail-row" v-if="props.dataType !== 'public'">
                <div class="detail-label">分类</div>
                <div class="detail-value">{{ file.cateName }}</div>
            </div>
            <div class="detail-row" v-if="props.dataType !== 'public'">
                <div class="detail-label">目录路径</div>
                <div class="detail-value" style="margin: 3px 0 0 0;">{{ file.cataPath }}</div>
            </div>
            <div class="detail-row">
                <div class="detail-label">文件名</div>
                <div class="detail-value filename">
                    <span :title="file.sourceFileName">{{ smartTruncateMiddle(file.sourceFileName) }}</span>
                </div>
            </div>
            <div class="detail-row">
                <div class="detail-label">文件类型</div>
                <div class="detail-value">{{ file.fileType }}</div>
            </div>
            <div class="detail-row" v-if="file.resolutionRatio">
                <div class="detail-label">分辨率</div>
                <div class="detail-value">{{ file.resolutionRatio }}</div>
            </div>
            <div class="detail-row">
                <div class="detail-label">文件大小</div>
                <div class="detail-value">{{ formatSize(file.size) }}</div>
            </div>
            <div class="detail-row" v-if="props.dataType != 'public'">
                <div class="detail-label">是否公开</div>
                <div class="detail-value">
                    <el-switch
                        v-model="localFile.share"
                        :disabled="saving || isReadOnly"
                        @change="handlePublicChange"
                    />
                </div>
            </div>
            <div class="detail-row">
                <div class="detail-label">创建时间</div>
                <div class="detail-value">{{ formatTime(file.createTime) }}</div>
            </div>
            <div class="detail-row" v-if="file.updateTime">
                <div class="detail-label">修改时间</div>
                <div class="detail-value">{{ formatTime(file.updateTime) }}</div>
            </div>
            <div class="detail-row">
                <div class="detail-label">评分</div>
                <div class="detail-value">
                    <el-rate
                        v-model="localFile.star"
                        @change="handleRateChange"
                        :disabled="saving || isReadOnly"
                        show-score
                        text-color="#ff9900"
                        score-template="{value} 分"
                    >
                        <template #suffix>
                            <el-icon v-if="saving" class="is-loading">
                                <Loading />
                            </el-icon>
                        </template>
                    </el-rate>
                </div>
            </div>

            <div class="detail-row" v-if="props.dataType !== 'public'">
                <div class="tags">
                    <div class="tag-label">标签</div>
                    <el-tag
                        v-for="tag in localFile.tags"
                        :key="tag.tagId"
                        :closable="!isReadOnly"
                        :disable-transitions="false"
                        @close="handleTagClose(tag)"
                        @click.stop
                    >
                        {{ tag.tagName }}
                    </el-tag>
                    <el-input
                        v-if="tagInputVisible"
                        ref="tagInputRef"
                        v-model="tagInputValue"
                        class="tag-input"
                        size="small"
                        @keyup.enter="handleTagInputConfirm"
                        @blur="handleTagInputConfirm"
                        @click.stop
                    />
                    <el-button
                        v-else-if="!isReadOnly"
                        class="button-new-tag"
                        size="small"
                        @click="showTagInput"
                        @click.stop
                    >
                        + 新标签
                    </el-button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch, defineProps, defineEmits } from "vue";
import { Document, Picture, Loading } from "@element-plus/icons-vue";
import { ElMessage, ElInput } from "element-plus";
import { addResourceTag, removeResourceTag, setResourcePublic, updateResource } from "@/apis/resource-api";
import type { Resource, Tag } from '@/apis/interface';
import AudioPlayer from "@/components/common/AudioPlayer.vue";
import VideoPlayer from '@/components/common/VideoPlayer.vue';
import emitter from "@/utils/event-bus";

type EditableField = 'title' | 'description' | 'tags';

const props = defineProps<{
    file: Resource;
    dataType?: string;
}>();

const emit = defineEmits<{
    (e: 'preview-doc', file: Resource): void;
    (e: 'update-file', file: Resource): void;
}>();

// 状态定义
const localFile = ref<Resource>({...props.file});
const editingField = ref<EditableField | ''>('');
const originalValue = ref<string | boolean | number | Tag[] | undefined>();
const saving = ref<boolean>(false);
const titleInput = ref<InstanceType<typeof ElInput> | null>(null);
const descInput = ref<InstanceType<typeof ElInput> | null>(null);
const tagInputVisible = ref(false);
const tagInputValue = ref('');
const tagInputRef = ref<InstanceType<typeof ElInput> | null>(null);

// 计算属性
const isImageType = computed<boolean>(() => props.file.typeName === 'image');
const isVideoType = computed<boolean>(() => props.file.typeName === 'video');
const isAudioType = computed<boolean>(() => props.file.typeName === 'audio');
const isOfficeType = computed<boolean>(() => {
    const officeTypes = [
        'application/msword',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        'application/vnd.ms-excel',
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        'application/vnd.ms-powerpoint',
        'application/vnd.openxmlformats-officedocument.presentationml.presentation'
    ];
    return officeTypes.includes(props.file.mimeType) && !!props.file.previewUrl;
});
const isPdfType = computed<boolean>(() => props.file.fileType === 'application/pdf' && !!props.file.previewUrl);
const isReadOnly = computed(() => props.dataType === 'recycle' || props.dataType === 'public');
const videoPlayer = ref<InstanceType<typeof VideoPlayer> | null>(null);
const isVideoPlaying = ref(false);

const handleVideoPlay = () => {
    isVideoPlaying.value = true;
};

const handleVideoPause = () => {
    isVideoPlaying.value = false;
};

const audioFileUrl = computed(() => {
    return props.file.resourceUrl.startsWith('http')
        ? props.file.resourceUrl
        : `${window.location.origin}${props.file.resourceUrl}`;
});

const handleAudioError = (error: Event) => {
    console.error('音频播放失败:', error);
    ElMessage.error('音频播放失败，请检查文件格式或网络连接');
};

// 视频文件URL（处理相对路径）
const videoFileUrl = computed(() => {
    if (!props.file.resourceUrl) return '';
    return props.file.resourceUrl.startsWith('http')
        ? props.file.resourceUrl
        : `${window.location.origin}${props.file.resourceUrl}`;
});

// 视频缩略图URL（处理相对路径）
const videoThumbnailUrl = computed(() => {
    if (!props.file.thumbnailUrl) return undefined;
    return props.file.thumbnailUrl.startsWith('http')
        ? props.file.thumbnailUrl
        : `${window.location.origin}${props.file.thumbnailUrl}`;
});

// 视频MIME类型
const videoMimeType = computed(() => {
    // 优先使用后端返回的mimeType
    if (props.file.mimeType && props.file.mimeType.includes('/')) {
        return props.file.mimeType;
    }

    // 根据文件扩展名推断
    const ext = props.file.resourceUrl?.split('.').pop()?.toLowerCase();
    switch(ext) {
        case 'mp4': return 'video/mp4';
        case 'webm': return 'video/webm';
        case 'ogg': return 'video/ogg';
        case 'mov': return 'video/quicktime';
        case 'avi': return 'video/x-msvideo';
        default: return 'video/*';
    }
});

const formatSize = (bytes: number): string => {
    if (!bytes) return '未知';
    const units = ['B', 'KB', 'MB', 'GB'];
    let size = bytes;
    let unitIndex = 0;
    while (size >= 1024 && unitIndex < units.length - 1) {
        size /= 1024;
        unitIndex++;
    }
    return `${size.toFixed(2)} ${units[unitIndex]}`;
};

const formatTime = (timeStr: string): string => {
    return timeStr ? new Date(timeStr).toLocaleString() : '未知';
};

const startEditing = async (field: EditableField): Promise<void> => {
    if(isReadOnly.value){
        return
    }
    editingField.value = field;
    await nextTick();
    const inputRef = field === 'title' ? titleInput.value : descInput.value;
    if (inputRef) {
        inputRef.focus();
    }
};

const handleRateChange = async (value: number): Promise<void> => {
    saving.value = true;
    try {
        await updateResource(localFile.value.resourceId, {
            star: value
        });

        const updatedFile: Resource = {
            ...props.file,
            star: value
        };

        localFile.value = updatedFile;
        emit('update-file', updatedFile);
    } catch (error) {
        localFile.value.star = props.file.star;
        ElMessage.error(`评分更新失败: ${error instanceof Error ? error.message : String(error)}`);
    } finally {
        saving.value = false;
    }
};

const saveField = async (field: EditableField): Promise<void> => {
    if (!editingField.value) return;

    const currentValue = field === 'tags'
        ? localFile.value.tags
        : localFile.value[field];

    if (JSON.stringify(currentValue) === JSON.stringify(originalValue.value)) {
        editingField.value = '';
        return;
    }

    saving.value = true;
    try {
        await updateResource(localFile.value.resourceId, {
            [field]: currentValue
        });

        const updatedFile: Resource = {
            ...props.file,
            [field]: currentValue
        };

        emit('update-file', updatedFile);
        localFile.value = updatedFile;
    } catch (error) {
        if (field === 'tags') {
            localFile.value.tags = originalValue.value as Tag[] || [];
        } else {
            localFile.value[field] = originalValue.value as string;
        }
        ElMessage.error(`更新失败: ${error instanceof Error ? error.message : String(error)}`);
    } finally {
        saving.value = false;
        editingField.value = '';
    }
};

const cancelEditing = (): void => {
    if (editingField.value) {
        if (editingField.value === 'tags') {
            localFile.value.tags = originalValue.value as Tag[] || [];
        } else {
            localFile.value[editingField.value] = originalValue.value as string;
        }
        editingField.value = '';
    }
};

const handleClickOutside = (event: MouseEvent): void => {
    const target = event.target as HTMLElement;
    const isClickInsideEditor = target.closest('.el-input') || target.closest('.editable-field');

    if (editingField.value && !isClickInsideEditor) {
        saveField(editingField.value);
    }
};

const showTagInput = () => {
    tagInputVisible.value = true;
    nextTick(() => {
        tagInputRef.value?.focus();
    });
};

const handleTagInputConfirm = async () => {
    if (tagInputValue.value) {
        try {
            saving.value = true;
            const newTag = await addResourceTag(localFile.value.resourceId, {
                tagName: tagInputValue.value
            });

            const updatedTags = [...localFile.value.tags, newTag];
            localFile.value.tags = updatedTags;

            const updatedFile = {
                ...props.file,
                tags: updatedTags
            };
            emit('update-file', updatedFile);
            //触发数据刷新事件
            emitter.emit('refresh-tags')

        } catch (error) {
            ElMessage.error(`添加标签失败: ${error instanceof Error ? error.message : String(error)}`);
        } finally {
            saving.value = false;
            tagInputVisible.value = false;
            tagInputValue.value = '';
        }
    } else {
        tagInputVisible.value = false;
    }
};

const handleTagClose = async (tag: Tag) => {
    if (isReadOnly.value) return;

    try {
        saving.value = true;
        await removeResourceTag(localFile.value.resourceId, {
            tagId: tag.tagId
        });

        const updatedTags = localFile.value.tags.filter(t => t.tagId !== tag.tagId);
        localFile.value.tags = updatedTags;

        const updatedFile = {
            ...props.file,
            tags: updatedTags
        };
        emit('update-file', updatedFile);
        //触发数据刷新事件
        emitter.emit('refresh-tags')
    } catch (error) {
        ElMessage.error(`移除标签失败: ${error instanceof Error ? error.message : String(error)}`);
    } finally {
        saving.value = false;
    }
};

const handlePublicChange = async (share: boolean) => {
    saving.value = true;
    try {
        await setResourcePublic(localFile.value.resourceId, share);

        const updatedFile: Resource = {
            ...props.file,
            share: share
        };

        emit('update-file', updatedFile);
        localFile.value = updatedFile;
    } catch (error) {
        localFile.value["share"] = originalValue.value as boolean;
        ElMessage.error(`更新失败: ${error instanceof Error ? error.message : String(error)}`);
    } finally {
        saving.value = false;
        editingField.value = '';
    }
};

// 计算字符串的显示宽度（中文算2，英文算1）
const getDisplayWidth = (str: string): number => {
    return [...str].reduce((width, char) => {
        return width + (/[\u4e00-\u9fa5]/.test(char) ? 2 : 1);
    }, 0);
};

const smartTruncateMiddle = (filename: string): string => {
    if (!filename) return '';

    const MAX_DISPLAY_WIDTH = 20; // 显示区域最大宽度

    // 如果文件名本身很短，直接返回
    if (getDisplayWidth(filename) <= MAX_DISPLAY_WIDTH) return filename;

    // 分离文件名和扩展名
    const lastDotIndex = filename.lastIndexOf('.');
    let name = filename;
    let ext = '';

    if (lastDotIndex > 0) {
        name = filename.substring(0, lastDotIndex);
        ext = filename.substring(lastDotIndex);
    }

    const extWidth = getDisplayWidth(ext);
    const ellipsisWidth = 2; // "……"
    const availableWidth = MAX_DISPLAY_WIDTH - extWidth - ellipsisWidth;

    // 1. 尝试在中文短语后截断
    const chinesePhraseEnd = name.search(/[\u4e00-\u9fa5][^\u4e00-\u9fa5]/);
    if (chinesePhraseEnd > 0) {
        const frontPart = name.substring(0, chinesePhraseEnd + 1);
        if (getDisplayWidth(frontPart) <= availableWidth) {
            const remainingWidth = availableWidth - getDisplayWidth(frontPart);
            let endPart = '';
            // 从后往前取剩余宽度
            for (let i = name.length - 1; i >= 0; i--) {
                const char = name[i];
                const charWidth = /[\u4e00-\u9fa5]/.test(char) ? 2 : 1;
                if (getDisplayWidth(endPart) + charWidth <= remainingWidth) {
                    endPart = char + endPart;
                } else break;
            }
            return `${frontPart}……${endPart}${ext}`;
        }
    }

    // 2. 保底方案：精确宽度截断
    let frontPart = '';
    let frontWidth = 0;
    for (const char of name) {
        const charWidth = /[\u4e00-\u9fa5]/.test(char) ? 2 : 1;
        if (frontWidth + charWidth <= availableWidth) {
            frontPart += char;
            frontWidth += charWidth;
        } else break;
    }
    return `${frontPart}……${ext}`;
};

// 生命周期
onMounted(() => {
    document.addEventListener('click', handleClickOutside);
});

onUnmounted(() => {
    document.removeEventListener('click', handleClickOutside);
});

// 监听props变化
watch(() => props.file, (newVal: Resource) => {
    if (!editingField.value) {
        localFile.value = {...newVal};
    }
}, { deep: true });
</script>

<style scoped>
.file-detail {
    width: 100%;
    height: 100%;
    background-color: var(--el-bg-color-view);
}

.preview-container {
    height: 300px;
    padding: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-bottom: 1px solid var(--el-border-color-light);
    overflow: hidden;
}

.preview-content {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
    display: block;
    margin: 0 auto;
    cursor: pointer;
}

/* Specific styles for el-image elements */
.preview-container :deep(.el-image) {
    display: flex !important;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
}

.preview-container :deep(.el-image__inner) {
    max-width: 100%;
    max-height: 300px;
    object-fit: contain;
    width: auto;
    height: auto;
}

/* For portrait images */
.preview-container :deep(.el-image__inner[style*="height: 100%"]) {
    height: auto !important;
    max-height: 300px;
}

/* For landscape images */
.preview-container :deep(.el-image__inner[style*="width: 100%"]) {
    width: 100% !important;
    height: auto !important;
}

.preview-error,
.unsupported-file {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: var(--el-text-color-secondary);
    font-size: 14px;
    width: 100%;
    height: 100%;
}

.audio-preview-container {
    width: 100%;
    height: 100%;
    padding: 0 20px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.preview-error .el-icon,
.unsupported-file .el-icon {
    font-size: 48px;
    margin-bottom: 10px;
}

.detail-content {
    padding: 15px 16px 10px 16px;
}

.detail-row {
    display: flex;
    margin-bottom: 12px;
    font-size: 12px;
}

.detail-label {
    width: 80px;
    color: var(--el-text-color-secondary);
    padding-right: 12px;
}

.detail-value {
    flex: 1;
}

:deep(.el-rate){
    height: 21px!important;
    --el-rate-disabled-void-color: var(--el-rate-disabled-color)
}

/* 标签文字 */
.tag-label {
    color: var(--el-item-text-color);
    padding-right: 5px;
    height: 32px;
    line-height: 32px;
    flex-shrink: 0;
}

.tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: center;
}

.tag-input {
    width: 80px;
}

.button-new-tag {
    height: 24px;
    line-height: 22px;
    padding-top: 0;
    padding-bottom: 0;
}

.el-tag {
    cursor: default;
}

.empty-value {
    color: var(--el-text-color-placeholder);
    font-style: italic;
}

.editable-field {
    cursor: pointer;
    border-radius: 4px;
    transition: all 0.2s;
    display: flex;
    align-items: center;
}

.editable-field:hover {
    background-color: var(--el-fill-color-light);
}

.is-loading {
    animation: rotating 2s linear infinite;
}

@keyframes rotating {
    from {
        transform: rotate(0deg);
    }
    to {
        transform: rotate(360deg);
    }
}

.video-preview-container {
    position: relative;
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
}

:deep(.el-switch){
    height: 20px;
}

.filename {
    display: inline-block;
    max-width: 280px;  /* 根据实际UI调整 */
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    vertical-align: middle;
    font-family: "PingFang SC", "Microsoft YaHei", sans-serif; /* 中文字体优化 */
}
</style>
