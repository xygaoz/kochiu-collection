<template>
    <div class="enhanced-audio-player">
        <!-- 波形图部分 - 占80%高度 -->
        <div class="wave-container" ref="waveContainer">
            <div
                v-for="i in 20"
                :key="i"
                class="wave-bar"
                :style="getWaveBarStyle(i)"
            ></div>
        </div>

        <!-- 进度条部分 - 占10%高度 -->
        <div class="progress-container">
            <el-slider
                v-model="currentTime"
                :max="duration"
                :format-tooltip="formatTimeTooltip"
                @change="handleSeek"
                :show-tooltip="false"
                :disabled="!audioReady"
            />
        </div>

        <!-- 控制按钮部分 - 占10%高度 -->
        <div class="controls-container">
            <!-- 播放/暂停按钮 -->
            <el-button
                circle
                @click="togglePlay"
                class="play-button"
                :disabled="!audioReady"
            >
                <el-icon :size="20">
                    <VideoPlay v-if="!isPlaying"/>
                    <VideoPause v-else/>
                </el-icon>
            </el-button>

            <!-- 时间显示 -->
            <div class="time-display">
                {{ formatTime(currentTime) }} / {{ formatTime(duration) }}
            </div>

            <!-- 音量控制 -->
            <div class="volume-control">
                <el-popover
                    placement="top"
                    :width="32"
                    trigger="click"
                    popper-class="volume-popper"
                    :popper-style="{ minWidth: '32px', width: '32px', maxWidth: '32px' }"
                >
                    <template #reference>
                        <el-button circle class="volume-button" :disabled="!audioReady">
                            <el-icon :size="18">
                                <i class="iconfont icon-col-volume-medium" style="font-size: 18px" v-if="volume > 0.5"/>
                                <i class="iconfont icon-col-volume-low" style="font-size: 18px" v-else-if="volume > 0"/>
                                <Mute v-else/>
                            </el-icon>
                        </el-button>
                    </template>
                    <el-slider
                        v-model="volume"
                        vertical
                        height="80px"
                        :min="0"
                        :max="1"
                        :step="0.1"
                        @change="handleVolumeChange"
                        class="volume-slider"
                    />
                </el-popover>
            </div>
        </div>

        <!-- 隐藏的原生audio元素 -->
        <audio
            ref="audioElement"
            :src="src"
            @timeupdate="updateCurrentTime"
            @loadedmetadata="handleLoadedMetadata"
            @play="handlePlay"
            @pause="handlePause"
            @ended="handleEnded"
            @error="handleError"
            class="native-audio"
        ></audio>
    </div>
</template>

<script setup lang="ts">
import { ref, defineProps, defineEmits, onMounted, watch, onBeforeUnmount } from "vue";
import {
    VideoPlay,
    VideoPause,
    Mute
} from '@element-plus/icons-vue';

