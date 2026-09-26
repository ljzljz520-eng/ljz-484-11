<template>
  <div class="container author-page">
    <div class="page-header">
      <div>
        <h1 class="page-title text-gradient">作者后台</h1>
        <p class="page-subtitle">管理你的作品与章节草稿，未发布的章节仅在这里可见</p>
      </div>
      <el-button type="primary" size="large" round :icon="Plus" @click="dialogVisible = true">
        新建小说
      </el-button>
    </div>

    <div v-loading="loading" class="novel-list">
      <div v-for="novel in novels" :key="novel.id" class="novel-row glass-panel">
        <img :src="novel.coverUrl" class="novel-cover" :alt="novel.title" />
        <div class="novel-info">
          <h3 class="novel-name">{{ novel.title }}</h3>
          <p class="novel-desc">{{ novel.description }}</p>
          <span class="novel-date">创建于 {{ formatDate(novel.createdAt) }}</span>
        </div>
        <div class="novel-actions">
          <el-button round @click="goChapters(novel)">章节管理</el-button>
        </div>
      </div>
      <el-empty v-if="!loading && novels.length === 0" description="还没有作品，点击右上角创建第一本小说吧" />
    </div>

    <el-dialog v-model="dialogVisible" title="新建小说" width="480px" class="create-dialog">
      <el-form :model="form" label-position="top">
        <el-form-item label="小说标题" required>
          <el-input v-model="form.title" maxlength="50" show-word-limit placeholder="请输入小说标题" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="一句话介绍你的故事..."
          />
        </el-form-item>
        <el-form-item label="封面链接（可选）">
          <el-input v-model="form.coverUrl" placeholder="留空则使用默认封面" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { fetchNovels, createNovel, errorMessage } from '../../api'

const router = useRouter()
const novels = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const creating = ref(false)
const form = ref({ title: '', description: '', coverUrl: '' })

const loadNovels = async () => {
  loading.value = true
  try {
    const res = await fetchNovels({ page: 1, size: 100 })
    novels.value = res.data.data
  } catch (err) {
    ElMessage.error(errorMessage(err))
  } finally {
    loading.value = false
  }
}

const handleCreate = async () => {
  if (!form.value.title.trim()) {
    ElMessage.warning('请输入小说标题')
    return
  }
  creating.value = true
  try {
    const res = await createNovel({
      title: form.value.title.trim(),
      description: form.value.description,
      coverUrl: form.value.coverUrl
    })
    ElMessage.success('小说创建成功，去添加章节吧')
    dialogVisible.value = false
    form.value = { title: '', description: '', coverUrl: '' }
    router.push(`/author/novels/${res.data.id}/chapters`)
  } catch (err) {
    ElMessage.error(errorMessage(err))
  } finally {
    creating.value = false
  }
}

const goChapters = (novel) => {
  router.push(`/author/novels/${novel.id}/chapters`)
}

const formatDate = (val) => {
  if (!val) return ''
  return new Date(val).toLocaleDateString('zh-CN')
}

onMounted(loadNovels)
</script>

<style scoped>
.author-page {
  padding-top: 40px;
  padding-bottom: 60px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.page-title {
  font-size: 2.2rem;
  margin-bottom: 8px;
}

.page-subtitle {
  color: var(--text-sub);
  margin: 0;
}

.novel-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 200px;
}

.novel-row {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px 24px;
  background: white;
}

.novel-cover {
  width: 64px;
  height: 86px;
  object-fit: cover;
  border-radius: 8px;
  box-shadow: var(--shadow-sm);
  flex-shrink: 0;
}

.novel-info {
  flex: 1;
  min-width: 0;
}

.novel-name {
  font-size: 1.15rem;
  margin-bottom: 6px;
}

.novel-desc {
  color: var(--text-sub);
  font-size: 0.9rem;
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.novel-date {
  font-size: 0.8rem;
  color: var(--slate-400);
}

.novel-actions {
  flex-shrink: 0;
}

@media (max-width: 640px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .novel-row {
    flex-wrap: wrap;
  }
}
</style>
