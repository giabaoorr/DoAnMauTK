package com.news.strategy;

import com.news.model.Article;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * [Strategy] Concrete Strategy: Sắp xếp bài viết theo thời gian xuất bản mới nhất lên đầu.
 */
public class NewestSortStrategy implements SortStrategy {

    @Override
    public List<Article> sort(List<Article> articles) {
        if (articles == null) return List.of();
        List<Article> copy = new ArrayList<>(articles);
        copy.sort(Comparator.comparing(
                a -> a != null ? a.getPublishedDate() : null,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));
        return copy;
    }
}
