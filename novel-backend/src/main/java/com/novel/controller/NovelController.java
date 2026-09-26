package com.novel.controller;

import com.novel.exception.NotFoundException;
import com.novel.model.Chapter;
import com.novel.model.Novel;
import com.novel.repository.DataRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow frontend to access
@Tag(name = "Novel System API")
public class NovelController {

    private final DataRepository dataRepository;

    public NovelController(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @GetMapping("/novels")
    @Operation(summary = "Get Novel List")
    public Map<String, Object> getNovels(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        List<Novel> list = dataRepository.findAllNovels(keyword, page, size);
        long total = dataRepository.countNovels(keyword);

        Map<String, Object> response = new HashMap<>();
        response.put("data", list);
        response.put("total", total);
        response.put("page", page);
        response.put("size", size);
        return response;
    }

    @GetMapping("/novels/{id}")
    @Operation(summary = "Get Novel Details (with published chapters)")
    public Map<String, Object> getNovelDetail(@PathVariable Long id) {
        Novel novel = dataRepository.findNovelById(id);
        if (novel == null) {
            throw new NotFoundException("Novel not found");
        }
        // 公开读者只能看到已发布章节，草稿不会出现在目录中
        List<Chapter> chapters = dataRepository.findPublishedChaptersByNovelId(id);

        Map<String, Object> response = new HashMap<>();
        response.put("novel", novel);
        response.put("chapters", chapters);
        return response;
    }

    @GetMapping("/novels/{id}/chapters")
    @Operation(summary = "Get Published Chapters for a Novel")
    public List<Chapter> getChapters(@PathVariable Long id) {
        if (dataRepository.findNovelById(id) == null) {
            throw new NotFoundException("Novel not found");
        }
        return dataRepository.findPublishedChaptersByNovelId(id);
    }

    @GetMapping("/chapters/{id}")
    @Operation(summary = "Get Published Chapter Content")
    public Chapter getChapter(@PathVariable Long id) {
        // 草稿章节在公开接口中直接返回 404，只有作者后台接口可以读取
        Chapter chapter = dataRepository.findPublishedChapterById(id);
        if (chapter == null) {
            throw new NotFoundException("Chapter not found");
        }
        return chapter;
    }
}
