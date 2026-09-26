package com.novel.exception;

/** 请求的资源不存在（或当前视角不可见，例如公开访问未发布草稿） */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
