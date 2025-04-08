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
            <video
                v-else-if="isVideoType"
                controls
                class="preview-content"
                :poster="file.thumbnailUrl"
                @click.stop="handlePlayVideo"
            >
                <source :src="file.resourceUrl" :type="file.fileType">
                您的浏览器不支持视频播放
            </video>

            <!-- 音频预览 -->
            <audio
                v-else-if="isAudioType"
                controls
                class="preview-content audio-preview"
            >
                <source :src="file.resourceUrl" :type="file.fileType">
                您的浏览器不支持音频播放
            </audio>

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

        <div class="detail-header">
            <div>文件详情</div>
            <el-icon class="download-file"
                     @click="emit('download', file)"
            >
                <Download/>
            </el-icon>
        </div>

        <el-divider />

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
            <div class="detail-row">
                <div class="detail-label">文件名</div>
                <div class="detail-value">{{ file.sourceFileName }}</div>
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
                        :model-value="file.star"
                        disabled
                        show-score
                        text-color="#ff9900"
                        score-template="{value} 分"
                    />
                </div>
            </div>

            <div class="tag-section" v-if="file.tags?.length">
                <el-divider>标签</el-divider>
                <div class="tags">
                    <el-tag
                        v-for="tag in file.tags"
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

        <!-- 视频弹窗 -->
        <el-dialog
            v-model="localVideoDialogVisible"
            title="视频播放"
            width="70%"
            top="5vh"
            destroy-on-close
        >
            <video
                controls
                autoplay
                style="width: 100%"
                :src="localCurrentVideoUrl"
            ></video>
        </el-dialog>
    </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { Document, Download, Picture, Loading } from "@element-plus/icons-vue";
import { updateResource } from "@/apis/services";
import { ElMessage } from "element-plus";
import { watch } from "vue";

export default {
    components: { Download, Document, Picture, Loading },
    props: {
        file: {
            type: Object,
            required: true
        }
    },
    emits: ['download', 'preview-doc', 'update:file'],
    setup(props, { emit }) {
        // 状态定义
        const localCurrentVideoUrl = ref('');
        const localVideoDialogVisible = ref(false);
        const localFile = ref({...props.file});
        const editingField = ref(null);
        const originalValue = ref('');
        const saving = ref(false);
        const titleInput = ref(null);
        const descInput = ref(null);

        // 计算属性
        const isImageType = computed(() => props.file.typeName === 'image');
        const isVideoType = computed(() => props.file.typeName === 'video');
        const isAudioType = computed(() => props.file.typeName === 'audio');
        const isOfficeType = computed(() => {
            const officeTypes = [
                'application/msword',
                'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
                'application/vnd.ms-excel',
                'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
                'application/vnd.ms-powerpoint',
                'application/vnd.openxmlformats-officedocument.presentationml.presentation'
            ];
            return officeTypes.includes(props.file.mimeType) && props.file.previewUrl;
        });
        const isPdfType = computed(() => props.file.fileType === 'application/pdf' && props.file.previewUrl);

        // 方法定义
        const handlePlayVideo = () => {
            localCurrentVideoUrl.value = props.file.resourceUrl;
            localVideoDialogVisible.value = true;
        };

        const formatSize = (bytes) => {
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

        const formatTime = (timeStr) => {
            return timeStr ? new Date(timeStr).toLocaleString() : "未知";
        };

        const startEditing = async (field) => {
            editingField.value = field;
            originalValue.value = localFile.value[field];

            await nextTick();

            if (field === 'title' && titleInput.value) {
                titleInput.value.focus();
            } else if (field === 'description' && descInput.value) {
                descInput.value.focus();
            }
        };

        const saveField = async (field) => {
            if (!editingField.value) return;

            // 如果没有修改，直接退出编辑
            if (localFile.value[field] === originalValue.value) {
                editingField.value = null;
                return;
            }

            saving.value = true;
            try {
                // 调用API更新
                await updateResource(localFile.value.resourceId, {
                    [field]: localFile.value[field]
                });

                // 构造更新后的完整文件对象
                const updatedFile = {
                    ...props.file,
                    [field]: localFile.value[field]
                };

                // 通知父组件
                emit('update:file', updatedFile);

                // 更新本地副本
                localFile.value = updatedFile;

                ElMessage.success('更新成功');
            } catch (error) {
                // 恢复原始值
                localFile.value[field] = originalValue.value;
                ElMessage.error('更新失败: ' + error.message);
            } finally {
                saving.value = false;
                editingField.value = null;
            }
        };

        const cancelEditing = () => {
            if (editingField.value) {
                localFile.value[editingField.value] = originalValue.value;
                editingField.value = null;
            }
        };

        const handleClickOutside = (event) => {
            const isClickInsideEditor = event.target.closest('.el-input') ||
                event.target.closest('.editable-field');

            if (editingField.value && !isClickInsideEditor) {
                saveField(editingField.value);
            }
        };

        // 生命周期
        onMounted(() => {
            document.addEventListener('click', handleClickOutside);
        });

        onUnmounted(() => {
            document.removeEventListener('click', handleClickOutside);
        });

        // 监听props变化
        watch(() => props.file, (newVal) => {
            // 只有当不是当前编辑的字段时才更新
            if (!editingField.value) {
                localFile.value = {...newVal};
            }
        }, { deep: true });

        return {
            isImageType,
            isVideoType,
            isAudioType,
            isOfficeType,
            isPdfType,
            localCurrentVideoUrl,
            localVideoDialogVisible,
            handlePlayVideo,
            formatSize,
            formatTime,
            localFile,
            editingField,
            saving,
            startEditing,
            saveField,
            cancelEditing,
            emit,
            titleInput,
            descInput
        };
    }
};
</script>

<style scoped>
.file-detail {
    width: 100%;
    height: 100%;
    background-color: #f9f9f9;
}

.preview-container {
    height: 300px;
    padding: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f5f5f5;
    border-bottom: 1px solid #eee;
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

.detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 16px 0;
}

.detail-content {
    padding: 0 16px;
}

.detail-row {
    display: flex;
    margin-bottom: 12px;
    font-size: 14px;
}

.detail-label {
    width: 80px;
    color: #666;
    padding-right: 12px;
}

.detail-value {
    flex: 1;
}

.tag-section {
    margin-top: 8px;
}

.tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.download-file{
    cursor: pointer;
}

.empty-value {
    color: #999;
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
    background-color: #f0f0f0;
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
</style>