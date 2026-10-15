package com.news.service;

import com.news.model.Article;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookmarkServiceTest {

    @TempDir
    Path tempDir;

    private BookmarkService bookmarkService;
    private Path bookmarkFile;
    private Article sampleArticle;

    @BeforeEach
    void setUp() {
        bookmarkFile = tempDir.resolve("bookmarks-test.json");
        bookmarkService = new BookmarkService(bookmarkFile.toString());

        sampleArticle = new Article(
                "bm-1",
                "Tiêu đề bài viết cần lưu",
                "Mô tả",
                "Nội dung",
                "http://test.com/bm1",
                null,
                LocalDateTime.of(2026, 10, 15, 8, 0),
                "Tuổi Trẻ",
                "Thời sự"
        );
    }

    @AfterEach
    void tearDown() {
        bookmarkService.clearAll();
    }

    @Test
    @DisplayName("[Bookmark & JSON] Toggle đánh dấu bài viết và ghi/đọc bền vững ra file JSON")
    void testToggleBookmarkAndPersistence() {
        assertThat(bookmarkService.getBookmarkCount()).isEqualTo(0);

        // 1. Đánh dấu bài viết
        boolean isMarked = bookmarkService.toggleBookmark(sampleArticle);
        assertThat(isMarked).isTrue();
        assertThat(bookmarkService.getBookmarkCount()).isEqualTo(1);
        assertThat(bookmarkService.isBookmarked("bm-1")).isTrue();

        // Kiểm tra file JSON đã được tạo ra trên đĩa
        File file = bookmarkFile.toFile();
        assertThat(file).exists();
        assertThat(file.length()).isGreaterThan(0);

        // 2. [Decorator] Danh sách trả về được bọc bởi BookmarkedArticle
        List<Article> bookmarkedList = bookmarkService.getBookmarkedArticles();
        assertThat(bookmarkedList).hasSize(1);
        Article item = bookmarkedList.get(0);
        assertThat(item.isBookmarked()).isTrue();
        assertThat(item.getBadge()).contains("ĐÃ LƯU");

        // 3. Khởi tạo một BookmarkService mới trỏ vào cùng file JSON để kiểm tra tính bền vững (Persistence)
        BookmarkService reloadedService = new BookmarkService(bookmarkFile.toString());
        reloadedService.loadBookmarksFromFile();
        assertThat(reloadedService.getBookmarkCount()).isEqualTo(1);
        assertThat(reloadedService.isBookmarked("bm-1")).isTrue();

        // 4. Toggle lần thứ hai để hủy đánh dấu
        boolean unMarked = bookmarkService.toggleBookmark(sampleArticle);
        assertThat(unMarked).isFalse();
        assertThat(bookmarkService.getBookmarkCount()).isEqualTo(0);
        assertThat(bookmarkService.isBookmarked("bm-1")).isFalse();
    }
}
