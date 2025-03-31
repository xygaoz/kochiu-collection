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
import router from "@/apis/base-routes";
import LoginUI from "@/components/LoginUI.vue";

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

        // 监听路由变化
        router.afterEach((to) => {
            if (to.name === "MainUI") {
                this.state.isLoggedIn = true;
            }
        });
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