package com.news.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.news.decorator.BookmarkedArticle;
import com.news.model.Article;
import com.news.model.UserBookmark;
import com.news.repository.UserBookmarkRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service quản lý bài viết đã đánh dấu (bookmark).
 * Hỗ trợ lưu trữ theo tài khoản người dùng trong H2 Database (khi đã đăng nhập)
 * và lưu trữ bền vững ra file JSON (data/bookmarks.json) cho khách vãng lai (guest).
 * Kết hợp mẫu thiết kế Decorator để bọc các bài viết được đánh dấu (BookmarkedArticle).
 */
@Service
public class BookmarkService {
    private static final Logger logger = LoggerFactory.getLogger(BookmarkService.class);
    private static final String DEFAULT_FILE_PATH = "data/bookmarks.json";

    private final Path storagePath;
    private final Map<String, Article> bookmarkedMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private UserBookmarkRepository userBookmarkRepository;

    public BookmarkService() {
        this(DEFAULT_FILE_PATH);
    }

    public BookmarkService(String storageFilePath) {
        this.storagePath = Paths.get(storageFilePath);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public BookmarkService(UserBookmarkRepository userBookmarkRepository) {
        this(DEFAULT_FILE_PATH);
        this.userBookmarkRepository = userBookmarkRepository;
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
     * Chuyển đổi trạng thái đánh dấu của bài viết cho khách vãng lai hoặc mặc định.
     */
    public synchronized boolean toggleBookmark(Article article) {
        return toggleBookmark(article, null);
    }

    /**
     * Chuyển đổi trạng thái đánh dấu của bài viết:
     * - Nếu có userId (đã đăng nhập): lưu/xóa trong bảng user_bookmarks của H2 Database.
     * - Nếu không có userId (khách vãng lai): lưu vào bookmarks.json.
     *
     * @param article Bài viết cần toggle
     * @param userId ID người dùng đang đăng nhập (hoặc null nếu chưa đăng nhập)
     * @return true nếu bài viết hiện được đánh dấu, false nếu bị hủy đánh dấu
     */
    @Transactional
    public synchronized boolean toggleBookmark(Article article, Long userId) {
        if (article == null || article.getId() == null) {
            return false;
        }

        String id = article.getId();

        // 1. Trường hợp người dùng đã đăng nhập: Lưu vào H2 Database
        if (userId != null && userBookmarkRepository != null) {
            Optional<UserBookmark> existing = userBookmarkRepository.findByUserIdAndArticleId(userId, id);
            if (existing.isPresent()) {
                userBookmarkRepository.deleteByUserIdAndArticleId(userId, id);
                article.setBookmarked(false);
                logger.info("User {} đã bỏ đánh dấu bài viết: {}", userId, id);
                return false;
            } else {
                UserBookmark bookmark = new UserBookmark(
                        userId,
                        id,
                        article.getTitle(),
                        article.getUrl(),
                        article.getImageUrl(),
                        article.getSource(),
                        article.getCategory(),
                        article.getDescription()
                );
                userBookmarkRepository.save(bookmark);
                article.setBookmarked(true);
                logger.info("User {} đã đánh dấu bài viết vào H2: {}", userId, id);
                return true;
            }
        }

        // 2. Trường hợp khách vãng lai (Guest): Lưu vào file JSON
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
        return isBookmarked(articleId, null);
    }

    public boolean isBookmarked(String articleId, Long userId) {
        if (articleId == null) return false;
        if (userId != null && userBookmarkRepository != null) {
            return userBookmarkRepository.existsByUserIdAndArticleId(userId, articleId);
        }
        return bookmarkedMap.containsKey(articleId);
    }

    /**
     * Lấy danh sách các bài viết đã đánh dấu của khách vãng lai.
     */
    public List<Article> getBookmarkedArticles() {
        return new ArrayList<>(bookmarkedMap.values());
    }

    /**
     * Lấy danh sách các bài viết đã đánh dấu của một tài khoản cụ thể từ H2 Database.
     */
    public List<Article> getBookmarkedArticles(Long userId) {
        if (userId != null && userBookmarkRepository != null) {
            List<UserBookmark> list = userBookmarkRepository.findByUserIdOrderBySavedAtDesc(userId);
            return list.stream().map(ub -> {
                Article a = new Article(
                        ub.getArticleId(),
                        ub.getTitle(),
                        ub.getDescription(),
                        ub.getDescription(),
                        ub.getUrl(),
                        ub.getImageUrl(),
                        ub.getSavedAt(),
                        ub.getSource(),
                        ub.getCategory()
                );
                return (Article) new BookmarkedArticle(a);
            }).collect(Collectors.toList());
        }
        return getBookmarkedArticles();
    }

    public int getBookmarkCount() {
        return bookmarkedMap.size();
    }

    public int getBookmarkCount(Long userId) {
        if (userId != null && userBookmarkRepository != null) {
            return (int) userBookmarkRepository.countByUserId(userId);
        }
        return getBookmarkCount();
    }

    public void clearAll() {
        bookmarkedMap.clear();
        saveBookmarksToFile();
    }
}
