package com.news.strategy;

import com.news.model.Article;

import java.util.List;
import java.util.stream.Collectors;

/**
 * [Strategy] Concrete Strategy: Lọc bài viết theo chuyên mục (ví dụ: Thời sự, Kinh doanh, Công nghệ).
 */
public class CategoryFilterStrategy implements FilterStrategy {

    private final String targetCategory;

    public CategoryFilterStrategy(String targetCategory) {
        this.targetCategory = targetCategory;
    }

    @Override
    public List<Article> filter(List<Article> articles) {
        if (articles == null) return List.of();
        if (targetCategory == null || targetCategory.trim().isEmpty()) {
            return articles;
        }
        return articles.stream()
                .filter(this::matches)
                .collect(Collectors.toList());
    }

    @Override
    public boolean matches(Article article) {
        if (article == null || article.getCategory() == null) return false;
        return targetCategory != null && targetCategory.equalsIgnoreCase(article.getCategory().trim());
    }

    public String getTargetCategory() {
        return targetCategory;
    }
}
