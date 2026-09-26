package com.novel.dto;

/**
 * 作者保存章节草稿时提交的数据。
 * 章节状态不由调用方传入，发布走专用接口。
 */
public record ChapterRequest(String title, String content) {
}
