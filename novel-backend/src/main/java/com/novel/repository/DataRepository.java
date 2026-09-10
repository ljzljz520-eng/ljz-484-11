package com.novel.repository;

import com.novel.model.Chapter;
import com.novel.model.Novel;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class DataRepository {
        private final Map<Long, Novel> novels = new ConcurrentHashMap<>();
        private final Map<Long, Chapter> chapters = new ConcurrentHashMap<>();
        private final AtomicLong novelIdGenerator = new AtomicLong(1);
        private final AtomicLong chapterIdGenerator = new AtomicLong(1);

        @PostConstruct
        public void init() {
                // Seeding Data
                Novel novel1 = new Novel(novelIdGenerator.getAndIncrement(),
                                "星际穿越之编程大师",
                                "讲述一位程序员意外穿越到未来，用代码拯救宇宙的故事。",
                                "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?q=80&w=800&auto=format&fit=crop",
                                LocalDateTime.now());
                novels.put(novel1.getId(), novel1);

                seedChapter(novel1.getId(), "第一章：Hello World", 1, "他醒来时，发现眼前只有绿色的代码流...");
                seedChapter(novel1.getId(), "第二章：变量声明", 2, "“你是谁？”面前的机器人冷冷地问道。“Define me.”他回答。");
                seedChapter(novel1.getId(), "第三章：循环陷阱", 3, "时间仿佛陷入了死循环，他必须找到 break 的条件。");

                Novel novel2 = new Novel(novelIdGenerator.getAndIncrement(),
                                "灵气复苏时代的架构师",
                                "灵气复苏，万物进化。他发现修仙法门竟然符合微服务架构原理。",
                                "https://images.unsplash.com/photo-1518770660439-4636190af475?q=80&w=600&auto=format&fit=crop",
                                LocalDateTime.now());
                novels.put(novel2.getId(), novel2);

                seedChapter(novel2.getId(), "第一章：单体应用破碎", 1, "天地巨变，世界原本的秩序（Monolith）崩塌了。");
                seedChapter(novel2.getId(), "第二章：服务发现", 2, "他感应到了周围的灵气节点，就像注册中心里的服务一样清晰。");

                Novel novel3 = new Novel(novelIdGenerator.getAndIncrement(),
                                "只有我知道剧情的测试员",
                                "作为世界系统的唯一QA，他能看到由于Bug导致的隐藏剧情。",
                                "https://images.unsplash.com/photo-1555949963-ff9fe0c870eb?q=80&w=800&auto=format&fit=crop",
                                LocalDateTime.now());
                novels.put(novel3.getId(), novel3);
        }

        private void seedChapter(Long novelId, String title, int orderNo, String content) {
                long id = chapterIdGenerator.getAndIncrement();
                chapters.put(id, new Chapter(id, novelId, title, orderNo, content,
                                Chapter.STATUS_PUBLISHED, LocalDateTime.now(), LocalDateTime.now()));
        }

        // ---------- Novel ----------

        public List<Novel> findAllNovels(String keyword, int page, int size) {
                return novels.values().stream()
                                .filter(n -> keyword == null || keyword.isEmpty() || n.getTitle().contains(keyword)
                                                || n.getDescription().contains(keyword))
                                .sorted(Comparator.comparing(Novel::getId).reversed())
                                .skip((long) (page - 1) * size)
                                .limit(size)
                                .collect(Collectors.toList());
        }

        public long countNovels(String keyword) {
                return novels.values().stream()
                                .filter(n -> keyword == null || keyword.isEmpty() || n.getTitle().contains(keyword)
                                                || n.getDescription().contains(keyword))
                                .count();
        }

        public Novel findNovelById(Long id) {
                return novels.get(id);
        }

        public Novel saveNovel(Novel novel) {
                novel.setId(novelIdGenerator.getAndIncrement());
                novel.setCreatedAt(LocalDateTime.now());
                novels.put(novel.getId(), novel);
                return novel;
        }

        // ---------- Chapter ----------

        /** 前台读者视角：仅已发布章节 */
        public List<Chapter> findPublishedChaptersByNovelId(Long novelId) {
                return chapters.values().stream()
                                .filter(c -> c.getNovelId().equals(novelId)
                                                && Chapter.STATUS_PUBLISHED.equals(c.getStatus()))
                                .sorted(Comparator.comparing(Chapter::getOrderNo))
                                .collect(Collectors.toList());
        }

        /** 作者后台视角：包含草稿在内的全部章节 */
        public List<Chapter> findAllChaptersByNovelId(Long novelId) {
                return chapters.values().stream()
                                .filter(c -> c.getNovelId().equals(novelId))
                                .sorted(Comparator.comparing(Chapter::getOrderNo))
                                .collect(Collectors.toList());
        }

        public Chapter findChapterById(Long id) {
                return chapters.get(id);
        }

        /** 前台阅读：草稿章节对读者不可见 */
        public Chapter findPublishedChapterById(Long id) {
                Chapter chapter = chapters.get(id);
                if (chapter == null || !Chapter.STATUS_PUBLISHED.equals(chapter.getStatus())) {
                        return null;
                }
                return chapter;
        }

        public Chapter saveChapter(Chapter chapter) {
                chapter.setId(chapterIdGenerator.getAndIncrement());
                chapter.setCreatedAt(LocalDateTime.now());
                chapter.setUpdatedAt(LocalDateTime.now());
                chapters.put(chapter.getId(), chapter);
                return chapter;
        }

        public Chapter updateChapter(Long id, String title, String content) {
                Chapter chapter = chapters.get(id);
                if (chapter == null) {
                        return null;
                }
                chapter.setTitle(title);
                chapter.setContent(content);
                chapter.setUpdatedAt(LocalDateTime.now());
                return chapter;
        }

        public Chapter publishChapter(Long id) {
                Chapter chapter = chapters.get(id);
                if (chapter == null) {
                        return null;
                }
                chapter.setStatus(Chapter.STATUS_PUBLISHED);
                chapter.setUpdatedAt(LocalDateTime.now());
                return chapter;
        }

        /** 计算某小说下一章节的序号 */
        public int nextOrderNo(Long novelId) {
                return chapters.values().stream()
                                .filter(c -> c.getNovelId().equals(novelId))
                                .mapToInt(Chapter::getOrderNo)
                                .max()
                                .orElse(0) + 1;
        }
}
