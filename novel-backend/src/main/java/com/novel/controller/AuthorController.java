package com.novel.controller;

import com.novel.dto.ChapterRequest;
import com.novel.dto.NovelRequest;
import com.novel.exception.BadRequestException;
import com.novel.exception.NotFoundException;
import com.novel.model.Chapter;
import com.novel.model.ChapterStatus;
import com.novel.model.Novel;
import com.novel.repository.DataRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * 作者后台接口。
 * 演示版没有接入登录鉴权，作者的全部作品/草稿都通过 /api/author/** 访问；
 * 公开阅读接口 (/api/novels, /api/chapters) 永远不会返回草稿。
 */
@RestController
@RequestMapping("/api/author")
@CrossOrigin(origins = "*")
@Tag(name = "Author Console API")
public class AuthorController {

    private static final String DEFAULT_COVER_URL =
            "https://images.unsplash.com/photo-1457369804613-52c61a468e7d?q=80&w=800&auto=format&fit=crop";

    private final DataRepository dataRepository;

    public AuthorController(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @GetMapping("/novels")
    @Operation(summary = "List author's novels (author console)")
    public List<Novel> listMyNovels() {
        return dataRepository.findAllNovels(null, 1, Integer.MAX_VALUE).stream()
                .sorted(Comparator.comparing(Novel::getId).reversed())
                .toList();
    }

    @PostMapping("/novels")
    @Operation(summary = "Create a new novel")
    public Novel createNovel(@RequestBody NovelRequest request) {
        if (request == null || StringUtils.isBlank(request.title())) {
            throw new BadRequestException("小说标题不能为空");
        }
        Novel novel = new Novel(
                null,
                request.title().trim(),
                StringUtils.defaultString(request.description()),
                StringUtils.defaultIfBlank(request.coverUrl(), DEFAULT_COVER_URL),
                LocalDateTime.now());
        return dataRepository.saveNovel(novel);
    }

    @GetMapping("/novels/{id}")
    @Operation(summary = "Get novel detail with ALL chapters (incl. drafts)")
    public Novel getNovel(@PathVariable Long id) {
        Novel novel = dataRepository.findNovelById(id);
        if (novel == null) {
            throw new NotFoundException("小说不存在");
        }
        return novel;
    }

    @GetMapping("/novels/{id}/chapters")
    @Operation(summary = "List all chapters of a novel, including drafts")
    public List<Chapter> listChapters(@PathVariable Long id) {
        requireNovel(id);
        return dataRepository.findAllChaptersByNovelId(id);
    }

    @PostMapping("/novels/{id}/chapters")
    @Operation(summary = "Create a new chapter draft (title and content editable)")
    public Chapter createChapter(@PathVariable Long id, @RequestBody(required = false) ChapterRequest request) {
        requireNovel(id);

        Chapter chapter = new Chapter();
        chapter.setNovelId(id);
        // 刚新增的草稿允许没有标题，进入编辑器后再修改；保存与发布时会校验
        chapter.setTitle(request == null || StringUtils.isBlank(request.title())
                ? "未命名章节" : request.title().trim());
        chapter.setContent(request == null ? "" : StringUtils.defaultString(request.content()));
        chapter.setOrderNo(dataRepository.nextOrderNo(id));
        chapter.setStatus(ChapterStatus.DRAFT);
        return dataRepository.saveChapter(chapter);
    }

    @GetMapping("/chapters/{id}")
    @Operation(summary = "Get a chapter in author console (draft or published)")
    public Chapter getChapter(@PathVariable Long id) {
        return requireChapter(id);
    }

    @PutMapping("/chapters/{id}")
    @Operation(summary = "Update chapter title and content (draft stays private, published stays online)")
    public Chapter updateChapter(@PathVariable Long id, @RequestBody ChapterRequest request) {
        Chapter chapter = requireChapter(id);
        chapter.setTitle(resolveTitle(request));
        chapter.setContent(request == null ? "" : StringUtils.defaultString(request.content()));
        return dataRepository.saveChapter(chapter);
    }

    @PostMapping("/chapters/{id}/publish")
    @Operation(summary = "Publish a chapter so it becomes visible to public readers")
    public Chapter publishChapter(@PathVariable Long id) {
        Chapter chapter = requireChapter(id);
        if (StringUtils.isBlank(chapter.getTitle())) {
            throw new BadRequestException("章节标题不能为空，无法发布");
        }
        chapter.setStatus(ChapterStatus.PUBLISHED);
        return dataRepository.saveChapter(chapter);
    }

    private Novel requireNovel(Long id) {
        Novel novel = dataRepository.findNovelById(id);
        if (novel == null) {
            throw new NotFoundException("小说不存在");
        }
        return novel;
    }

    private Chapter requireChapter(Long id) {
        Chapter chapter = dataRepository.findChapterById(id);
        if (chapter == null) {
            throw new NotFoundException("章节不存在");
        }
        return chapter;
    }

    private String resolveTitle(ChapterRequest request) {
        if (request == null || StringUtils.isBlank(request.title())) {
            throw new BadRequestException("章节标题不能为空");
        }
        return request.title().trim();
    }
}
