package com.news.strategy;

import com.news.model.Article;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * [Strategy] Concrete Strategy: Sắp xếp bài viết theo thời gian đọc ngắn nhất lên đầu (Đọc nhanh 1-2 phút).
 * Phù hợp với thói quen đọc tin vắn, cập nhật nhanh của độc giả hiện đại.
 * Nếu thời gian đọc tương đương nhau, bài viết mới hơn sẽ được ưu tiên xếp trước.
 */
public class ReadingTimeSortStrategy implements SortStrategy {

    @Override
    public List<Article> sort(List<Article> articles) {
        if (articles == null) return List.of();
        List<Article> copy = new ArrayList<>(articles);

        copy.sort(
                Comparator.comparingInt((Article a) -> a != null ? a.getReadingTimeMinutes() : Integer.MAX_VALUE)
                        .thenComparing(
                                a -> a != null ? a.getPublishedDate() : null,
                                Comparator.nullsLast(Comparator.reverseOrder())
                        )
        );

        return copy;
    }
}
