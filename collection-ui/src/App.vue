<template>
    <div id="app">
        <LoginUI v-if="state.isShow === 1" @login-success="handleLoginSuccess" />
        <MainUI v-else-if="state.isShow === 2" />
        <div v-else class="waiting-message">请等待</div>
    </div>
</template>

<script lang="ts" setup>
import MainUI from './components/MainUI.vue';
import { onMounted, reactive } from "vue";
import LoginUI from "@/components/LoginUI.vue";
import { tokenStore } from "@/apis/system-api";
import { refreshAccessToken } from "@/apis/utils";

let isRefreshing = false;
const state = reactive({ isShow: 0 });

onMounted(async () => {
    state.isShow = 0;

    if (!isRefreshing) {
        isRefreshing = true;
        try {
            const token = tokenStore.getToken();
            if (token) {
                state.isShow = 2;
                return;
            }

            const newToken = await refreshAccessToken();
            state.isShow = newToken ? 2 : 1;
        } catch (error) {
            console.error('Failed to refresh access token:', error);
            state.isShow = 1;
        } finally {
            isRefreshing = false;
        }
    }
})

const handleLoginSuccess = async () => {
    state.isShow = 2;
}
</script>

<style>
#app {
    transition: background-color 0.3s, color 0.3s;
}

:root {
    /* Light 主题变量 */
    --el-text-color-master: #6e6e6e;
    --el-bg-color-page: #f5f7fa;
    --el-bg-color-view: #f5f7fa;
    --el-bg-color-header: #eeeeee;
    --el-border-color-light: #e4e7ed;
    --el-border-color-bottom: #dcdfe6;
    --el-card-border-color: #ebeef5;
    --el-text-color-regular: #606266;
    --el-text-color-secondary: #909399;
    --el-color-primary: #409EFF;
    --el-tag-bg-color: #f0f2f5;
    --el-tag-text-color: #606266;
    --el-box-shadow-light: 0 0 8px rgba(0, 0, 0, 0.1);
    --el-box-shadow-dark: 0 0 8px rgba(0, 0, 0, 0.5);
    --el-menu-hover-bg-color: #f5f7fa;
    --el-item-text-color: #2b2b2b;
    --el-rate-disabled-color: #858585;
}

html.dark {
    /* Dark 主题变量 */
    --el-text-color-master: #ececec;
    --el-bg-color-page: #141414;
    --el-bg-color-view: #2b2b2b;
    --el-bg-color-header: rgb(51, 51, 51);
    --el-border-color-light: #4c4d4f;
    --el-border-color-bottom: #333;
    --el-card-border-color: #333;
    --el-text-color-regular: #CFD3DC;
    --el-text-color-secondary: #909399;
    --el-color-primary: #409EFF;
    --el-tag-bg-color: #1D1E1F;
    --el-tag-text-color: #CFD3DC;
    --el-box-shadow: var(--el-box-shadow-dark);
    --el-menu-hover-bg-color: #2e2e2e;
    --el-item-text-color: #f5f7fa;
    --el-rate-disabled-color: #474747;
}

html:not(.dark) {
    --el-box-shadow: var(--el-box-shadow-light);
}

body {
    margin: 0;
    transition: background-color 0.3s;
}

/* 确保Element Plus组件能响应主题变化 */
.el-switch.is-dark .el-switch__core {
    background-color: var(--el-switch-on-color);
}

.waiting-message {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    font-size: 21px;
}

.el-switch {
    --el-switch-on-color: var(--el-color-primary);
    --el-switch-off-color: var(--el-border-color-light);
}

/* 暗黑模式开关特殊处理 */
html.dark .el-switch.is-dark .el-switch__core {
    background-color: var(--el-switch-on-color) !important;
}
</style>