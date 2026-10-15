package com.news.strategy;

import com.news.model.Article;

import java.util.List;

/**
 * [Strategy] Strategy Interface cho thuật toán sắp xếp bài viết.
 * Cho phép thay đổi thuật toán sắp xếp linh hoạt tại thời điểm chạy (runtime) theo lựa chọn của người dùng.
 */
public interface SortStrategy {

    /**
     * Sắp xếp danh sách bài viết theo tiêu chí của chiến lược cụ thể.
     *
     * @param articles Danh sách bài viết đầu vào
     * @return Danh sách bài viết mới đã được sắp xếp
     */
    List<Article> sort(List<Article> articles);
}
