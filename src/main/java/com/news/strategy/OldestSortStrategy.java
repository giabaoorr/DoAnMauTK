package com.news.strategy;

import com.news.model.Article;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * [Strategy] Concrete Strategy: Sắp xếp bài viết theo thời gian xuất bản cũ nhất lên đầu.
 * @deprecated Đã loại bỏ khỏi giao diện người dùng theo chuẩn trải nghiệm đọc báo hiện đại.
 *             Thay thế bằng {@link ReadingTimeSortStrategy} hoặc {@link FeaturedFirstSortStrategy}.
 */
@Deprecated
public class OldestSortStrategy implements SortStrategy {

    @Override
    public List<Article> sort(List<Article> articles) {
        if (articles == null) return List.of();
        List<Article> copy = new ArrayList<>(articles);
        copy.sort(Comparator.comparing(
                a -> a != null ? a.getPublishedDate() : null,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));
        return copy;
    }
}
