// main.ts
import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import router from "./apis/base-routes";
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import './assets/iconfont/iconfont.css'
import * as Icons from '@element-plus/icons-vue'
import { createPinia } from "pinia";
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import { useThemeStore } from "@/utils/themeStore";

const app = createApp(App)
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)
app.use(pinia)

// 必须在app.use(pinia)之后
const initApp = async () => {
  const themeStore = useThemeStore();
  await themeStore.initTheme();

  app.use(ElementPlus);
  for (const [key, component] of Object.entries(Icons)) {
    app.component(key, component)
  }
  app.use(router)
  app.mount('#app');
};

initApp().catch(console.error);