<template>
  <div class="container author-page">
    <div class="page-header">
      <div>
        <h1 class="page-title text-gradient">作者后台</h1>
        <p class="page-subtitle">管理你的作品与章节草稿</p>
      </div>
      <el-button type="primary" size="large" round icon="Plus" @click="dialogVisible = true">
        新建小说
      </el-button>
    </div>

    <div v-loading="loading" class="novel-list">
      <div
        v-for="novel in novels"
        :key="novel.id"
        class="novel-row glass-panel hover-lift"
      >
        <img :src="novel.coverUrl" class="novel-cover" alt="cover" />
        <div class="novel-info">
          <h3 class="novel-name">{{ novel.title }}</h3>
          <p class="novel-desc">{{ novel.description }}</p>
          <span class="novel-date">创建于 {{ formatDate(novel.createdAt) }}</span>
        </div>
        <div class="novel-actions">
          <el-button round @click="goChapters(novel.id)">章节管理</el-button>
        </div>
      </div>
      <el-empty v-if="!loading && novels.length === 0" description="还没有作品，点击右上角创建第一本小说吧" />
    </div>

    <!-- 新建小说弹窗 -->
    <el-dialog v-model="dialogVisible" title="新建小说" width="480px" class="create-dialog">
      <el-form :model="form" label-position="top">
        <el-form-item label="小说标题" required>
          <el-input v-model="form.title" placeholder="请输入小说标题" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="简介">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="一句话介绍你的故事"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="封面图 URL（可选）">
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
import { createNovel, fetchAuthorNovels } from '../../api'

const router = useRouter()
const novels = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const creating = ref(false)
const form = ref({ title: '', description: '', coverUrl: '' })

const loadNovels = async () => {
  loading.value = true
  try {
    const res = await fetchAuthorNovels()
    novels.value = res.data
  } catch (err) {
    console.error(err)
    ElMessage.error('加载小说列表失败')
  } finally {
    loading.value = false
  }
}

const handleCreate = async () => {
  if (!form.value.title.trim()) {
    ElMessage.warning('请填写小说标题')
    return
  }
  creating.value = true
  try {
    const res = await createNovel({
      title: form.value.title.trim(),
      description: form.value.description.trim(),
      coverUrl: form.value.coverUrl.trim()
    })
    ElMessage.success('创建成功，去添加章节吧')
    dialogVisible.value = false
    form.value = { title: '', description: '', coverUrl: '' }
    router.push(`/author/novel/${res.data.id}`)
  } catch (err) {
    console.error(err)
    ElMessage.error('创建失败，请稍后重试')
  } finally {
    creating.value = false
  }
}

const goChapters = (id) => {
  router.push(`/author/novel/${id}`)
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
  margin-bottom: 6px;
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
  padding: 16px 24px;
  background: white;
}

.novel-cover {
  width: 64px;
  height: 86px;
  object-fit: cover;
  border-radius: 6px;
  box-shadow: var(--shadow-sm);
  flex-shrink: 0;
}

.novel-info {
  flex: 1;
  min-width: 0;
}

.novel-name {
  margin: 0 0 6px;
  font-size: 1.15rem;
}

.novel-desc {
  margin: 0 0 6px;
  color: var(--text-sub);
  font-size: 0.9rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.novel-date {
  font-size: 0.8rem;
  color: var(--slate-400);
}

.novel-actions {
  flex-shrink: 0;
}

@media (max-width: 640px) {
  .novel-row {
    flex-wrap: wrap;
  }
  .novel-actions {
    width: 100%;
    text-align: right;
  }
}
</style>
