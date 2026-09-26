package com.novel.model;

/**
 * Chapter publication status.
 * DRAFT chapters are only visible in the author studio,
 * PUBLISHED chapters are visible to all readers.
 */
public enum ChapterStatus {
    DRAFT,
    PUBLISHED;

    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        for (ChapterStatus status : values()) {
            if (status.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
