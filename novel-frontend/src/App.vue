<template>
  <el-config-provider :locale="locale">
    <div class="app-layout">
      <header class="app-header">
        <div class="container header-content">
          <router-link to="/" class="logo">
            <span class="logo-icon">📚</span>
            Novels
          </router-link>

           <nav class="app-nav">
             <router-link to="/" class="nav-link" exact-active-class="nav-active">书库</router-link>
             <router-link to="/author" class="nav-link" active-class="nav-active">作者后台</router-link>
           </nav>
        </div>
      </header>
      <main class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </el-config-provider>
</template>

<script setup>
import { computed } from 'vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
const locale = zhCn
</script>

<style scoped>
.app-header {
  height: 64px;
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}
.app-nav {
  display: flex;
  align-items: center;
  gap: 24px;
}
.nav-link {
  color: var(--slate-300);
  font-size: 0.95rem;
  font-weight: 500;
  letter-spacing: 0.02em;
}
.nav-link:hover {
  color: #fff;
}
.nav-active {
  color: #fff;
}
.nav-active::after {
  content: '';
  display: block;
  height: 2px;
  margin-top: 2px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 2px;
}
.logo {
  font-size: 24px;
  font-weight: bold;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  display: flex;
  align-items: center;
  gap: 10px;
}
.logo-icon {
  -webkit-text-fill-color: initial;
}
.app-main {
  min-height: calc(100vh - 64px);
  padding-bottom: 40px;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
