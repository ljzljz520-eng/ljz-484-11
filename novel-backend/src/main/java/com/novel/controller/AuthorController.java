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
 * 无登录体系，通过 X-Author-Id 请求头标识作者身份，
 * 所有接口仅允许操作当前作者本人的小说与章节。
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
    public Novel createNovel(@RequestHeader(value = "X-Author-Id", required = false) String authorId,
                             @RequestBody NovelRequest request) {
        String author = requireAuthor(authorId);
        if (request == null || !StringUtils.hasText(request.getTitle())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Novel title is required");
        }
        Novel novel = new Novel();
        novel.setTitle(request.getTitle());
        novel.setDescription(request.getDescription() == null ? "" : request.getDescription());
        novel.setCoverUrl(StringUtils.hasText(request.getCoverUrl())
                ? request.getCoverUrl()
                : "https://images.unsplash.com/photo-1512820790803-83ca734da794?q=80&w=800&auto=format&fit=crop");
        novel.setAuthorId(author); // 作品归属当前作者
        return dataRepository.saveNovel(novel);
    }

    @GetMapping("/novels")
    @Operation(summary = "List My Novels (author)")
    public List<Novel> listMyNovels(@RequestHeader(value = "X-Author-Id", required = false) String authorId) {
        // 仅返回当前作者本人的小说，作者之间互不可见
        return dataRepository.findNovelsByAuthorId(requireAuthor(authorId));
    }

    // ---------- 章节草稿 ----------

    @GetMapping("/novels/{novelId}/chapters")
    @Operation(summary = "List Chapters Including Drafts (author)")
    public List<Chapter> listChaptersForAuthor(@RequestHeader(value = "X-Author-Id", required = false) String authorId,
                                               @PathVariable Long novelId) {
        requireOwnedNovel(novelId, requireAuthor(authorId));
        return dataRepository.findAllChaptersByNovelId(novelId);
    }

    @PostMapping("/novels/{novelId}/chapters")
    @Operation(summary = "Create Chapter Draft (author)")
    public Chapter createChapter(@RequestHeader(value = "X-Author-Id", required = false) String authorId,
                                 @PathVariable Long novelId, @RequestBody ChapterRequest request) {
        requireOwnedNovel(novelId, requireAuthor(authorId));
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
    public Chapter getChapterForAuthor(@RequestHeader(value = "X-Author-Id", required = false) String authorId,
                                       @PathVariable Long id) {
        return requireOwnedChapter(id, requireAuthor(authorId));
    }

    @PutMapping("/chapters/{id}")
    @Operation(summary = "Save Chapter Draft (update title/content)")
    public Chapter saveChapterDraft(@RequestHeader(value = "X-Author-Id", required = false) String authorId,
                                    @PathVariable Long id, @RequestBody ChapterRequest request) {
        requireOwnedChapter(id, requireAuthor(authorId));
        if (request == null || !StringUtils.hasText(request.getTitle())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chapter title is required");
        }
        return dataRepository.updateChapter(id, request.getTitle(),
                request.getContent() == null ? "" : request.getContent());
    }

    @PostMapping("/chapters/{id}/publish")
    @Operation(summary = "Publish Chapter (author)")
    public Chapter publishChapter(@RequestHeader(value = "X-Author-Id", required = false) String authorId,
                                  @PathVariable Long id) {
        requireOwnedChapter(id, requireAuthor(authorId));
        return dataRepository.publishChapter(id);
    }

    // ---------- 身份与归属校验 ----------

    /** 缺少作者身份头时拒绝请求 */
    private String requireAuthor(String authorId) {
        if (!StringUtils.hasText(authorId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing X-Author-Id header");
        }
        return authorId.trim();
    }

    /** 小说不存在返回 404，不属于当前作者返回 403 */
    private Novel requireOwnedNovel(Long novelId, String authorId) {
        Novel novel = dataRepository.findNovelById(novelId);
        if (novel == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Novel not found");
        }
        if (!authorId.equals(novel.getAuthorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission to access this novel");
        }
        return novel;
    }

    /** 章节不存在返回 404，所属小说不属于当前作者返回 403 */
    private Chapter requireOwnedChapter(Long chapterId, String authorId) {
        Chapter chapter = dataRepository.findChapterById(chapterId);
        if (chapter == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Chapter not found");
        }
        requireOwnedNovel(chapter.getNovelId(), authorId);
        return chapter;
    }

    private String resolveTitle(ChapterRequest request) {
        if (request == null || !StringUtils.hasText(request.getTitle())) {
            return "未命名章节";
        }
        return request.getTitle();
    }
}
