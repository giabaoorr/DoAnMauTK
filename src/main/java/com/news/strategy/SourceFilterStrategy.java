package com.news.strategy;

import com.news.model.Article;

import java.util.List;
import java.util.stream.Collectors;

/**
 * [Strategy] Concrete Strategy: Lọc bài viết theo nguồn báo (ví dụ: VnExpress, Tuổi Trẻ).
 */
public class SourceFilterStrategy implements FilterStrategy {

    private final String targetSource;

    public SourceFilterStrategy(String targetSource) {
        this.targetSource = targetSource;
    }

    @Override
    public List<Article> filter(List<Article> articles) {
        if (articles == null) return List.of();
        if (targetSource == null || targetSource.trim().isEmpty()) {
            return articles;
        }
        return articles.stream()
                .filter(this::matches)
                .collect(Collectors.toList());
    }

    @Override
    public boolean matches(Article article) {
        if (article == null || article.getSource() == null) return false;
        return targetSource != null && targetSource.equalsIgnoreCase(article.getSource().trim());
    }

    public String getTargetSource() {
        return targetSource;
    }
}
