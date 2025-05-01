<template>
    <div class="enhanced-video-player">
        <!-- 主容器 -->
        <div class="player-container">
            <!-- 视频显示区域 -->
            <div class="video-display">
                <video
                    ref="videoElement"
                    :src="src"
                    :poster="poster"
                    class="video-element"
                    @timeupdate="updateCurrentTime"
                    @loadedmetadata="handleLoadedMetadata"
                    @play="handlePlay"
                    @pause="handlePause"
                    @ended="handleEnded"
                    @error="handleError"
                ></video>

                <!-- 中央播放按钮 -->
                <div class="center-play-button" v-if="!isPlaying" @click="togglePlay">
                    <el-button circle class="play-button-large">
                        <el-icon :size="32"><VideoPlay /></el-icon>
                    </el-button>
                </div>
            </div>

            <!-- 进度条 -->
            <div class="progress-bar">
                <el-slider
                    v-model="currentTime"
                    :max="duration"
                    :format-tooltip="formatTimeTooltip"
                    @change="handleSeek"
                    :show-tooltip="false"
                    :disabled="!videoReady"
                    class="custom-slider"
                />
            </div>

            <!-- 底部控制栏 -->
            <div class="bottom-controls">
                <el-button circle @click="togglePlay" class="control-button">
                    <el-icon :size="18">
                        <VideoPlay v-if="!isPlaying"/>
                        <VideoPause v-else/>
                    </el-icon>
                </el-button>

                <div class="time-display">
                    {{ formatTime(currentTime) }} / {{ formatTime(duration) }}
                </div>

                <el-button circle @click="toggleFullscreen" class="control-button">
                    <el-icon :size="18"><FullScreen /></el-icon>
                </el-button>
            </div>
        </div>

        <!-- 全屏弹窗 -->
        <el-dialog
            v-model="fullscreenVisible"
            title="视频播放"
            width="80%"
            top="5vh"
            destroy-on-close
            class="video-dialog"
            @close="handleFullscreenClose"
        >
            <video
                ref="fullscreenVideo"
                controls
                autoplay
                :src="src"
                class="fullscreen-video"
            ></video>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, watch, defineProps, defineEmits, nextTick } from "vue";
import { VideoPlay, VideoPause, FullScreen } from '@element-plus/icons-vue';

