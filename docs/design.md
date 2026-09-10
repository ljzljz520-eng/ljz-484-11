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

## 4. 接口设计 (RESTful API)

### 4.1 读者侧公开接口（仅已发布内容）
- `GET /api/novels`: 获取小说列表（支持分页与搜索）。
- `GET /api/novels/{id}`: 获取小说详细信息及章节目录（目录仅含已发布章节）。
- `GET /api/novels/{id}/chapters`: 获取某小说的已发布章节列表。
- `GET /api/chapters/{id}`: 获取具体章节正文内容（草稿返回 404）。

### 4.2 作者后台接口（`/api/author`，含草稿）
作者后台接口通过 `X-Author-Id` 请求头标识作者身份（前端为每个浏览器生成并持久化一个随机 ID），
仅允许操作当前作者本人的小说与章节：缺少身份头返回 400，资源不存在返回 404，属于他人返回 403。
- `POST /api/author/novels`: 创建小说（归属当前作者）。
- `GET /api/author/novels`: 当前作者的小说列表（不含他人作品与种子数据）。
- `GET /api/author/novels/{novelId}/chapters`: 章节列表（含草稿，仅本人小说）。
- `POST /api/author/novels/{novelId}/chapters`: 新增章节（默认保存为草稿，仅本人小说）。
- `GET /api/author/chapters/{id}`: 获取章节（含草稿，用于编辑，仅本人章节）。
- `PUT /api/author/chapters/{id}`: 保存草稿（修改标题与正文，仅本人章节）。
- `POST /api/author/chapters/{id}/publish`: 发布章节，发布后读者可见（仅本人章节）。

## 5. 数据模型
- **Novel (小说)**: ID, Title, Description, CoverUrl, AuthorId, CreatedAt.
- **Chapter (章节)**: ID, NovelId, Title, OrderNo, Content, Status (`DRAFT`/`PUBLISHED`), CreatedAt, UpdatedAt.
  - `DRAFT`（草稿）仅作者后台可见；`PUBLISHED`（已发布）对读者可见。
