package com.novel.controller;

import com.novel.dto.ChapterRequest;
import com.novel.dto.NovelRequest;
import com.novel.model.Chapter;
import com.novel.model.Novel;
import com.novel.repository.DataRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 作者后台接口：小说创建、章节草稿的保存与查询。
 * 与读者侧 {@link NovelController} 隔离，草稿章节仅在此暴露。
 */
@RestController
@RequestMapping("/api/author")
@CrossOrigin(origins = "*") // Allow frontend to access
@Tag(name = "Author Backend API", description = "作者后台：章节草稿管理")
public class AuthorController {

    private final DataRepository dataRepository;

    public AuthorController(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    // ---------- 小说 ----------

    @PostMapping("/novels")
    @Operation(summary = "Create Novel (author)")
    public Novel createNovel(@RequestBody NovelRequest request) {
        if (request == null || !StringUtils.hasText(request.getTitle())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Novel title is required");
        }
        Novel novel = new Novel();
        novel.setTitle(request.getTitle());
        novel.setDescription(request.getDescription() == null ? "" : request.getDescription());
        novel.setCoverUrl(StringUtils.hasText(request.getCoverUrl())
                ? request.getCoverUrl()
                : "https://images.unsplash.com/photo-1512820790803-83ca734da794?q=80&w=800&auto=format&fit=crop");
        return dataRepository.saveNovel(novel);
    }

    @GetMapping("/novels")
    @Operation(summary = "List My Novels (author)")
    public List<Novel> listMyNovels() {
        // 无登录体系，作者后台暂返回全部小说
        return dataRepository.findAllNovels(null, 1, 1000);
    }

    // ---------- 章节草稿 ----------

    @GetMapping("/novels/{novelId}/chapters")
    @Operation(summary = "List Chapters Including Drafts (author)")
    public List<Chapter> listChaptersForAuthor(@PathVariable Long novelId) {
        requireNovel(novelId);
        return dataRepository.findAllChaptersByNovelId(novelId);
    }

    @PostMapping("/novels/{novelId}/chapters")
    @Operation(summary = "Create Chapter Draft (author)")
    public Chapter createChapter(@PathVariable Long novelId, @RequestBody ChapterRequest request) {
        requireNovel(novelId);
        Chapter chapter = new Chapter();
        chapter.setNovelId(novelId);
        chapter.setTitle(resolveTitle(request));
        chapter.setContent(request == null || request.getContent() == null ? "" : request.getContent());
        chapter.setOrderNo(dataRepository.nextOrderNo(novelId));
        chapter.setStatus(Chapter.STATUS_DRAFT); // 新章节默认保存为草稿
        return dataRepository.saveChapter(chapter);
    }

    @GetMapping("/chapters/{id}")
    @Operation(summary = "Get Chapter for Editing (author, drafts included)")
    public Chapter getChapterForAuthor(@PathVariable Long id) {
        Chapter chapter = dataRepository.findChapterById(id);
        if (chapter == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Chapter not found");
        }
        return chapter;
    }

    @PutMapping("/chapters/{id}")
    @Operation(summary = "Save Chapter Draft (update title/content)")
    public Chapter saveChapterDraft(@PathVariable Long id, @RequestBody ChapterRequest request) {
        if (request == null || !StringUtils.hasText(request.getTitle())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chapter title is required");
        }
        Chapter chapter = dataRepository.updateChapter(id, request.getTitle(),
                request.getContent() == null ? "" : request.getContent());
        if (chapter == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Chapter not found");
        }
        return chapter;
    }

    @PostMapping("/chapters/{id}/publish")
    @Operation(summary = "Publish Chapter (author)")
    public Chapter publishChapter(@PathVariable Long id) {
        Chapter chapter = dataRepository.publishChapter(id);
        if (chapter == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Chapter not found");
        }
        return chapter;
    }

    private Novel requireNovel(Long novelId) {
        Novel novel = dataRepository.findNovelById(novelId);
        if (novel == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Novel not found");
        }
        return novel;
    }

    private String resolveTitle(ChapterRequest request) {
        if (request == null || !StringUtils.hasText(request.getTitle())) {
            return "未命名章节";
        }
        return request.getTitle();
    }
}
