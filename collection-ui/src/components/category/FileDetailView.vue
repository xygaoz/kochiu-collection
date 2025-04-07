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
            <el-button
                type="primary"
                size="small"
                @click="emit('download', file)"
                :icon="Download"
            >
                下载
            </el-button>
        </div>

        <el-divider />

        <div class="detail-content">
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
import { ref, computed } from 'vue'
import { Document, Picture } from '@element-plus/icons-vue'

export default {
    components: { Document, Picture },
    props: {
        file: {
            type: Object,
            required: true
        }
    },
    emits: ['download', 'preview-doc'],
    setup(props, { emit }) {
        // 视频相关状态
        const localCurrentVideoUrl = ref('')
        const localVideoDialogVisible = ref(false)

        // 计算属性判断文件类型
        const isImageType = computed(() => props.file.typeName === 'image')
        const isVideoType = computed(() => props.file.typeName === 'video')
        const isAudioType = computed(() => props.file.typeName === 'audio')
        const isOfficeType = computed(() => {
            const officeTypes = [
                'application/msword',
                'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
                'application/vnd.ms-excel',
                'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
                'application/vnd.ms-powerpoint',
                'application/vnd.openxmlformats-officedocument.presentationml.presentation'
            ]
            return officeTypes.includes(props.file.mimeType) && props.file.previewUrl
        })
        const isPdfType = computed(() => props.file.fileType === 'application/pdf' && props.file.previewUrl)

        // 播放视频方法
        const handlePlayVideo = () => {
            localCurrentVideoUrl.value = props.file.resourceUrl
            localVideoDialogVisible.value = true
        }

        // 辅助方法：格式化文件大小
        const formatSize = (bytes) => {
            if (!bytes) return "未知"
            const units = ["B", "KB", "MB", "GB"]
            let size = bytes
            let unitIndex = 0
            while (size >= 1024 && unitIndex < units.length - 1) {
                size /= 1024
                unitIndex++
            }
            return `${size.toFixed(2)} ${units[unitIndex]}`
        }

        // 辅助方法：格式化时间
        const formatTime = (timeStr) => {
            return timeStr ? new Date(timeStr).toLocaleString() : "未知"
        }

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
            emit
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
</style>