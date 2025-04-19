/* eslint-disable */
import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import router from "./apis/base-routes";
import 'element-plus/dist/index.css'
import './assets/iconfont/iconfont.css'
import * as Icons from '@element-plus/icons-vue'
import { createPinia } from "pinia";
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

const app = createApp(App)
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

app.use(ElementPlus)
app.use(pinia)
for (const [key, component] of Object.entries(Icons)) {
  app.component(key, component)
}
app.use(router)
app.mount('#app')