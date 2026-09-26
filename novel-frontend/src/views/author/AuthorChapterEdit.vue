<template>
  <div class="container edit-page" v-loading="loading">
    <el-button plain class="back-btn" @click="goBack">
      <el-icon><ArrowLeft /></el-icon>&nbsp;返回章节管理
    </el-button>

    <div v-if="chapter" class="editor-panel glass-panel">
      <div class="editor-head">
        <el-tag v-if="chapter.status === 'PUBLISHED'" type="success" size="large">已发布</el-tag>
        <el-tag v-else type="warning" size="large">草稿（仅作者可见）</el-tag>
        <span class="order-no" v-if="chapter.orderNo">第 {{ chapter.orderNo }} 章</span>
      </div>

      <el-input
        v-model="chapter.title"
        class="title-input"
        placeholder="请输入章节标题"
        maxlength="80"
        size="large"
      />

      <el-input
        v-model="chapter.content"
        class="content-input"
        type="textarea"
        :rows="22"
        placeholder="在这里开始写作，可随时保存草稿，未发布章节仅作者后台可见"
        maxlength="100000"
        show-word-limit
      />

      <div class="editor-actions">
        <div class="save-tip" v-if="lastSavedAt">草稿已保存：{{ lastSavedAt }}</div>
        <div class="action-buttons">
          <el-button size="large" :loading="saving" @click="saveDraft">保存草稿</el-button>
          <el-button
            type="primary"
            size="large"
            :loading="publishing"
            @click="saveAndPublish"
          >
            {{ chapter.status === 'PUBLISHED' ? '保存并更新发布' : '保存并发布' }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { fetchAuthorChapter, updateChapter, publishChapter } from '../../api/author'

const route = useRoute()
const router = useRouter()

const novelId = route.params.novelId
const chapterId = route.params.chapterId

const chapter = ref(null)
const loading = ref(true)
const saving = ref(false)
const publishing = ref(false)
const lastSavedAt = ref('')

const payload = () => ({
  title: chapter.value.title,
  content: chapter.value.content
})

const load = async () => {
  loading.value = true
  try {
    chapter.value = await fetchAuthorChapter(chapterId)
  } catch (err) {
    if (err.response?.status === 404) {
      ElMessage.error('章节不存在')
      router.replace(`/author/novel/${novelId}`)
    } else {
      ElMessage.error('章节加载失败')
    }
    console.error(err)
  } finally {
    loading.value = false
  }
}

const validateForm = () => {
  if (!chapter.value.title || !chapter.value.title.trim()) {
    ElMessage.warning('请先填写章节标题')
    return false
  }
  return true
}

// 保存草稿：标题/正文被更新，但章节状态不变，草稿依旧不对读者可见
const saveDraft = async () => {
  if (!validateForm()) return
  saving.value = true
  try {
    chapter.value = await updateChapter(chapterId, payload())
    lastSavedAt.value = new Date().toLocaleTimeString('zh-CN')
    ElMessage.success('草稿已保存')
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '保存失败')
    console.error(err)
  } finally {
    saving.value = false
  }
}

// 先把最新标题/正文保存下来，再执行发布
const saveAndPublish = async () => {
  if (!validateForm()) return
  if (chapter.value.status !== 'PUBLISHED') {
    try {
      await ElMessageBox.confirm('发布后该章节将对所有读者可见，确认发布吗？', '发布章节', {
        confirmButtonText: '发布',
        cancelButtonText: '取消',
        type: 'warning'
      })
    } catch {
      return
    }
  }
  publishing.value = true
  try {
    chapter.value = await updateChapter(chapterId, payload())
    if (chapter.value.status !== 'PUBLISHED') {
      chapter.value = await publishChapter(chapterId)
    }
    lastSavedAt.value = new Date().toLocaleTimeString('zh-CN')
    ElMessage.success(chapter.value.status === 'PUBLISHED' ? '已保存并发布' : '操作成功')
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '操作失败')
    console.error(err)
  } finally {
    publishing.value = false
  }
}

const goBack = () => {
  router.push(`/author/novel/${novelId}`)
}

onMounted(load)
</script>

<style scoped>
.edit-page {
  padding-top: 30px;
}

.back-btn {
  margin-bottom: 20px;
}

.editor-panel {
  padding: 32px;
  background: #fff;
}

.editor-head {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
}

.order-no {
  color: var(--text-sub);
  font-size: 0.95rem;
}

.title-input {
  margin-bottom: 20px;
}

:deep(.title-input .el-input__inner) {
  font-size: 1.4rem;
  font-weight: 600;
}

.content-input {
  margin-bottom: 24px;
}

:deep(.content-input .el-textarea__inner) {
  line-height: 1.9;
  font-size: 1rem;
  font-family: 'Merriweather', 'Inter', serif;
}

.editor-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  border-top: 1px solid var(--border-color);
  padding-top: 20px;
}

.save-tip {
  color: var(--slate-400);
  font-size: 0.85rem;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

@media (max-width: 768px) {
  .editor-actions {
    flex-direction: column;
    align-items: stretch;
  }
  .action-buttons {
    justify-content: stretch;
  }
  .action-buttons .el-button {
    flex: 1;
  }
}
</style>
