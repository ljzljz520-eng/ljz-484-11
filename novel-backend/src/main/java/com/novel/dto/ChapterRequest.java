package com.novel.dto;

/**
 * Request body for creating / updating a chapter draft.
 * status is optional and accepts DRAFT or PUBLISHED (case-insensitive).
 */
public class ChapterRequest {
    private String title;
    private String content;
    private String status;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
