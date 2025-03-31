/* eslint-disable */
import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import router from "./apis/base-routes";
import 'element-plus/dist/index.css'
import './assets/iconfont/iconfont.css'
import * as Icons from '@element-plus/icons-vue'

const app = createApp(App)
app.use(ElementPlus)
for (const [key, component] of Object.entries(Icons)) {
  app.component(key, component)
}
app.use(router)
app.mount('#app')