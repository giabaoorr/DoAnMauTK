package com.news.strategy;

import com.news.model.Article;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StrategyPatternTest {

    private Article a1;
    private Article a2;
    private Article a3;
    private List<Article> testArticles;

    @BeforeEach
    void setUp() {
        a1 = new Article("1", "Bóng đá Việt Nam thăng tiến", "Mô tả 1", "Nội dung 1",
                "http://tuoitre.vn/1", null, LocalDateTime.of(2026, 10, 10, 8, 0), "Tuổi Trẻ", "Thể thao");

        a2 = new Article("2", "Công nghệ AI phát triển vượt bậc", "Mô tả 2", "Nội dung 2",
                "http://vnexpress.net/2", null, LocalDateTime.of(2026, 10, 15, 10, 30), "VnExpress", "Công nghệ");

        a3 = new Article("3", "Áp thấp nhiệt đới trên biển Đông", "Mô tả 3", "Nội dung 3",
                "http://vnexpress.net/3", null, LocalDateTime.of(2026, 10, 12, 14, 0), "VnExpress", "Thời sự");

        testArticles = List.of(a1, a2, a3);
    }

    @Test
    @DisplayName("[Strategy] NewestSortStrategy sắp xếp thời gian giảm dần (mới nhất trước)")
    void testNewestSortStrategy() {
        SortStrategy strategy = new NewestSortStrategy();
        List<Article> sorted = strategy.sort(testArticles);

        assertThat(sorted).hasSize(3);
        assertThat(sorted.get(0).getId()).isEqualTo("2"); // 15/10
        assertThat(sorted.get(1).getId()).isEqualTo("3"); // 12/10
        assertThat(sorted.get(2).getId()).isEqualTo("1"); // 10/10
    }

    @Test
    @DisplayName("[Strategy] OldestSortStrategy sắp xếp thời gian tăng dần (cũ nhất trước)")
    void testOldestSortStrategy() {
        SortStrategy strategy = new OldestSortStrategy();
        List<Article> sorted = strategy.sort(testArticles);

        assertThat(sorted).hasSize(3);
        assertThat(sorted.get(0).getId()).isEqualTo("1"); // 10/10
        assertThat(sorted.get(1).getId()).isEqualTo("3"); // 12/10
        assertThat(sorted.get(2).getId()).isEqualTo("2"); // 15/10
    }

    @Test
    @DisplayName("[Strategy] TitleSortStrategy sắp xếp theo thứ tự bảng chữ cái A-Z")
    void testTitleSortStrategy() {
        SortStrategy strategy = new TitleSortStrategy();
        List<Article> sorted = strategy.sort(testArticles);

        assertThat(sorted).hasSize(3);
        // "Áp thấp..." -> "Bóng đá..." -> "Công nghệ..."
        assertThat(sorted.get(0).getId()).isEqualTo("3");
        assertThat(sorted.get(1).getId()).isEqualTo("1");
        assertThat(sorted.get(2).getId()).isEqualTo("2");
    }

    @Test
    @DisplayName("[Strategy] CategoryFilterStrategy lọc theo đúng chuyên mục")
    void testCategoryFilterStrategy() {
        FilterStrategy strategy = new CategoryFilterStrategy("Công nghệ");
        List<Article> filtered = strategy.filter(testArticles);

        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).getId()).isEqualTo("2");
        assertThat(filtered.get(0).getCategory()).isEqualTo("Công nghệ");
    }

    @Test
    @DisplayName("[Strategy] SourceFilterStrategy lọc theo đúng nguồn báo")
    void testSourceFilterStrategy() {
        FilterStrategy strategy = new SourceFilterStrategy("Tuổi Trẻ");
        List<Article> filtered = strategy.filter(testArticles);

        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).getSource()).isEqualTo("Tuổi Trẻ");
    }

    @Test
    @DisplayName("[Strategy] CompositeFilterStrategy kết hợp lọc theo cả Chuyên mục và Nguồn")
    void testCompositeFilterStrategy() {
        CompositeFilterStrategy composite = new CompositeFilterStrategy()
                .addStrategy(new SourceFilterStrategy("VnExpress"))
                .addStrategy(new CategoryFilterStrategy("Thời sự"));

        List<Article> filtered = composite.filter(testArticles);

        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).getId()).isEqualTo("3");
        assertThat(filtered.get(0).getSource()).isEqualTo("VnExpress");
        assertThat(filtered.get(0).getCategory()).isEqualTo("Thời sự");
    }

    @Test
    @DisplayName("[Strategy] ArticleProcessorContext thay đổi chiến lược sắp xếp và lọc tại thời điểm chạy")
    void testContextRuntimeStrategySwapping() {
        ArticleProcessorContext context = new ArticleProcessorContext();

        // 1. Áp dụng chiến lược ban đầu: NewestSort + AllPass
        List<Article> res1 = context.process(testArticles);
        assertThat(res1.get(0).getId()).isEqualTo("2"); // mới nhất

        // 2. [Strategy] Hoán đổi chiến lược sắp xếp sang OldestSortStrategy lúc runtime
        context.setSortStrategy(new OldestSortStrategy());
        List<Article> res2 = context.process(testArticles);
        assertThat(res2.get(0).getId()).isEqualTo("1"); // cũ nhất

        // 3. [Strategy] Hoán đổi chiến lược lọc sang CategoryFilterStrategy("Thể thao") lúc runtime
        context.setFilterStrategy(new CategoryFilterStrategy("Thể thao"));
        List<Article> res3 = context.process(testArticles);
        assertThat(res3).hasSize(1);
        assertThat(res3.get(0).getId()).isEqualTo("1");

        // 4. Kiểm tra helper fromUserPreferences
        ArticleProcessorContext prefContext = ArticleProcessorContext.fromUserPreferences("Thời sự", "VnExpress", "title");
        List<Article> res4 = prefContext.process(testArticles);
        assertThat(res4).hasSize(1);
        assertThat(res4.get(0).getId()).isEqualTo("3");
    }
}
