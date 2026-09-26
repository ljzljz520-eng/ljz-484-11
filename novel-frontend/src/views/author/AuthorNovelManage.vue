<template>
  <div class="container manage-page" v-loading="loading">
    <el-button plain class="back-btn" @click="goBack">
      <el-icon><ArrowLeft /></el-icon>&nbsp;返回作品列表
    </el-button>

    <template v-if="novel">
      <div class="novel-banner glass-panel">
        <img :src="novel.coverUrl" class="cover" />
        <div class="banner-info">
          <h1 class="novel-title">{{ novel.title }}</h1>
          <p class="novel-desc">{{ novel.description || '暂无简介' }}</p>
          <div class="banner-stats">
            <el-tag type="success" effect="light">已发布 {{ publishedCount }}</el-tag>
            <el-tag type="info" effect="light">草稿 {{ draftCount }}</el-tag>
          </div>
          <el-button type="primary" round class="add-btn" @click="addChapter">
            <el-icon class="btn-icon"><Plus /></el-icon>新增章节
          </el-button>
        </div>
      </div>

      <div class="chapter-panel glass-panel">
        <h2 class="panel-title">章节管理</h2>
        <el-table :data="chapters" style="width: 100%">
          <el-table-column label="序号" width="90">
            <template #default="{ row }">{{ formatNumber(row.orderNo) }}</template>
          </el-table-column>
          <el-table-column label="章节标题" prop="title" min-width="220">
            <template #default="{ row }">
              <span class="chapter-title-text">{{ row.title }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag v-if="row.status === 'PUBLISHED'" type="success" size="small">已发布</el-tag>
              <el-tag v-else type="warning" size="small">草稿</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="最后修改" width="180">
            <template #default="{ row }">{{ formatDateTime(row.updatedAt || row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="editChapter(row.id)">编辑</el-button>
              <el-button
                v-if="row.status === 'DRAFT'"
                link
                type="success"
                :loading="publishingId === row.id"
                @click="publish(row)"
              >
                发布
              </el-button>
              <el-button v-else link disabled>已上线</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="chapters.length === 0" description="还没有章节，点击「新增章节」开始写作" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import {
  fetchAuthorNovel,
  fetchAuthorChapters,
  createChapterDraft,
  publishChapter
} from '../../api/author'

const route = useRoute()
const router = useRouter()

const novel = ref(null)
const chapters = ref([])
const loading = ref(true)
const publishingId = ref(null)

const novelId = route.params.id

const publishedCount = computed(() => chapters.value.filter((c) => c.status === 'PUBLISHED').length)
const draftCount = computed(() => chapters.value.filter((c) => c.status === 'DRAFT').length)

const load = async () => {
  loading.value = true
  try {
        const [novelData, chapterData] = await Promise.all([
          fetchAuthorNovel(novelId),
          fetchAuthorChapters(novelId)
        ])
        novel.value = novelData
        chapters.value = chapterData
  } catch (err) {
    if (err.response?.status === 404) {
      ElMessage.error('小说不存在')
      router.replace('/author')
    } else {
      ElMessage.error('加载失败')
    }
    console.error(err)
  } finally {
    loading.value = false
  }
}

// 新增章节：先创建一份空白草稿，再跳转编辑器修改标题和正文
const addChapter = async () => {
  try {
    const chapter = await createChapterDraft(novelId, { title: '', content: '' })
    router.push(`/author/novel/${novelId}/chapter/${chapter.id}`)
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '新增章节失败')
    console.error(err)
  }
}

const editChapter = (id) => {
  router.push(`/author/novel/${novelId}/chapter/${id}`)
}

const publish = async (row) => {
  try {
    await ElMessageBox.confirm(`确认发布「${row.title}」吗？发布后读者将可以看到该章节。`, '发布章节', {
      confirmButtonText: '发布',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  publishingId.value = row.id
  try {
    const updated = await publishChapter(row.id)
    Object.assign(row, updated)
    ElMessage.success('章节已发布')
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '发布失败')
    console.error(err)
  } finally {
    publishingId.value = null
  }
}

const goBack = () => {
  router.push('/author')
}

const formatNumber = (num) => (num == null ? '-' : num.toString().padStart(2, '0'))

const formatDateTime = (val) => {
  if (!val) return ''
  return new Date(val).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(load)
</script>

<style scoped>
.manage-page {
  padding-top: 30px;
}

.back-btn {
  margin-bottom: 20px;
}

.novel-banner {
  display: flex;
  gap: 30px;
  padding: 30px;
  background: #fff;
  margin-bottom: 24px;
}

.cover {
  width: 140px;
  height: 190px;
  object-fit: cover;
  border-radius: 10px;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.15);
}

.banner-info {
  flex: 1;
}

.novel-title {
  font-size: 1.8rem;
  margin-bottom: 10px;
}

.novel-desc {
  color: var(--text-sub);
  line-height: 1.7;
  margin-bottom: 16px;
}

.banner-stats {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.add-btn {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
}

.btn-icon {
  margin-right: 4px;
}

.chapter-panel {
  padding: 30px;
  background: #fff;
}

.panel-title {
  font-size: 1.3rem;
  margin-bottom: 20px;
  padding-left: 10px;
  border-left: 4px solid var(--primary-color);
}

.chapter-title-text {
  font-weight: 500;
}

@media (max-width: 768px) {
  .novel-banner {
    flex-direction: column;
  }
  .cover {
    width: 120px;
    height: 165px;
  }
}
</style>
