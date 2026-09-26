<template>
  <div class="read-page" v-loading="loading">

     <div class="reader-container">
         <div v-if="notFound" class="chapter-unavailable">
             <h2 class="unavailable-title">章节不存在或尚未发布</h2>
             <p class="unavailable-desc">未发布的草稿仅作者在后台可见，发布后即可正常阅读。</p>
             <el-button type="primary" round @click="goHome">回到首页</el-button>
         </div>

         <div class="content-paper" v-else-if="chapter">
             <h2 class="chapter-heading">{{ chapter.title }}</h2>
             <div class="text-content font-serif">
                 <p v-for="(para, idx) in paragraphs" :key="idx">{{ para }}</p>
             </div>
         </div>

         <div class="footer-controls" v-if="chapter">
             <!-- Navigation logic could be added here if we fetched next/prev IDs -->
             <el-button class="nav-chapter-btn glass-panel" @click="goBack">返回目录</el-button>
         </div>
     </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchChapter } from '../api/novel'

const route = useRoute()
const router = useRouter()
const chapter = ref(null)
const notFound = ref(false)
const loading = ref(true)

const loadChapter = async () => {
  loading.value = true
  notFound.value = false
  try {
    chapter.value = await fetchChapter(route.params.id)
  } catch(err) {
    // 草稿章节在公开接口返回 404，不会泄露正文
    if (err.response && err.response.status === 404) {
      notFound.value = true
    }
    console.error(err)
  } finally {
    loading.value = false
  }
}

const paragraphs = computed(() => {
    if(!chapter.value || !chapter.value.content) return []
    return chapter.value.content.split('\n')
})

const goBack = () => {
    if(window.history.length > 1) {
        router.back()
    } else {
        router.push('/')
    }
}

const goHome = () => {
    router.push('/')
}

onMounted(loadChapter)
</script>

<style scoped>
.read-page {
    min-height: 100vh;
    background-color: #fcf6e5; /* Warm paper background */
    color: #374151;
    position: relative;
    padding-top: 60px;
}

.reader-container {
    max-width: 720px; /* Optimal reading width */
    margin: 0 auto;
    padding: 0 20px 80px;
}

.content-paper {
    padding: 0px 0 40px;
}

.chapter-heading {
    text-align: center;
    font-size: 2rem;
    margin-bottom: 3rem;
    color: #111827;
    font-family: 'Merriweather', serif;
}

.text-content {
    font-size: 1.25rem;
    line-height: 2;
    color: #374151;
}

.text-content p {
    margin-bottom: 2em;
    text-align: justify;
}

.footer-controls {
    margin-top: 60px;
    display: flex;
    justify-content: center;
}

.nav-chapter-btn {
    padding: 20px 40px;
    background: transparent;
    color: #4b5563;
    border: 1px solid rgba(0, 0, 0, 0.1);
    transition: all 0.3s;
}

.nav-chapter-btn:hover {
    background: rgba(0, 0, 0, 0.05);
    border-color: var(--primary-color);
    color: var(--primary-color);
}

.chapter-unavailable {
    text-align: center;
    padding: 80px 20px;
}

.unavailable-title {
    font-size: 1.6rem;
    color: #111827;
    margin-bottom: 12px;
}

.unavailable-desc {
    color: #6b7280;
    margin-bottom: 30px;
}
</style>
