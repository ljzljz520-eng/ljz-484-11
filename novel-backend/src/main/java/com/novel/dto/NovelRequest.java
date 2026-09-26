package com.novel.dto;

/**
 * 作者创建小说时提交的数据
 */
public record NovelRequest(String title, String description, String coverUrl) {
}
