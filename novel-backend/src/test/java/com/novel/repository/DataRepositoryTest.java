package com.novel.repository;

import com.novel.model.Chapter;
import com.novel.model.Novel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataRepositoryTest {

    private DataRepository repository;

    @BeforeEach
    void setUp() {
        repository = new DataRepository();
        repository.init(); // 手动触发种子数据
    }

    @Test
    void seededChaptersArePublishedAndVisible() {
        // 种子章节全部为已发布，读者侧可见
        assertFalse(repository.findPublishedChaptersByNovelId(1L).isEmpty());
        assertEquals(repository.findAllChaptersByNovelId(1L).size(),
                repository.findPublishedChaptersByNovelId(1L).size());
    }

    @Test
    void draftChapterIsHiddenFromReaders() {
        Chapter draft = new Chapter();
        draft.setNovelId(1L);
        draft.setTitle("草稿章节");
        draft.setContent("未发布的内容");
        draft.setOrderNo(repository.nextOrderNo(1L));
        draft.setStatus(Chapter.STATUS_DRAFT);
        repository.saveChapter(draft);

        // 作者后台可见
        assertTrue(repository.findAllChaptersByNovelId(1L).stream()
                .anyMatch(c -> c.getId().equals(draft.getId())));
        // 读者目录不可见
        assertFalse(repository.findPublishedChaptersByNovelId(1L).stream()
                .anyMatch(c -> c.getId().equals(draft.getId())));
        // 读者按 ID 读取草稿返回 null
        assertNull(repository.findPublishedChapterById(draft.getId()));
    }

    @Test
    void publishedDraftBecomesVisible() {
        Chapter draft = new Chapter();
        draft.setNovelId(1L);
        draft.setTitle("待发布");
        draft.setContent("内容");
        draft.setOrderNo(repository.nextOrderNo(1L));
        draft.setStatus(Chapter.STATUS_DRAFT);
        repository.saveChapter(draft);

        repository.publishChapter(draft.getId());

        assertEquals(Chapter.STATUS_PUBLISHED, repository.findChapterById(draft.getId()).getStatus());
        assertNotNull(repository.findPublishedChapterById(draft.getId()));
    }

    @Test
    void updateChapterChangesTitleAndContent() {
        Chapter chapter = repository.findAllChaptersByNovelId(1L).get(0);
        repository.updateChapter(chapter.getId(), "新标题", "新正文");

        Chapter updated = repository.findChapterById(chapter.getId());
        assertEquals("新标题", updated.getTitle());
        assertEquals("新正文", updated.getContent());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    void saveNovelAssignsId() {
        Novel novel = new Novel();
        novel.setTitle("新书");
        novel.setDescription("简介");
        Novel saved = repository.saveNovel(novel);

        assertNotNull(saved.getId());
        assertEquals(saved, repository.findNovelById(saved.getId()));
    }

    @Test
    void nextOrderNoIncrements() {
        int next = repository.nextOrderNo(1L);
        assertEquals(4, next); // 种子数据已有 3 章
    }
}
