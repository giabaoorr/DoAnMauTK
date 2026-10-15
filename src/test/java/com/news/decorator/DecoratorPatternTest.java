package com.news.decorator;

import com.news.model.Article;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DecoratorPatternTest {

    private Article baseArticle;

    @BeforeEach
    void setUp() {
        baseArticle = new Article(
                "art-100",
                "Việt Nam phát triển chip bán dẫn thế hệ mới",
                "Các nhà khoa học tại Viện nghiên cứu công nghệ cao vừa công bố thiết kế chip vi xử lý tiết kiệm năng lượng phục vụ thiết bị IoT thông minh và mạng truyền thông 5G thế hệ mới.",
                "Toàn văn nội dung chi tiết bài viết...",
                "https://vnexpress.net/chip-ban-dan-100.html",
                "https://image.com/chip.jpg",
                LocalDateTime.of(2026, 10, 15, 9, 0),
                "VnExpress",
                "Công nghệ"
        );
    }

    @Test
    @DisplayName("[Decorator] FeaturedArticle bổ sung tính năng đánh dấu NỔI BẬT")
    void testFeaturedArticleDecorator() {
        // [Decorator] Bọc Article bằng FeaturedArticle
        Article featured = new FeaturedArticle(baseArticle);

        assertThat(featured.isFeatured()).isTrue();
        assertThat(featured.getBadge()).contains("NỔI BẬT");
        assertThat(featured.getTitle()).startsWith("★ ");
        // Các trường khác vẫn giữ nguyên giá trị gốc
        assertThat(featured.getId()).isEqualTo(baseArticle.getId());
        assertThat(featured.getSource()).isEqualTo("VnExpress");
    }

    @Test
    @DisplayName("[Decorator] BookmarkedArticle bổ sung tính năng đánh dấu ĐÃ LƯU")
    void testBookmarkedArticleDecorator() {
        assertThat(baseArticle.isBookmarked()).isFalse();

        // [Decorator] Bọc Article bằng BookmarkedArticle
        Article bookmarked = new BookmarkedArticle(baseArticle);

        assertThat(bookmarked.isBookmarked()).isTrue();
        assertThat(bookmarked.getBadge()).contains("ĐÃ LƯU");
        assertThat(bookmarked.getTitle()).isEqualTo(baseArticle.getTitle());
    }

    @Test
    @DisplayName("[Decorator] SummarizedArticle rút gọn mô tả và tính toán thời gian đọc")
    void testSummarizedArticleDecorator() {
        // [Decorator] Bọc Article bằng SummarizedArticle với giới hạn 60 ký tự
        SummarizedArticle summarized = new SummarizedArticle(baseArticle, 60);

        assertThat(summarized.getDescription().length()).isLessThanOrEqualTo(65);
        assertThat(summarized.getDescription()).endsWith("...");
        assertThat(summarized.getEstimatedReadingTime()).contains("phút đọc");
        assertThat(summarized.getSummary()).contains("⏱");
    }

    @Test
    @DisplayName("[Decorator] Xếp chồng (stacking) nhiều Decorator trên cùng một đối tượng Article")
    void testMultipleDecoratorsStacked() {
        // [Decorator] Xếp chồng: BaseArticle -> SummarizedArticle -> FeaturedArticle -> BookmarkedArticle
        Article decorated = new BookmarkedArticle(
                new FeaturedArticle(
                        new SummarizedArticle(baseArticle, 80)
                )
        );

        // Kiểm tra tất cả các đặc tính từ các decorator đều có mặt
        assertThat(decorated.isBookmarked()).isTrue();
        assertThat(decorated.isFeatured()).isTrue();
        assertThat(decorated.getTitle()).startsWith("★ ");
        assertThat(decorated.getBadge()).contains("NỔI BẬT");
        assertThat(decorated.getBadge()).contains("ĐÃ LƯU");
        assertThat(decorated.getDescription()).endsWith("...");
    }

    @Test
    @DisplayName("[Decorator] Ném lỗi ngoại lệ khi truyền article null vào ArticleDecorator")
    void testNullArticleThrows() {
        assertThrows(IllegalArgumentException.class, () -> new FeaturedArticle(null));
        assertThrows(IllegalArgumentException.class, () -> new BookmarkedArticle(null));
        assertThrows(IllegalArgumentException.class, () -> new SummarizedArticle(null));
    }
}
