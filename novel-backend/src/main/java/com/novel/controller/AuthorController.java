package com.novel.controller;

import com.novel.dto.ChapterRequest;
import com.novel.dto.NovelRequest;
import com.novel.exception.BusinessException;
import com.novel.model.Chapter;
import com.novel.model.ChapterStatus;
import com.novel.model.Novel;
import com.novel.repository.DataRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Author studio API: novel creation and chapter draft management.
 * Endpoints under /api/author can see and modify drafts,
 * unlike the public reader API which only exposes published chapters.
 */
@RestController
@RequestMapping("/api/author")
@CrossOrigin(origins = "*") // Allow frontend to access
@Tag(name = "Author Studio API")
public class AuthorController {

    private static final String DEFAULT_COVER_URL =
            "https://images.unsplash.com/photo-1512820790803-83ca734da794?q=80&w=800&auto=format&fit=crop";

    private final DataRepository dataRepository;

    public AuthorController(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @PostMapping("/novels")
    @Operation(summary = "Create a Novel")
    public Novel createNovel(@RequestBody NovelRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new BusinessException("Title is required");
        }
        String coverUrl = (request.getCoverUrl() == null || request.getCoverUrl().isBlank())
                ? DEFAULT_COVER_URL
                : request.getCoverUrl().trim();
        String description = safeTrim(request.getDescription());
        return dataRepository.saveNovel(request.getTitle().trim(), description, coverUrl);
    }

    @GetMapping("/novels/{novelId}/chapters")
    @Operation(summary = "List All Chapters of a Novel (including drafts)")
    public List<Chapter> listChapters(@PathVariable Long novelId) {
        dataRepository.requireNovel(novelId);
        return dataRepository.findAllChaptersByNovelId(novelId);
    }

    @GetMapping("/chapters/{id}")
    @Operation(summary = "Get a Chapter for Editing (including drafts)")
    public Chapter getChapterForEdit(@PathVariable Long id) {
        return dataRepository.requireChapter(id);
    }

    @PostMapping("/novels/{novelId}/chapters")
    @Operation(summary = "Create a Chapter (save as draft or publish)")
    public Chapter createChapter(@PathVariable Long novelId, @RequestBody ChapterRequest request) {
        dataRepository.requireNovel(novelId);
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new BusinessException("Chapter title is required");
        }
        String status = normalizeStatus(request.getStatus(), ChapterStatus.DRAFT.name());
        return dataRepository.saveChapter(novelId, request.getTitle().trim(),
                safeTrim(request.getContent()), status);
    }

    @PutMapping("/chapters/{id}")
    @Operation(summary = "Update Chapter Title and/or Content (and optionally status)")
    public Chapter updateChapter(@PathVariable Long id, @RequestBody ChapterRequest request) {
        Chapter chapter = dataRepository.requireChapter(id);
        if (request.getTitle() != null) {
            if (request.getTitle().isBlank()) {
                throw new BusinessException("Chapter title cannot be empty");
            }
            chapter.setTitle(request.getTitle().trim());
        }
        if (request.getContent() != null) {
            chapter.setContent(request.getContent());
        }
        if (request.getStatus() != null) {
            chapter.setStatus(normalizeStatus(request.getStatus(), chapter.getStatus()));
        }
        return dataRepository.updateChapter(chapter);
    }

    @PutMapping("/chapters/{id}/status")
    @Operation(summary = "Publish / Unpublish a Chapter")
    public Chapter updateChapterStatus(@PathVariable Long id, @RequestParam String status) {
        Chapter chapter = dataRepository.requireChapter(id);
        String normalized = normalizeStatus(status, null);
        if (normalized == null) {
            throw new BusinessException("Status is required");
        }
        chapter.setStatus(normalized);
        return dataRepository.updateChapter(chapter);
    }

    @DeleteMapping("/chapters/{id}")
    @Operation(summary = "Delete a Chapter (draft or published)")
    public void deleteChapter(@PathVariable Long id) {
        dataRepository.requireChapter(id);
        dataRepository.deleteChapter(id);
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Normalizes a status string to uppercase DRAFT/PUBLISHED.
     * Returns the fallback when the input is null or blank.
     */
    private String normalizeStatus(String status, String fallback) {
        if (status == null || status.isBlank()) {
            return fallback;
        }
        String upper = status.trim().toUpperCase();
        if (!ChapterStatus.isValid(upper)) {
            throw new BusinessException("Invalid status: " + status + ". Must be DRAFT or PUBLISHED");
        }
        return upper;
    }
}
