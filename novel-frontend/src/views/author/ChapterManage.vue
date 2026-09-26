<template>
  <div class="container manage-page" v-loading="loading">
    <div class="top-bar">
      <el-button circle plain :icon="ArrowLeft" @click="goBack" class="back-btn"></el-button>
      <div class="novel-meta" v-if="novel">
        <h1 class="page-title">{{ novel.title }}</h1>
        <p class="page-subtitle">章节管理 · 共 {{ chapters.length }} 章（草稿 {{ draftCount }} 章）</p>
      </div>
      <el-button type="primary" round :icon="Plus" class="new-btn" @click="goNewChapter">
        新建章节
      </el-button>
    </div>

    <div class="chapter-table glass-panel">
      <el-table :data="chapters" style="width: 100%">
        <el-table-column label="序号" width="70" align="center">
          <template #default="{ row }">
            <span class="order-no">{{ formatNumber(row.orderNo) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="章节标题" min-width="220">
          <template #default="{ row }">
            <span class="chapter-title">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'warning'" effect="light" round>
              {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最近更新" width="170">
          <template #default="{ row }">
            <span class="update-time">{{ formatTime(row.updatedAt || row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="right">
          <template #default="{ row }">
            <el-button size="small" round @click="goEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status !== 'PUBLISHED'"
              size="small"
              type="success"
              round
              @click="handlePublish(row)"
            >发布</el-button>
            <el-button
              v-else
              size="small"
              type="warning"
              round
              plain
              @click="handleUnpublish(row)"
            >撤回</el-button>
            <el-button size="small" type="danger" round plain @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无章节，点击右上角新建第一章" />
        </template>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import {
  fetchNovelDetail,
  fetchAuthorChapters,
  updateChapterStatus,
  deleteChapter,
  errorMessage
} from '../../api'

const route = useRoute()
const router = useRouter()
const novelId = route.params.novelId

const novel = ref(null)
const chapters = ref([])
const loading = ref(true)

const draftCount = computed(() => chapters.value.filter(c => c.status !== 'PUBLISHED').length)

const loadData = async () => {
  loading.value = true
  try {
    const [detailRes, chaptersRes] = await Promise.all([
      fetchNovelDetail(novelId),
      fetchAuthorChapters(novelId)
    ])
    novel.value = detailRes.data.novel
    chapters.value = chaptersRes.data
  } catch (err) {
    ElMessage.error(errorMessage(err))
  } finally {
    loading.value = false
  }
}

const goBack = () => router.push('/author')
const goNewChapter = () => router.push(`/author/novels/${novelId}/chapters/new`)
const goEdit = (row) => router.push(`/author/chapters/${row.id}/edit`)

const handlePublish = async (row) => {
  try {
    await updateChapterStatus(row.id, 'PUBLISHED')
    ElMessage.success(`「${row.title}」已发布，读者现在可以看到`)
    loadData()
  } catch (err) {
    ElMessage.error(errorMessage(err))
  }
}

const handleUnpublish = async (row) => {
  try {
    await ElMessageBox.confirm(
      `撤回后「${row.title}」将变为草稿，读者无法继续阅读该章。确定撤回吗？`,
      '撤回章节',
      { confirmButtonText: '确定撤回', cancelButtonText: '取消', type: 'warning' }
    )
    await updateChapterStatus(row.id, 'DRAFT')
    ElMessage.success('已撤回为草稿')
    loadData()
  } catch (err) {
    if (err !== 'cancel') ElMessage.error(errorMessage(err))
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除「${row.title}」吗？删除后不可恢复。`,
      '删除章节',
      { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'error' }
    )
    await deleteChapter(row.id)
    ElMessage.success('章节已删除')
    loadData()
  } catch (err) {
    if (err !== 'cancel') ElMessage.error(errorMessage(err))
  }
}

const formatNumber = (num) => String(num).padStart(2, '0')

const formatTime = (val) => {
  if (!val) return ''
  return new Date(val).toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
  })
}

onMounted(loadData)
</script>

<style scoped>
.manage-page {
  padding-top: 40px;
  padding-bottom: 60px;
}

.top-bar {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
}

.back-btn {
  background: white;
  flex-shrink: 0;
}

.novel-meta {
  flex: 1;
  min-width: 0;
}

.page-title {
  font-size: 1.8rem;
  margin-bottom: 4px;
}

.page-subtitle {
  color: var(--text-sub);
  margin: 0;
  font-size: 0.9rem;
}

.new-btn {
  flex-shrink: 0;
}

.chapter-table {
  background: white;
  padding: 10px 20px 20px;
  overflow: hidden;
}

.order-no {
  font-family: 'Space Mono', monospace;
  color: var(--slate-400);
}

.chapter-title {
  font-weight: 500;
}

.update-time {
  color: var(--text-sub);
  font-size: 0.85rem;
}

:deep(.el-table) {
  --el-table-header-bg-color: var(--slate-50);
  --el-table-border-color: var(--slate-200);
}

@media (max-width: 640px) {
  .top-bar {
    flex-wrap: wrap;
  }
}
</style>
