<template>
  <div class="container author-page">
    <div class="page-head">
      <div>
        <h1 class="page-title text-gradient">作者后台</h1>
        <p class="page-subtitle">管理你的小说作品与章节草稿</p>
      </div>
      <el-button type="primary" size="large" round @click="openCreateDialog">
        <el-icon class="btn-icon"><Plus /></el-icon>创建小说
      </el-button>
    </div>

    <div v-loading="loading" class="novel-grid">
      <div v-for="novel in novels" :key="novel.id" class="novel-card glass-panel hover-lift">
        <div class="card-image" :style="{ backgroundImage: 'url(' + novel.coverUrl + ')' }"></div>
        <div class="card-body">
          <h3 class="novel-title">{{ novel.title }}</h3>
          <p class="novel-desc">{{ novel.description || '暂无简介' }}</p>
          <div class="card-footer">
            <span class="created-at">创建于 {{ formatDate(novel.createdAt) }}</span>
            <el-button type="primary" plain size="small" @click="manageNovel(novel.id)">
              章节管理
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && novels.length === 0" description="还没有作品，点击右上角创建第一本小说吧" />

    <el-dialog v-model="dialogVisible" title="创建小说" width="520px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="书名" prop="title">
          <el-input v-model="form.title" placeholder="请输入小说标题" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="简介">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="简单介绍一下故事内容（可选）"
            maxlength="300"
          />
        </el-form-item>
        <el-form-item label="封面图片 URL">
          <el-input v-model="form.coverUrl" placeholder="留空将使用默认封面（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { fetchMyNovels, createNovel } from '../../api/author'

const router = useRouter()

const novels = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = ref({ title: '', description: '', coverUrl: '' })

const rules = {
  title: [{ required: true, message: '请输入小说标题', trigger: 'blur' }]
}

const loadNovels = async () => {
  loading.value = true
  try {
    novels.value = await fetchMyNovels()
  } catch (err) {
    ElMessage.error('作品列表加载失败')
    console.error(err)
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  dialogVisible.value = true
}

const resetForm = () => {
  form.value = { title: '', description: '', coverUrl: '' }
  formRef.value?.clearValidate()
}

const submitCreate = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const novel = await createNovel({
        title: form.value.title,
        description: form.value.description,
        coverUrl: form.value.coverUrl
      })
      ElMessage.success('小说创建成功')
      dialogVisible.value = false
      // 创建完成直接进入章节管理，作者可以马上新增章节草稿
      router.push(`/author/novel/${novel.id}`)
    } catch (err) {
      ElMessage.error(err.response?.data?.message || '创建失败，请重试')
      console.error(err)
    } finally {
      submitting.value = false
    }
  })
}

const manageNovel = (id) => {
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
}

.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
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

.btn-icon {
  margin-right: 4px;
}

.novel-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.novel-card {
  overflow: hidden;
  background: #fff;
}

.card-image {
  height: 180px;
  background-size: cover;
  background-position: center;
}

.card-body {
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.novel-title {
  font-size: 1.2rem;
  margin-bottom: 8px;
}

.novel-desc {
  color: var(--text-sub);
  font-size: 0.9rem;
  line-height: 1.6;
  min-height: 44px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 16px;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  border-top: 1px solid var(--border-color);
  padding-top: 14px;
}

.created-at {
  font-size: 0.8rem;
  color: var(--slate-400);
}
</style>
