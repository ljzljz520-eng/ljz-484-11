package com.novel.model;

/**
 * 章节发布状态。
 * DRAFT: 草稿，仅作者后台 (/api/author/**) 可见；
 * PUBLISHED: 已发布，公开读者接口可见。
 */
public enum ChapterStatus {
    DRAFT,
    PUBLISHED
}