const props = defineProps({
    src: {
        type: String,
        required: true
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

const audioElement = ref<HTMLAudioElement | null>(null);
const isPlaying = ref(false);
const currentTime = ref(0);
const duration = ref(0);
const volume = ref(0.8);
const waveActive = ref(false);
const audioReady = ref(false);
const waveContainer = ref<HTMLElement | null>(null);
const containerHeight = ref(0);

// 更新容器高度
const updateContainerHeight = () => {
    if (waveContainer.value) {
        containerHeight.value = waveContainer.value.clientHeight - 55;
    }
};

// 初始化时获取高度
onMounted(() => {
    updateContainerHeight();
    window.addEventListener('resize', updateContainerHeight);
});

// 组件卸载时移除监听
onBeforeUnmount(() => {
    window.removeEventListener('resize', updateContainerHeight);
});

// 修改getWaveBarStyle方法
const getWaveBarStyle = (i: number) => {
    if (containerHeight.value === 0) return {};

    const maxHeight = containerHeight.value * 0.9; // 最大高度为容器高度的90%
    const minHeight = containerHeight.value * 0.1; // 最小高度为容器高度的10%

    const baseHeight = minHeight + Math.random() * (maxHeight - minHeight) * 0.3;
    const activeHeight = waveActive.value
        ? baseHeight + Math.random() * (maxHeight - baseHeight)
        : baseHeight;

    return {
        height: `${activeHeight}px`,
        animationDelay: `${i * 0.05}s`,
        backgroundColor: waveActive.value
            ? 'var(--el-color-primary)'
            : 'var(--el-color-primary-light-5)'
    };
};
// 切换播放/暂停
const togglePlay = () => {
    if (!audioElement.value || !audioReady.value) return;

    if (isPlaying.value) {
        audioElement.value.pause();
    } else {
        audioElement.value.play();
        startWaveAnimation();
    }
};

// 启动波形动画
const startWaveAnimation = () => {
    waveActive.value = true;
    setTimeout(() => waveActive.value = false, 300);
};

// 更新当前播放时间
const updateCurrentTime = () => {
    if (audioElement.value) {
        currentTime.value = audioElement.value.currentTime;
        emit('timeupdate', currentTime.value);
    }
};

// 处理元数据加载
const handleLoadedMetadata = () => {
    if (audioElement.value) {
        duration.value = audioElement.value.duration;
        audioReady.value = true;
        if (props.autoplay) {
            audioElement.value.play();
        }
    }
};

// 跳转到指定时间
const handleSeek = (val: number) => {
    if (audioElement.value) {
        audioElement.value.currentTime = val;
    }
};

// 调整音量
const handleVolumeChange = (val: number) => {
    if (audioElement.value) {
        audioElement.value.volume = val;
    }
};

// 播放事件
const handlePlay = () => {
    isPlaying.value = true;
    emit('play');
};

// 暂停事件
const handlePause = () => {
    isPlaying.value = false;
    emit('pause');
};

// 结束事件
const handleEnded = () => {
    isPlaying.value = false;
    if (props.loop && audioElement.value) {
        audioElement.value.currentTime = 0;
        audioElement.value.play();
    }
    emit('ended');
};

// 错误处理
const handleError = (error: Event) => {
    console.error('音频播放错误:', error);
    emit('error', error);
};

// 格式化时间显示
const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
};

// 进度条tooltip格式化
const formatTimeTooltip = (val: number) => {
    return formatTime(val);
};

// 监听src变化
watch(() => props.src, (newSrc) => {
    if (audioElement.value && newSrc) {
        audioReady.value = false;
        audioElement.value.src = newSrc;
        audioElement.value.load();
    }
});

// 初始化
onMounted(() => {
    if (audioElement.value) {
        audioElement.value.volume = volume.value;
        audioElement.value.loop = props.loop;
    }
});
</script>

<style scoped>
.enhanced-audio-player {
    width: 100%;
    max-width: 500px;
    height: 200px; /* 设置固定高度以便比例分配 */
    display: flex;
    flex-direction: column;
    padding: 12px;
    background-color: var(--el-bg-color);
    border-radius: 8px;
    box-shadow: var(--el-box-shadow-light);
}

/* 波形图容器 - 占80%高度 */
.wave-container {
    flex: 8;
    width: 100%;
    display: flex;
    align-items: flex-end;
    justify-content: center;
    gap: 4px;
    padding: 10px 0 45px 0;
    position: relative; /* 确保高度计算准确 */
}

.wave-bar {
    width: 4px;
    min-height: 10px; /* 设置最小高度 */
    border-radius: 3px;
    transition: all 0.3s ease;
    animation: wave 0.8s infinite alternate;
    will-change: height; /* 优化动画性能 */
}

@keyframes wave {
    from {
        transform: scaleY(0.7);
    }
    to {
        transform: scaleY(1.3);
    }
}

/* 进度条部分 - 占10%高度 */
.progress-container {
    flex: 1; /* 10% */
    width: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.time-display {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    text-align: center;
    display: none;
}

/* 控制按钮部分 - 占10%高度 */
.controls-container {
    flex: 1; /* 10% */
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 10px;
}

.play-button {
    width: 36px;
    height: 36px;
    border: none !important;
    background: transparent !important;
    box-shadow: none !important;
}

.play-button:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.volume-control {
    display: flex;
    align-items: center;
}

.volume-button {
    width: 36px;
    height: 36px;
    border: none !important;
    background: transparent !important;
    box-shadow: none !important;
}

.volume-button:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

/* 调整滑块样式 */
:deep(.volume-popper) {
    min-width: 40px !important;  /* 稍微加宽确保滑块能居中 */
    width: 40px !important;
    padding: 10px 2px !important;
    display: flex;
    justify-content: center;
}

/* 滑块容器样式 */
:deep(.volume-slider) {
    width: 20px !important;
    margin: 0 auto !important;
    display: flex;
    justify-content: center;
}

:deep(.volume-slider .el-slider__runway) {
    margin: 0 !important;
    width: 4px !important;      /* 可选：固定滑道宽度 */
    left: 50% !important;       /* 水平居中 */
    transform: translateX(-50%) !important; /* 微调对齐 */
}

/* 调整滑块按钮位置 */
:deep(.volume-slider .el-slider__button-wrapper) {
    left: -8px !important;      /* 确保按钮对齐滑道 */
}

:deep(.el-slider__runway){
    margin: 0!important;
}

.native-audio {
    display: none;
}

/* 响应式设计 */
@media (min-width: 768px) {
    .time-display {
        display: block;
    }
    .time-text {
        display: none;
    }

    .enhanced-audio-player {
        height: 250px;
    }
}
</style>