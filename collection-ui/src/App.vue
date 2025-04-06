<template>
  <div id="app">
    <LoginUI v-if="state.isShow === 1" @login-success="handleLoginSuccess" />
    <MainUI v-else-if="state.isShow === 2" />
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
        isShow: 0
    });

    async mounted() {
        // 初始状态
        this.state.isShow = 0;

        if (!isRefreshing) {
            isRefreshing = true;

            try {
                // 获取token并设置isShow状态
                const token = tokenStore.getToken();
                if (token) {
                    this.state.isShow = 2;
                } else {
                    this.state.isShow = 1;
                    // 尝试用refreshToken获取新accessToken
                    refreshAccessToken().then(newToken => {
                        if (newToken) {
                            this.state.isShow = 2;
                        }
                    }).catch(error => {
                        console.error('Failed to refresh access token:', error);
                    });
                }
            }
            catch (error) {
                this.state.isShow = 1;
            }
            finally {
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
body{
    margin: 0;
}
</style>