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

### 4.1 公共阅读接口（仅暴露已发布内容）
- `GET /api/novels`: 获取小说列表（支持分页与搜索）。
- `GET /api/novels/{id}`: 获取小说详细信息及**已发布**章节目录。
- `GET /api/novels/{id}/chapters`: 获取小说的已发布章节列表。
- `GET /api/chapters/{id}`: 获取已发布章节正文内容（草稿返回 404）。

### 4.2 作者后台接口（`/api/author`，含草稿）
- `POST /api/author/novels`: 创建小说（标题必填，封面留空用默认图）。
- `GET /api/author/novels/{novelId}/chapters`: 查询小说全部章节（含草稿）。
- `GET /api/author/chapters/{id}`: 查询单个章节（含草稿，用于编辑回显）。
- `POST /api/author/novels/{novelId}/chapters`: 新增章节（可存草稿或直接发布）。
- `PUT /api/author/chapters/{id}`: 修改章节标题/正文（可选同时改状态）。
- `PUT /api/author/chapters/{id}/status?status=`: 发布 / 撤回章节。
- `DELETE /api/author/chapters/{id}`: 删除章节。

### 4.3 统一错误响应
所有接口异常由 `GlobalExceptionHandler` 统一处理，响应体为
`{ "status": 400, "message": "...", "timestamp": "..." }`。

## 5. 数据模型
- **Novel (小说)**: ID, Title, Description, CoverUrl, CreatedAt.
- **Chapter (章节)**: ID, NovelId, Title, OrderNo, Content, Status (`DRAFT`/`PUBLISHED`), CreatedAt, UpdatedAt.
  - `DRAFT`（草稿）章节仅作者后台可见；`PUBLISHED`（已发布）章节对读者可见。
