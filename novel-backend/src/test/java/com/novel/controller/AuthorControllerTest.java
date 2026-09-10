package com.novel.controller;

import com.novel.repository.DataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 作者后台接口的身份隔离测试：
 * 不同 X-Author-Id 的作者之间，小说与章节草稿互不可见、不可操作。
 */
class AuthorControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        DataRepository repository = new DataRepository();
        repository.init();
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthorController(repository)).build();
    }

    private Long createNovelAs(String authorId, String title) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/author/novels")
                        .header("X-Author-Id", authorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorId").value(authorId))
                .andReturn();
        String body = result.getResponse().getContentAsString();
        return Long.valueOf(body.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    @Test
    void missingAuthorHeaderIsRejected() throws Exception {
        mockMvc.perform(get("/api/author/novels"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/author/novels/1/chapters"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void novelListIsIsolatedByAuthor() throws Exception {
        createNovelAs("author-a", "作者A的书");

        // 作者A 能看到自己的书
        mockMvc.perform(get("/api/author/novels").header("X-Author-Id", "author-a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("作者A的书"));

        // 作者B 看不到作者A的书，也看不到种子数据
        mockMvc.perform(get("/api/author/novels").header("X-Author-Id", "author-b"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void chaptersOfOthersNovelAreForbidden() throws Exception {
        Long novelId = createNovelAs("author-a", "作者A的书");

        // 作者B 查看/操作作者A小说的章节列表 -> 403
        mockMvc.perform(get("/api/author/novels/{id}/chapters", novelId)
                        .header("X-Author-Id", "author-b"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/author/novels/{id}/chapters", novelId)
                        .header("X-Author-Id", "author-b")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"蹭章节\"}"))
                .andExpect(status().isForbidden());

        // 作者A 自己可以正常创建与查看
        mockMvc.perform(post("/api/author/novels/{id}/chapters", novelId)
                        .header("X-Author-Id", "author-a")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"第一章\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"));
        mockMvc.perform(get("/api/author/novels/{id}/chapters", novelId)
                        .header("X-Author-Id", "author-a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void chapterOperationsOfOthersAreForbidden() throws Exception {
        Long novelId = createNovelAs("author-a", "作者A的书");
        MvcResult chapterResult = mockMvc.perform(post("/api/author/novels/{id}/chapters", novelId)
                        .header("X-Author-Id", "author-a")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"秘密草稿\"}"))
                .andExpect(status().isOk())
                .andReturn();
        Long chapterId = Long.valueOf(chapterResult.getResponse().getContentAsString()
                .replaceAll(".*\"id\":(\\d+).*", "$1"));

        // 作者B 读/改/发布作者A的草稿 -> 403
        mockMvc.perform(get("/api/author/chapters/{id}", chapterId)
                        .header("X-Author-Id", "author-b"))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/author/chapters/{id}", chapterId)
                        .header("X-Author-Id", "author-b")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"篡改\",\"content\":\"x\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/author/chapters/{id}/publish", chapterId)
                        .header("X-Author-Id", "author-b"))
                .andExpect(status().isForbidden());

        // 作者A 自己操作正常
        mockMvc.perform(put("/api/author/chapters/{id}", chapterId)
                        .header("X-Author-Id", "author-a")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"定稿\",\"content\":\"正文\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("定稿"));
        mockMvc.perform(post("/api/author/chapters/{id}/publish", chapterId)
                        .header("X-Author-Id", "author-a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void nonExistentNovelReturns404() throws Exception {
        mockMvc.perform(get("/api/author/novels/{id}/chapters", 9999L)
                        .header("X-Author-Id", "author-a"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/author/chapters/{id}", 9999L)
                        .header("X-Author-Id", "author-a"))
                .andExpect(status().isNotFound());
    }

    @Test
    void seedNovelsAreNotAccessibleByAnyAuthor() throws Exception {
        // 种子小说归属 seed-author，普通作者无法通过作者后台读取其章节
        mockMvc.perform(get("/api/author/novels/{id}/chapters", 1L)
                        .header("X-Author-Id", "author-a"))
                .andExpect(status().isForbidden());
    }
}
