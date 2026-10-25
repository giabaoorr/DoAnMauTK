package com.news.strategy;

import com.news.model.Article;

import java.util.List;

/**
 * [Strategy] Context trong mẫu thiết kế Strategy.
 * Nắm giữ tham chiếu tới các chiến lược SortStrategy và FilterStrategy.
 * Cho phép hoán đổi (swap) thuật toán lọc và sắp xếp lúc chạy (runtime) theo lựa chọn người dùng.
 */
public class ArticleProcessorContext {

    private SortStrategy sortStrategy;
    private FilterStrategy filterStrategy;

    public ArticleProcessorContext() {
        this.sortStrategy = new NewestSortStrategy();
        this.filterStrategy = new AllPassFilterStrategy();
    }

    public ArticleProcessorContext(SortStrategy sortStrategy, FilterStrategy filterStrategy) {
        this.sortStrategy = sortStrategy != null ? sortStrategy : new NewestSortStrategy();
        this.filterStrategy = filterStrategy != null ? filterStrategy : new AllPassFilterStrategy();
    }

    // [Strategy] Thay đổi chiến lược sắp xếp tại thời điểm chạy
    public void setSortStrategy(SortStrategy sortStrategy) {
        this.sortStrategy = sortStrategy != null ? sortStrategy : new NewestSortStrategy();
    }

    // [Strategy] Thay đổi chiến lược lọc tại thời điểm chạy
    public void setFilterStrategy(FilterStrategy filterStrategy) {
        this.filterStrategy = filterStrategy != null ? filterStrategy : new AllPassFilterStrategy();
    }

    public SortStrategy getSortStrategy() {
        return sortStrategy;
    }

    public FilterStrategy getFilterStrategy() {
        return filterStrategy;
    }

    /**
     * [Strategy] Thực thi các thuật toán chiến lược đã được cấu hình.
     * Đầu tiên lọc bài viết qua filterStrategy, sau đó sắp xếp qua sortStrategy.
     *
     * @param articles Danh sách bài viết nguồn
     * @return Danh sách bài viết sau khi được lọc và sắp xếp
     */
    public List<Article> process(List<Article> articles) {
        if (articles == null) return List.of();

        // 1. Thực thi chiến lược lọc (FilterStrategy)
        List<Article> filtered = filterStrategy.filter(articles);

        // 2. Thực thi chiến lược sắp xếp (SortStrategy)
        List<Article> sorted = sortStrategy.sort(filtered);

        return sorted;
    }

    /**
     * Phương thức tiện ích xây dựng Context động dựa trên các tham số lựa chọn của người dùng từ Web.
     *
     * @param category Tên chuyên mục cần lọc (hoặc rỗng/null nếu lấy tất cả)
     * @param source Tên nguồn báo cần lọc (hoặc rỗng/null nếu lấy tất cả)
     * @param sortCode Mã sắp xếp: "newest", "oldest", "title"
     * @return Đối tượng ArticleProcessorContext với các chiến lược đã được lắp ghép
     */
    public static ArticleProcessorContext fromUserPreferences(String category, String source, String sortCode) {
        // [Strategy] Lựa chọn chiến lược lọc động
        String trimmedCategory = (category != null && !category.trim().isEmpty()) ? category.trim() : null;
        String trimmedSource = (source != null && !source.trim().isEmpty()) ? source.trim() : null;

        FilterStrategy filter;
        if (trimmedCategory != null && trimmedSource != null) {
            filter = new CompositeFilterStrategy()
                    .addStrategy(new CategoryFilterStrategy(trimmedCategory))
                    .addStrategy(new SourceFilterStrategy(trimmedSource));
        } else if (trimmedCategory != null) {
            filter = new CategoryFilterStrategy(trimmedCategory);
        } else if (trimmedSource != null) {
            filter = new SourceFilterStrategy(trimmedSource);
        } else {
            filter = new AllPassFilterStrategy();
        }

        // [Strategy] Lựa chọn chiến lược sắp xếp động
        SortStrategy sort;
        if ("featured".equalsIgnoreCase(sortCode)) {
            sort = new FeaturedFirstSortStrategy();
        } else if ("reading_time".equalsIgnoreCase(sortCode) || "readingtime".equalsIgnoreCase(sortCode)) {
            sort = new ReadingTimeSortStrategy();
        } else if ("title".equalsIgnoreCase(sortCode)) {
            sort = new TitleSortStrategy();
        } else if ("oldest".equalsIgnoreCase(sortCode)) {
            sort = new OldestSortStrategy();
        } else {
            // Mặc định: mới nhất
            sort = new NewestSortStrategy();
        }

        return new ArticleProcessorContext(sort, filter);
    }
}
