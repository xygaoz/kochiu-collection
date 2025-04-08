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
                        v-model="localFile.star"
                        @change="handleRateChange"
                        :disabled="saving"
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

            <div class="detail-row">
                <div class="detail-label">标签</div>
                <div class="detail-value">
                    <div class="tags-container">
                        <!-- 已有标签展示 -->
                        <div v-for="tag in localFile.tags" :key="tag.tagId" class="tag-item">
                            <span>{{ tag.tagName }}</span>
                            <el-icon class="delete-icon" @click="removeTag(tag.tagId)">
                                <Close />
                            </el-icon>
                        </div>

                        <!-- 新标签输入框 -->
                        <el-input
                            v-model="newTagName"
                            ref="tagInput"
                            size="small"
                            class="new-tag-input"
                            placeholder="+ 添加标签"
                            @keyup.enter="addTag"
                            @blur="addTag"
                        />
                    </div>
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

<script lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, PropType, watch } from "vue";
import { Document, Download, Picture, Loading, Close } from '@element-plus/icons-vue'
import { ElMessage, ElInput } from 'element-plus'
import { updateResource } from '@/apis/services'
import type { Resource, Tag } from '@/apis/interface'

export default {
    name: 'FileDetail',
    components: { Close, Download, Document, Picture, Loading },
    props: {
        file: {
            type: Object as PropType<Resource>,
            required: true,
            default: () => ({
                resourceId: 0,
                resourceUrl: '',
                thumbnailUrl: '',
                sourceFileName: '',
                title: '',
                description: '',
                resolutionRatio: '',
                size: 0,
                isPublic: 0,
                star: 0,
                tags: [],
                createTime: '',
                updateTime: '',
                width: 0,
                height: 0,
                fileType: '',
                typeName: '',
                mimeType: '',
                previewUrl: ''
            })
        }
    },
    emits: {
        'download': (file: Resource) => !!file,
        'preview-doc': (file: Resource) => !!file,
        'update-file': (file: Resource) => !!file
    },
    setup(props: { file: Resource }, { emit }: { emit: (event: string, ...args: any[]) => void }) {
        type EditableField = 'title' | 'description' | 'tags'

        // 状态定义
        const localCurrentVideoUrl = ref<string>('')
        const localVideoDialogVisible = ref<boolean>(false)
        const localFile = ref<Resource>({...props.file})
        const editingField = ref<EditableField | ''>('')
        const originalValue = ref<string | Tag[] | undefined>()
        const saving = ref<boolean>(false)
        const titleInput = ref<InstanceType<typeof ElInput> | null>(null)
        const descInput = ref<InstanceType<typeof ElInput> | null>(null)
        const newTagName = ref<string>('')
        const tagInput = ref<InstanceType<typeof ElInput> | null>(null)

        // 计算属性
        const isImageType = computed<boolean>(() => props.file.typeName === 'image')
        const isVideoType = computed<boolean>(() => props.file.typeName === 'video')
        const isAudioType = computed<boolean>(() => props.file.typeName === 'audio')
        const isOfficeType = computed<boolean>(() => {
            const officeTypes = [
                'application/msword',
                'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
                'application/vnd.ms-excel',
                'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
                'application/vnd.ms-powerpoint',
                'application/vnd.openxmlformats-officedocument.presentationml.presentation'
            ]
            return officeTypes.includes(props.file.mimeType) && !!props.file.previewUrl
        })
        const isPdfType = computed<boolean>(() => props.file.fileType === 'application/pdf' && !!props.file.previewUrl)

        // 方法定义
        const handlePlayVideo = (): void => {
            localCurrentVideoUrl.value = props.file.resourceUrl
            localVideoDialogVisible.value = true
        }

        const formatSize = (bytes: number): string => {
            if (!bytes) return '未知'
            const units = ['B', 'KB', 'MB', 'GB']
            let size = bytes
            let unitIndex = 0
            while (size >= 1024 && unitIndex < units.length - 1) {
                size /= 1024
                unitIndex++
            }
            return `${size.toFixed(2)} ${units[unitIndex]}`
        }

        const formatTime = (timeStr: string): string => {
            return timeStr ? new Date(timeStr).toLocaleString() : '未知'
        }

        const startEditing = async (field: EditableField): Promise<void> => {
            editingField.value = field
            originalValue.value = field === 'tags'
                ? [...(localFile.value.tags || [])]
                : localFile.value[field]?.toString() || ''

            await nextTick()

            const inputRef = field === 'title' ? titleInput.value : descInput.value
            if (inputRef) {
                inputRef.focus()
            }
        }

        const handleRateChange = async (value: number): Promise<void> => {
            saving.value = true
            try {
                await updateResource(localFile.value.resourceId, {
                    star: value
                })

                const updatedFile: Resource = {
                    ...props.file,
                    star: value
                }

                localFile.value = updatedFile
                emit('update-file', updatedFile)

                ElMessage.success('评分更新成功')
            } catch (error) {
                localFile.value.star = props.file.star
                ElMessage.error(`评分更新失败: ${error instanceof Error ? error.message : String(error)}`)
            } finally {
                saving.value = false
            }
        }

        const saveField = async (field: EditableField): Promise<void> => {
            if (!editingField.value) return

            const currentValue = field === 'tags'
                ? localFile.value.tags
                : localFile.value[field]

            if (JSON.stringify(currentValue) === JSON.stringify(originalValue.value)) {
                editingField.value = ''
                return
            }

            saving.value = true
            try {
                await updateResource(localFile.value.resourceId, {
                    [field]: currentValue
                })

                const updatedFile: Resource = {
                    ...props.file,
                    [field]: currentValue
                }

                emit('update-file', updatedFile)
                localFile.value = updatedFile

                ElMessage.success('更新成功')
            } catch (error) {
                if (field === 'tags') {
                    localFile.value.tags = originalValue.value as Tag[] || []
                } else {
                    localFile.value[field] = originalValue.value as string
                }
                ElMessage.error(`更新失败: ${error instanceof Error ? error.message : String(error)}`)
            } finally {
                saving.value = false
                editingField.value = ''
            }
        }

        const cancelEditing = (): void => {
            if (editingField.value) {
                if (editingField.value === 'tags') {
                    localFile.value.tags = originalValue.value as Tag[] || []
                } else {
                    localFile.value[editingField.value] = originalValue.value as string
                }
                editingField.value = ''
            }
        }

        const handleClickOutside = (event: MouseEvent): void => {
            const target = event.target as HTMLElement
            const isClickInsideEditor = target.closest('.el-input') || target.closest('.editable-field')

            if (editingField.value && !isClickInsideEditor) {
                saveField(editingField.value)
            }
        }

        const addTag = async (): Promise<void> => {
            const tagName = newTagName.value.trim()
            if (!tagName) return

            if (localFile.value.tags?.some(t => t.tagName === tagName)) {
                ElMessage.warning('标签已存在')
                newTagName.value = ''
                return
            }

            try {
                const newTag: Tag = {
                    tagId: Date.now(), // 临时ID，实际应从API获取
                    tagName: tagName
                }

                const updatedTags = [...(localFile.value.tags || []), newTag]
                const updatedFile: Resource = {
                    ...localFile.value,
                    tags: updatedTags
                }

                await updateResource(localFile.value.resourceId, {
                    tagIds: updatedTags.map(t => t.tagId)
                })

                localFile.value = updatedFile
                emit('update-file', updatedFile)

                newTagName.value = ''
                ElMessage.success('标签添加成功')
            } catch (error) {
                ElMessage.error(`添加标签失败: ${error instanceof Error ? error.message : String(error)}`)
            }
        }

        const removeTag = async (tagId: number): Promise<void> => {
            try {
                const updatedTags = localFile.value.tags?.filter(t => t.tagId !== tagId) || []
                const updatedFile: Resource = {
                    ...localFile.value,
                    tags: updatedTags
                }

                await updateResource(localFile.value.resourceId, {
                    tagIds: updatedTags.map(t => t.tagId)
                })

                localFile.value = updatedFile
                emit('update-file', updatedFile)

                ElMessage.success('标签已删除')
            } catch (error) {
                ElMessage.error(`删除标签失败: ${error instanceof Error ? error.message : String(error)}`)
            }
        }

        const focusInput = (): void => {
            nextTick(() => {
                tagInput.value?.focus()
            })
        }

        // 生命周期
        onMounted(() => {
            document.addEventListener('click', handleClickOutside)
        })

        onUnmounted(() => {
            document.removeEventListener('click', handleClickOutside)
        })

        // 监听props变化
        watch(() => props.file, (newVal: Resource) => {
            if (!editingField.value) {
                localFile.value = {...newVal}
            }
        }, { deep: true })

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
            descInput,
            handleRateChange,
            newTagName,
            tagInput,
            addTag,
            removeTag,
            focusInput
        }
    }
}
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