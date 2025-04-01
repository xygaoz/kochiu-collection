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
import { tokenStore } from "@/apis/services"; // 引入tokenStore

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

    mounted() {
        // 初始状态
        this.state.isLoggedIn = false;

        debugger
        // 获取token并设置isLoggedIn状态
        const token = tokenStore.getToken();
        if (token) {
            this.state.isLoggedIn = true;
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