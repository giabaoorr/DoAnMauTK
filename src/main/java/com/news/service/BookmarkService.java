package com.news.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.news.decorator.BookmarkedArticle;
import com.news.model.Article;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service quản lý bài viết đã đánh dấu (bookmark).
 * Dữ liệu đánh dấu được lưu trữ bền vững ra file JSON (data/bookmarks.json).
 * Kết hợp mẫu thiết kế Decorator để bọc các bài viết được đánh dấu (BookmarkedArticle).
 */
@Service
public class BookmarkService {
    private static final Logger logger = LoggerFactory.getLogger(BookmarkService.class);
    private static final String DEFAULT_FILE_PATH = "data/bookmarks.json";

    private final Path storagePath;
    private final Map<String, Article> bookmarkedMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public BookmarkService() {
        this(DEFAULT_FILE_PATH);
    }

    public BookmarkService(String storageFilePath) {
        this.storagePath = Paths.get(storageFilePath);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @PostConstruct
    public void init() {
        loadBookmarksFromFile();
    }

    /**
     * Nạp danh sách bài viết đã đánh dấu từ file JSON.
     */
    public synchronized void loadBookmarksFromFile() {
        try {
            if (!Files.exists(storagePath)) {
                return;
            }
            List<Article> list = objectMapper.readValue(storagePath.toFile(), new TypeReference<List<Article>>() {});
            bookmarkedMap.clear();
            if (list != null) {
                for (Article a : list) {
                    if (a.getId() != null) {
                        // [Decorator] Bọc bài viết bằng BookmarkedArticle
                        bookmarkedMap.put(a.getId(), new BookmarkedArticle(a));
                    }
                }
            }
            logger.info("Đã nạp {} bài viết đã lưu từ file {}", bookmarkedMap.size(), storagePath);
        } catch (Exception e) {
            logger.error("Không thể đọc file bookmarks.json: {}", e.getMessage(), e);
        }
    }

    /**
     * Ghi danh sách bài viết đã đánh dấu ra file JSON.
     */
    public synchronized void saveBookmarksToFile() {
        try {
            File file = storagePath.toFile();
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // Chuyển về đối tượng gốc trước khi serialize để tránh lặp lồng decorator
            List<Article> toSave = bookmarkedMap.values().stream()
                    .map(Article::new)
                    .collect(Collectors.toList());

            objectMapper.writeValue(file, toSave);
            logger.info("Đã lưu {} bài viết đã đánh dấu ra file {}", toSave.size(), storagePath);
        } catch (IOException e) {
            logger.error("Lỗi khi ghi file bookmarks.json: {}", e.getMessage(), e);
        }
    }

    /**
     * Chuyển đổi trạng thái đánh dấu của bài viết (Đánh dấu / Bỏ đánh dấu).
     *
     * @param article Bài viết cần toggle
     * @return true nếu bài viết hiện được đánh dấu, false nếu bị hủy đánh dấu
     */
    public synchronized boolean toggleBookmark(Article article) {
        if (article == null || article.getId() == null) {
            return false;
        }

        String id = article.getId();
        if (bookmarkedMap.containsKey(id)) {
            bookmarkedMap.remove(id);
            article.setBookmarked(false);
            saveBookmarksToFile();
            return false;
        } else {
            // [Decorator] Bọc đối tượng bằng BookmarkedArticle
            Article decorated = new BookmarkedArticle(article);
            bookmarkedMap.put(id, decorated);
            article.setBookmarked(true);
            saveBookmarksToFile();
            return true;
        }
    }

    public boolean isBookmarked(String articleId) {
        if (articleId == null) return false;
        return bookmarkedMap.containsKey(articleId);
    }

    /**
     * Lấy danh sách các bài viết đã đánh dấu, tất cả đều được bọc bởi BookmarkedArticle (Decorator).
     */
    public List<Article> getBookmarkedArticles() {
        return new ArrayList<>(bookmarkedMap.values());
    }

    public int getBookmarkCount() {
        return bookmarkedMap.size();
    }

    public void clearAll() {
        bookmarkedMap.clear();
        saveBookmarksToFile();
    }
}
