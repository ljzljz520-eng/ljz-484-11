<template>
  <div class="container editor-page" v-loading="loading">
    <div class="editor-bar">
      <el-button circle plain :icon="ArrowLeft" @click="goBack" class="back-btn"></el-button>
      <div class="editor-meta">
        <h1 class="editor-title">{{ isNew ? '新建章节' : '编辑章节' }}</h1>
        <el-tag
          v-if="!isNew"
          :type="form.status === 'PUBLISHED' ? 'success' : 'warning'"
          effect="light"
          round
          size="small"
        >
          {{ form.status === 'PUBLISHED' ? '已发布' : '草稿' }}
        </el-tag>
      </div>
      <div class="editor-actions">
        <el-button round :loading="saving" @click="handleSave('DRAFT')">
          {{ isNew ? '保存草稿' : '存为草稿' }}
        </el-button>
        <el-button type="primary" round :loading="saving" @click="handleSave('PUBLISHED')">
          {{ isNew ? '保存并发布' : (form.status === 'PUBLISHED' ? '保存修改' : '保存并发布') }}
        </el-button>
      </div>
    </div>

    <div class="editor-panel glass-panel">
      <el-input
        v-model="form.title"
        class="title-input"
        maxlength="80"
        show-word-limit
        placeholder="章节标题，例如：第一章：开端"
      />
      <el-input
        v-model="form.content"
        class="content-input"
        type="textarea"
        :rows="18"
        placeholder="开始你的创作..."
      />
      <div class="editor-footer">
        <span class="word-count">正文 {{ contentLength }} 字</span>
        <span class="save-hint">草稿仅作者可见，发布后读者才能阅读</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import {
  fetchAuthorChapter,
  createChapter,
  updateChapter,
  errorMessage
} from '../../api'

const route = useRoute()
const router = useRouter()

// New mode: /author/novels/:novelId/chapters/new  Edit mode: /author/chapters/:id/edit
const isNew = computed(() => route.params.id === undefined)
const novelId = ref(route.params.novelId)
const chapterId = ref(route.params.id)

const form = ref({ title: '', content: '', status: 'DRAFT' })
const loading = ref(false)
const saving = ref(false)

const contentLength = computed(() => (form.value.content || '').replace(/\s/g, '').length)

const loadChapter = async () => {
  if (isNew.value) return
  loading.value = true
  try {
    const res = await fetchAuthorChapter(chapterId.value)
    form.value = {
      title: res.data.title,
      content: res.data.content,
      status: res.data.status
    }
    novelId.value = res.data.novelId
  } catch (err) {
    ElMessage.error(errorMessage(err))
    goBack()
  } finally {
    loading.value = false
  }
}

const handleSave = async (targetStatus) => {
  if (!form.value.title.trim()) {
    ElMessage.warning('请输入章节标题')
    return
  }
  saving.value = true
  try {
    const payload = {
      title: form.value.title.trim(),
      content: form.value.content,
      status: targetStatus
    }
    if (isNew.value) {
      await createChapter(novelId.value, payload)
    } else {
      await updateChapter(chapterId.value, payload)
    }
    ElMessage.success(targetStatus === 'PUBLISHED' ? '已保存并发布' : '草稿已保存')
    goBack()
  } catch (err) {
    ElMessage.error(errorMessage(err))
  } finally {
    saving.value = false
  }
}

const goBack = () => {
  router.push(`/author/novels/${novelId.value}/chapters`)
}

onMounted(loadChapter)
</script>

<style scoped>
.editor-page {
  padding-top: 40px;
  padding-bottom: 60px;
}

.editor-bar {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
}

.back-btn {
  background: white;
  flex-shrink: 0;
}

.editor-meta {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.editor-title {
  font-size: 1.6rem;
  margin: 0;
}

.editor-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.editor-panel {
  background: white;
  padding: 30px;
}

.title-input {
  margin-bottom: 20px;
}

.title-input :deep(.el-input__wrapper) {
  padding: 12px 16px;
}

.title-input :deep(.el-input__inner) {
  font-size: 1.2rem;
  font-weight: 600;
}

.content-input :deep(.el-textarea__inner) {
  font-size: 1.05rem;
  line-height: 1.9;
  padding: 16px;
  font-family: 'Merriweather', serif;
}

.editor-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  font-size: 0.85rem;
  color: var(--slate-400);
}

@media (max-width: 640px) {
  .editor-bar {
    flex-wrap: wrap;
  }
}
</style>
