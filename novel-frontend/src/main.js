import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import './assets/main.css'

const app = createApp(App)

app.use(router)
app.use(ElementPlus)

// 全局注册 Element Plus 图标，模板中可直接使用 icon="Plus" 等写法
for (const [name, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(name, component)
}

app.mount('#app')
