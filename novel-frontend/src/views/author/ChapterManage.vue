<template>
  <div class="container manage-page" v-loading="loading">
    <el-button @click="goBack" circle plain icon="ArrowLeft" class="back-btn"></el-button>

    <div v-if="novel" class="page-header glass-panel">
      <img :src="novel.coverUrl" class="novel-cover" alt="cover" />
      <div class="header-info">
        <h1 class="page-title">{{ novel.title }}</h1>
        <p class="page-subtitle">
          共 {{ chapters.length }} 章 · 已发布 {{ publishedCount }} 章 · 草稿 {{ draftCount }} 篇
        </p>
      </div>
      <el-button type="primary" size="large" round icon="Plus" @click="createChapter">
        新增章节
      </el-button>
    </div>

    <div class="chapter-table glass-panel">
      <div class="table-head table-row">
        <span class="col-no">序号</span>
        <span class="col-title">章节标题</span>
        <span class="col-status">状态</span>
        <span class="col-time">最近更新</span>
        <span class="col-actions">操作</span>
      </div>
      <div v-for="chapter in chapters" :key="chapter.id" class="table-row">
        <span class="col-no">{{ formatNumber(chapter.orderNo) }}</span>
        <span class="col-title">{{ chapter.title }}</span>
        <span class="col-status">
          <el-tag :type="chapter.status === 'PUBLISHED' ? 'success' : 'warning'" size="small" effect="light">
            {{ chapter.status === 'PUBLISHED' ? '已发布' : '草稿' }}
          </el-tag>
        </span>
        <span class="col-time">{{ formatTime(chapter.updatedAt) }}</span>
        <span class="col-actions">
          <el-button size="small" round @click="editChapter(chapter.id)">编辑</el-button>
          <el-button
            v-if="chapter.status !== 'PUBLISHED'"
            size="small"
            type="success"
            round
            :loading="publishingId === chapter.id"
            @click="handlePublish(chapter)"
          >
            发布
          </el-button>
        </span>
      </div>
      <el-empty v-if="!loading && chapters.length === 0" description="暂无章节，点击右上角新增第一章" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchNovelDetail, fetchAuthorChapters, createChapterDraft, publishChapter } from '../../api'

const route = useRoute()
const router = useRouter()
const novelId = route.params.id

const novel = ref(null)
const chapters = ref([])
const loading = ref(true)
const publishingId = ref(null)

const publishedCount = computed(() => chapters.value.filter(c => c.status === 'PUBLISHED').length)
const draftCount = computed(() => chapters.value.filter(c => c.status !== 'PUBLISHED').length)

const loadData = async () => {
  loading.value = true
  try {
    const [detailRes, chaptersRes] = await Promise.all([
      fetchNovelDetail(novelId),
      fetchAuthorChapters(novelId) // 作者后台接口返回含草稿的全部章节
    ])
    novel.value = detailRes.data.novel
    chapters.value = chaptersRes.data
  } catch (err) {
    console.error(err)
    ElMessage.error('加载章节列表失败')
  } finally {
    loading.value = false
  }
}

const createChapter = async () => {
  try {
    const res = await createChapterDraft(novelId, { title: '', content: '' })
    ElMessage.success('已创建草稿，开始写作吧')
    router.push(`/author/novel/${novelId}/chapters/${res.data.id}/edit`)
  } catch (err) {
    console.error(err)
    ElMessage.error('创建章节失败')
  }
}

const editChapter = (chapterId) => {
  router.push(`/author/novel/${novelId}/chapters/${chapterId}/edit`)
}

const handlePublish = async (chapter) => {
  try {
    await ElMessageBox.confirm(
      `发布后「${chapter.title}」将对所有读者可见，确认发布？`,
      '发布章节',
      { confirmButtonText: '发布', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return // 用户取消
  }
  publishingId.value = chapter.id
  try {
    await publishChapter(chapter.id)
    ElMessage.success('发布成功')
    await loadData()
  } catch (err) {
    console.error(err)
    ElMessage.error('发布失败，请稍后重试')
  } finally {
    publishingId.value = null
  }
}

const goBack = () => {
  router.push('/author')
}

const formatNumber = (num) => {
  return String(num).padStart(2, '0')
}

const formatTime = (val) => {
  if (!val) return ''
  return new Date(val).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

onMounted(loadData)
</script>

<style scoped>
.manage-page {
  padding-top: 40px;
  padding-bottom: 60px;
}

.back-btn {
  margin-bottom: 20px;
  background: white;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.page-header {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 24px 32px;
  background: white;
  margin-bottom: 24px;
}

.novel-cover {
  width: 56px;
  height: 76px;
  object-fit: cover;
  border-radius: 6px;
  flex-shrink: 0;
}

.header-info {
  flex: 1;
  min-width: 0;
}

.page-title {
  font-size: 1.6rem;
  margin: 0 0 6px;
}

.page-subtitle {
  color: var(--text-sub);
  margin: 0;
  font-size: 0.9rem;
}

.chapter-table {
  background: white;
  padding: 12px 24px;
}

.table-row {
  display: grid;
  grid-template-columns: 60px 1fr 90px 140px 170px;
  align-items: center;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid var(--slate-100);
}

.table-row:last-child {
  border-bottom: none;
}

.table-head {
  font-size: 0.85rem;
  color: var(--slate-400);
  font-weight: 600;
  padding: 10px 0;
}

.col-no {
  font-family: 'Space Mono', monospace;
  color: var(--slate-400);
}

.col-title {
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.col-time {
  font-size: 0.85rem;
  color: var(--text-sub);
}

.col-actions {
  display: flex;
  gap: 8px;
}

@media (max-width: 768px) {
  .table-row {
    grid-template-columns: 40px 1fr 80px;
  }
  .col-time,
  .table-head .col-time,
  .table-head .col-actions {
    display: none;
  }
}
</style>
