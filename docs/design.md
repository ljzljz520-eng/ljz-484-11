# 设计文档 (Design)

## 1. 设计理念
本项目遵循“现代、简洁、高级”的设计原则。采用浅色系主题，配合毛玻璃效果 (Glassmorphism) 和优雅的微交互。

## 2. 视觉规范
- **色彩系统**:
  - 主背景色: `#f8fafc` (Slate 50)
  - 强调色: `#6366f1` (Indigo 600)
  - 文字主色: `#1e293b` (Slate 800)
  - 文字辅助色: `#64748b` (Slate 500)
- **字体**:
  - 系统 UI: Inter, sans-serif
  - 阅读正文: Merriweather, serif (提升长文阅读体验)
- **组件样式**:
  - 圆角: 16px (大圆角设计，增强现代感)
  - 阴影: 柔和的扩散阴影，增加层次感。

## 3. 关键页面设计
- **首页**: 沉浸式英雄区 (Hero Section)，双色渐变标题，列表卡片采用悬浮提升效果。
- **详情页**: 动态背景模糊设计，根据小说封面自动生成氛围背景。
- **阅读页**: 经典的“护眼纸质”配色 (`#fcf6e5`)，无干扰布局。
- **作者后台**: `/author` 作品列表与创建小说入口；章节管理页以表格展示草稿/已发布状态；章节编辑器支持修改标题正文、保存草稿与发布。

## 4. 接口设计 (RESTful API)
- `GET /api/novels`: 获取小说列表（支持分页与搜索）。
- `GET /api/novels/{id}`: 获取小说详细信息及**已发布**章节目录。
- `GET /api/chapters/{id}`: 获取具体章节正文内容（草稿在公开接口返回 404）。

### 作者后台接口 (`/api/author/**`)
- `GET /api/author/novels` / `POST /api/author/novels`: 作品列表 / 创建小说。
- `GET /api/author/novels/{id}/chapters`: 章节列表，**包含草稿**。
- `POST /api/author/novels/{id}/chapters`: 新增章节草稿。
- `GET /api/author/chapters/{id}`: 查询章节（草稿与已发布均可读）。
- `PUT /api/author/chapters/{id}`: 修改章节标题与正文（不改变发布状态）。
- `POST /api/author/chapters/{id}/publish`: 发布章节。

## 5. 数据模型
- **Novel (小说)**: ID, Title, Description, CoverUrl, CreatedAt.
- **Chapter (章节)**: ID, NovelId, Title, OrderNo, Content, Status, CreatedAt, UpdatedAt.
  - Status 取值 `DRAFT`（草稿，仅作者后台可见）/ `PUBLISHED`（已发布，公开可读）。
  - 种子章节默认为 PUBLISHED；作者新建章节默认为 DRAFT，序号自动递增。
