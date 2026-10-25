package com.news.strategy;

import com.news.model.Article;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * [Strategy] Concrete Strategy: Sắp xếp ưu tiên các bài viết Tiêu điểm / Nổi bật lên đầu.
 * Kết hợp chặt chẽ giữa Strategy Pattern và Decorator Pattern (FeaturedArticle).
 * Các bài viết cùng trạng thái nổi bật sẽ được sắp xếp theo thời gian mới nhất giảm dần.
 */
public class FeaturedFirstSortStrategy implements SortStrategy {

    @Override
    public List<Article> sort(List<Article> articles) {
        if (articles == null) return List.of();
        List<Article> copy = new ArrayList<>(articles);

        copy.sort(
                Comparator.comparing((Article a) -> a != null && a.isFeatured(), Comparator.reverseOrder())
                        .thenComparing(
                                a -> a != null ? a.getPublishedDate() : null,
                                Comparator.nullsLast(Comparator.reverseOrder())
                        )
        );

        return copy;
    }
}
