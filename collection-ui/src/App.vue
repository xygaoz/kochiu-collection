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
    --el-bg-color-page: #f5f7fa; /* light模式背景色 */
    --el-box-shadow-light: #00152914;
}

html.dark {
    --el-bg-color-page: #141414; /* dark模式背景色 */
    --el-box-shadow-light: #00000080;
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