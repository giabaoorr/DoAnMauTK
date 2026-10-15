package com.news.strategy;

import com.news.model.Article;

import java.util.List;

/**
 * [Strategy] Strategy Interface cho thuật toán lọc bài viết theo tiêu chí người dùng.
 */
public interface FilterStrategy {

    /**
     * Lọc danh sách bài viết theo tiêu chí của chiến lược.
     *
     * @param articles Danh sách bài viết đầu vào
     * @return Danh sách bài viết thỏa mãn điều kiện lọc
     */
    List<Article> filter(List<Article> articles);

    /**
     * Kiểm tra một bài viết đơn lẻ có thỏa mãn tiêu chí lọc hay không.
     */
    boolean matches(Article article);
}
