<template>
    <el-dialog
        v-model="visible"
        title="系统信息"
        width="500px"
    >
        <div class="info-content">
            <div class="info-item">
                <span class="info-label">应用名称：</span>
                <span>{{ appName }}</span>
            </div>
            <div class="info-item">
                <span class="info-label">版本号：</span>
                <span>{{ version }}</span>
            </div>
            <div class="info-item">
                <span class="info-label">作者：</span>
                <span>{{ author }}</span>
            </div>
            <div class="info-item">
                <span class="info-label">开源协议：</span>
                <span>{{ license }}</span>
            </div>
            <div class="info-item">
                <span class="info-label">项目地址：</span>
                <a :href="repoUrl" target="_blank">{{ repoUrl }}</a>
            </div>
        </div>
        <template #footer>
            <el-button type="primary" @click="close">确定</el-button>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps({
    appName: {
        type: String,
        default: 'KoChiu Collection'
    },
    version: {
        type: String,
        default: 'v0.1.0'
    },
    author: {
        type: String,
        default: 'KoChiu'
    },
    license: {
        type: String,
        default: 'MIT License'
    },
    repoUrl: {
        type: String,
        default: 'https://github.com/your-repo'
    }
})

const visible = ref(false)

const open = () => {
    visible.value = true
}

const close = () => {
    visible.value = false
}

// 暴露方法给父组件
defineExpose({
    open,
    close
})
</script>

<style scoped>
.info-content {
    line-height: 2;
    padding: 10px 20px;
}

.info-item {
    display: flex;
    margin-bottom: 8px;
}

.info-label {
    font-weight: bold;
    width: 80px;
    flex-shrink: 0;
}

.info-content a {
    color: var(--el-color-primary);
    text-decoration: none;
}

.info-content a:hover {
    text-decoration: underline;
}
</style>