const props = defineProps({
    src: {
        type: String,
        required: true
    },
    poster: {
        type: String,
        default: ''
    },
    mimeType: {
        type: String,
        default: 'video/*'
    },
    autoplay: {
        type: Boolean,
        default: false
    },
    loop: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits(['play', 'pause', 'ended', 'error', 'timeupdate']);

const videoElement = ref<HTMLVideoElement | null>(null);
const fullscreenVideo = ref<HTMLVideoElement | null>(null);
const isPlaying = ref(false);
const currentTime = ref(0);
const duration = ref(0);
const videoReady = ref(false);
const fullscreenVisible = ref(false);

// 播放控制方法
const togglePlay = () => {
    if (!videoElement.value || !videoReady.value) return;
    isPlaying.value ? videoElement.value.pause() : videoElement.value.play();
};

const updateCurrentTime = () => {
    if (videoElement.value) {
        currentTime.value = videoElement.value.currentTime;
        emit('timeupdate', currentTime.value);
    }
};

const handleLoadedMetadata = () => {
    if (videoElement.value) {
        duration.value = videoElement.value.duration;
        videoReady.value = true;
        props.autoplay && videoElement.value.play();
    }
};

const handleSeek = (val: number) => {
    videoElement.value && (videoElement.value.currentTime = val);
};

const handlePlay = () => {
    isPlaying.value = true;
    emit('play');
};

const handlePause = () => {
    isPlaying.value = false;
    emit('pause');
};

const handleEnded = () => {
    isPlaying.value = false;
    if (props.loop && videoElement.value) {
        videoElement.value.currentTime = 0;
        videoElement.value.play();
    }
    emit('ended');
};

const handleError = (error: Event) => {
    console.error('视频播放错误:', error);
    emit('error', error);
};

// 全屏控制
const toggleFullscreen = () => {
    if (videoElement.value && videoReady.value) {
        // 保存当前播放时间和播放状态
        const currentTimeValue = videoElement.value.currentTime;
        const wasPlaying = isPlaying.value;

        // 打开弹窗并设置时间
        fullscreenVisible.value = true;

        // 在弹窗打开后设置时间
        nextTick(() => {
            if (fullscreenVideo.value) {
                fullscreenVideo.value.currentTime = currentTimeValue;
                if (wasPlaying) {
                    // 暂停原视频
                    videoElement.value?.pause();
                    // 播放弹窗视频
                    fullscreenVideo.value.play().catch(e => console.error('全屏播放失败:', e));
                }
            }
        });
    }
};

// 修改弹窗关闭处理
const handleFullscreenClose = () => {
    if (fullscreenVideo.value && videoElement.value) {
        // 保存弹窗中的播放时间和状态
        const currentTimeValue = fullscreenVideo.value.currentTime;
        const wasPlaying = !fullscreenVideo.value.paused;

        // 关闭弹窗
        fullscreenVideo.value.pause();
        fullscreenVisible.value = false;

        // 将时间同步回原视频
        videoElement.value.currentTime = currentTimeValue;

        // 如果弹窗中正在播放，恢复原视频播放
        if (wasPlaying) {
            videoElement.value.play().catch(e => console.error('恢复播放失败:', e));
        }
    }
};

// 时间格式化
const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
};

const formatTimeTooltip = (val: number) => formatTime(val);

watch(() => props.src, (newSrc) => {
    if (videoElement.value && newSrc) {
        videoReady.value = false;
        videoElement.value.src = newSrc;
        videoElement.value.load();
    }
});
</script>

<style scoped>
.enhanced-video-player {
    width: 100%;
    max-width: 800px;
    background-color: #000;
    border-radius: 8px;
    overflow: hidden;
}

.player-container {
    display: flex;
    flex-direction: column;
    aspect-ratio: 16/9;
}

/* 视频显示区域 */
.video-display {
    position: relative;
    flex: 1;
    background-color: #000;
}

.video-element {
    width: 100%;
    height: 100%;
    object-fit: contain;
    display: block;
}

.center-play-button {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
}

.play-button-large {
    width: 60px;
    height: 60px;
    background-color: rgba(0, 0, 0, 0.5);
    border: none;
    color: white;
}

/* 进度条 */
.progress-bar {
    padding: 0 10px;
    background-color: rgba(0, 0, 0, 0.7);
}

.custom-slider {
    width: 100%;
}

.custom-slider :deep(.el-slider__runway) {
    margin: 0;
    height: 4px;
    background-color: rgba(255, 255, 255, 0.3);
}

.custom-slider :deep(.el-slider__bar) {
    height: 4px;
    background-color: var(--el-color-primary);
}

.custom-slider :deep(.el-slider__button) {
    width: 12px;
    height: 12px;
    border: 2px solid var(--el-color-primary);
}

/* 底部控制栏 */
.bottom-controls {
    display: flex;
    align-items: center;
    padding: 0 0 8px 0;
    background-color: rgba(0, 0, 0, 0.7);
}

.control-button {
    width: 36px;
    height: 36px;
    margin: 0 5px;
    background-color: transparent;
    border: none;
    color: white;
}

.time-display {
    flex: 1;
    font-size: 14px;
    color: white;
    text-align: center;
    margin: 0 10px;
}

/* 全屏弹窗样式 */
.video-dialog {
    max-width: 90vw;
    max-height: 90vh;
}

.video-dialog :deep(.el-dialog__body) {
    padding: 0;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: #000;
}

.fullscreen-video {
    width: 100%;
    max-height: 80vh;
}
</style>