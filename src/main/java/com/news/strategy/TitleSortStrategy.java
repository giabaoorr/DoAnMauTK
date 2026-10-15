package com.news.strategy;

import com.news.model.Article;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * [Strategy] Concrete Strategy: Sắp xếp bài viết theo thứ tự bảng chữ cái tiêu đề (A-Z).
 * Sử dụng Collator hỗ trợ tiếng Việt chính xác.
 */
public class TitleSortStrategy implements SortStrategy {

    private final Collator collator;

    public TitleSortStrategy() {
        this.collator = Collator.getInstance(Locale.forLanguageTag("vi-VN"));
        this.collator.setStrength(Collator.PRIMARY);
    }

    @Override
    public List<Article> sort(List<Article> articles) {
        if (articles == null) return List.of();
        List<Article> copy = new ArrayList<>(articles);
        copy.sort((a1, a2) -> {
            String t1 = a1.getTitle() != null ? a1.getTitle() : "";
            String t2 = a2.getTitle() != null ? a2.getTitle() : "";
            return collator.compare(t1, t2);
        });
        return copy;
    }
}
