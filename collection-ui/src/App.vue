<template>
  <div id="app">
    <LoginUI v-if="!state.isLoggedIn" @login-success="handleLoginSuccess" />
    <MainUI v-else />
  </div>
</template>

<script lang="ts">
import { Options, Vue } from 'vue-class-component';
import MainUI from './components/MainUI.vue';
import { reactive } from 'vue';
import LoginUI from "@/components/LoginUI.vue";
import { tokenStore } from "@/apis/services";
import { refreshAccessToken } from "@/apis/utils"; // 引入tokenStore

let isRefreshing = false;

@Options({
  components: {
      LoginUI,
      MainUI,
  },
})

export default class App extends Vue {
    state = reactive({
        isLoggedIn: false
    });

    async mounted() {
        // 初始状态
        this.state.isLoggedIn = true;

        if (!isRefreshing) {
            isRefreshing = true;

            try {
                // 获取token并设置isLoggedIn状态
                const token = tokenStore.getToken();
                if (token) {
                    this.state.isLoggedIn = true;
                } else {
                    this.state.isLoggedIn = false;
                    // 尝试用refreshToken获取新accessToken
                    refreshAccessToken().then(newToken => {
                        if (newToken) {
                            this.state.isLoggedIn = true;
                        }
                    }).catch(error => {
                        console.error('Failed to refresh access token:', error);
                    });
                }
            }
            catch (error) {
                this.state.isLoggedIn = false;
            }
            finally {
                isRefreshing = false;
            }
        }
    }

    handleLoginSuccess() {
        this.state.isLoggedIn = true;
    }
}
</script>

<style>
body{
    margin: 0;
}
</style>