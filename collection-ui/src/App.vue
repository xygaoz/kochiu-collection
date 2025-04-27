<template>
    <div id="app">
        <LoginUI v-if="state.isShow === 1" @login-success="handleLoginSuccess" />
        <MainUI v-else-if="state.isShow === 2" />
        <div v-else class="waiting-message">请等待</div>
    </div>
</template>

<script lang="ts">
import { Options, Vue } from 'vue-class-component';
import MainUI from './components/MainUI.vue';
import { reactive } from 'vue';
import LoginUI from "@/components/LoginUI.vue";
import { tokenStore } from "@/apis/system-api";
import { refreshAccessToken } from "@/apis/utils";

let isRefreshing = false;

@Options({
    components: {
        LoginUI,
        MainUI,
    },
})

export default class App extends Vue {
    state = reactive({
        isShow: 0
    });

    async mounted() {
        // 初始状态设置为等待
        this.state.isShow = 0;

        if (!isRefreshing) {
            isRefreshing = true;

            try {
                // 先检查本地token
                const token = tokenStore.getToken();
                if (token) {
                    this.state.isShow = 2;
                    return;
                }

                // 如果没有token，尝试刷新
                const newToken = await refreshAccessToken();
                if (newToken) {
                    this.state.isShow = 2;
                } else {
                    this.state.isShow = 1;
                }
            } catch (error) {
                console.error('Failed to refresh access token:', error);
                this.state.isShow = 1;
            } finally {
                isRefreshing = false;
            }
        }
    }

    handleLoginSuccess() {
        this.state.isShow = 2;
    }
}
</script>

<style>
body {
    margin: 0;
}
.waiting-message {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    font-size: 24px;
}
</style>