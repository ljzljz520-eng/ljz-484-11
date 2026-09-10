package com.novel.model;

import java.time.LocalDateTime;

public class Novel {
    private Long id;
    private String title;
    private String description;
    private String coverUrl;
    /** 作者身份标识：作者后台接口据此隔离不同作者的作品与草稿 */
    private String authorId;
    private LocalDateTime createdAt;

    public Novel() {}

    public Novel(Long id, String title, String description, String coverUrl, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.coverUrl = coverUrl;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
