<template>
  <div class="container editor-page" v-loading="loading">
    <div class="editor-topbar glass-panel">
      <el-button @click="goBack" circle plain icon="ArrowLeft"></el-button>
      <div class="topbar-info">
        <span class="novel-name">{{ novelTitle }}</span>
        <el-tag
          v-if="chapter"
          :type="chapter.status === 'PUBLISHED' ? 'success' : 'warning'"
          size="small"
          effect="light"
        >
          {{ chapter.status === 'PUBLISHED' ? '已发布' : '草稿' }}
        </el-tag>
        <span v-if="savedTip" class="saved-tip">{{ savedTip }}</span>
      </div>
      <div class="topbar-actions">
        <el-button round :loading="saving" @click="saveDraft">保存草稿</el-button>
        <el-button
          v-if="!chapter || chapter.status !== 'PUBLISHED'"
          type="primary"
          round
          :loading="publishing"
          @click="saveAndPublish"
        >
          发布
        </el-button>
      </div>
    </div>

    <div class="editor-body glass-panel">
      <input
        v-model="title"
        class="title-input"
        placeholder="请输入章节标题"
        maxlength="80"
      />
      <div class="editor-meta">
        <span>正文 {{ wordCount }} 字</span>
      </div>
      <textarea
        v-model="content"
        class="content-input font-serif"
        placeholder="开始你的创作..."
      ></textarea>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchAuthorChapter, fetchNovelDetail, saveChapterDraft, publishChapter } from '../../api'

const route = useRoute()
const router = useRouter()
const novelId = route.params.novelId
const chapterId = route.params.chapterId

const chapter = ref(null)
const novelTitle = ref('')
const title = ref('')
const content = ref('')
const loading = ref(true)
const saving = ref(false)
const publishing = ref(false)
const savedTip = ref('')

const wordCount = computed(() => (content.value || '').replace(/\s/g, '').length)

const loadChapter = async () => {
  loading.value = true
  try {
    const [chapterRes, novelRes] = await Promise.all([
      fetchAuthorChapter(chapterId), // 作者接口，草稿也可读取
      fetchNovelDetail(novelId)
    ])
    chapter.value = chapterRes.data
    title.value = chapterRes.data.title
    content.value = chapterRes.data.content || ''
    novelTitle.value = novelRes.data.novel.title
  } catch (err) {
    console.error(err)
    ElMessage.error('加载章节失败')
  } finally {
    loading.value = false
  }
}

const validate = () => {
  if (!title.value.trim()) {
    ElMessage.warning('章节标题不能为空')
    return false
  }
  return true
}

const doSave = async () => {
  const res = await saveChapterDraft(chapterId, {
    title: title.value.trim(),
    content: content.value
  })
  chapter.value = res.data
  savedTip.value = '已保存 ' + new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const saveDraft = async () => {
  if (!validate()) return
  saving.value = true
  try {
    await doSave()
    ElMessage.success('草稿已保存')
  } catch (err) {
    console.error(err)
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

const saveAndPublish = async () => {
  if (!validate()) return
  publishing.value = true
  try {
    await doSave() // 先保存最新标题与正文，再发布
    const res = await publishChapter(chapterId)
    chapter.value = res.data
    ElMessage.success('已发布，读者现在可以看到本章')
  } catch (err) {
    console.error(err)
    ElMessage.error('发布失败，请稍后重试')
  } finally {
    publishing.value = false
  }
}

const goBack = () => {
  router.push(`/author/novel/${novelId}`)
}

onMounted(loadChapter)
</script>

<style scoped>
.editor-page {
  padding-top: 40px;
  padding-bottom: 60px;
}

.editor-topbar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 20px;
  background: white;
  margin-bottom: 20px;
  position: sticky;
  top: 80px;
  z-index: 10;
}

.topbar-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.novel-name {
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.saved-tip {
  font-size: 0.8rem;
  color: var(--slate-400);
}

.topbar-actions {
  display: flex;
  gap: 8px;
}

.editor-body {
  background: white;
  padding: 40px 48px;
}

.title-input {
  width: 100%;
  border: none;
  outline: none;
  font-size: 1.8rem;
  font-weight: 700;
  color: var(--text-main);
  padding: 0 0 16px;
  border-bottom: 1px solid var(--slate-100);
  font-family: inherit;
}

.title-input::placeholder {
  color: var(--slate-300);
}

.editor-meta {
  padding: 12px 0;
  font-size: 0.85rem;
  color: var(--slate-400);
}

.content-input {
  width: 100%;
  min-height: 55vh;
  border: none;
  outline: none;
  resize: vertical;
  font-size: 1.1rem;
  line-height: 2;
  color: var(--slate-700);
  font-family: inherit;
}

.content-input::placeholder {
  color: var(--slate-300);
}

@media (max-width: 640px) {
  .editor-body {
    padding: 24px 20px;
  }
}
</style>
