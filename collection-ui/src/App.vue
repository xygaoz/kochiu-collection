<template>
  <div id="app">
    <LoginUI v-if="!isLoggedIn" />
    <MainUI v-else />
  </div>
</template>

<script lang="ts">
import { Options, Vue } from 'vue-class-component';
import LoginUI from './components/LoginUI.vue';
import MainUI from './components/MainUI.vue';
import { ref } from 'vue';

@Options({
  components: {
      LoginUI,
      MainUI,
  },
})
export default class App extends Vue {
  isLoggedIn = ref(false);

  mounted() {
    // 检查是否已经登录，这里可以添加检查逻辑
    // 假设未登录
    this.isLoggedIn.value = false;
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}
html,
body,
#app {
  height: 100%;
  margin: 0;
  padding: 0;
}
</style>