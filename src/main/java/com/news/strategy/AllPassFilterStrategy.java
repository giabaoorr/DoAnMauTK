package com.news.strategy;

import com.news.model.Article;

import java.util.ArrayList;
import java.util.List;

/**
 * [Strategy] Concrete Strategy: Chiến lược mặc định cho phép tất cả các bài viết đi qua mà không lọc.
 */
public class AllPassFilterStrategy implements FilterStrategy {

    @Override
    public List<Article> filter(List<Article> articles) {
        if (articles == null) return List.of();
        return new ArrayList<>(articles);
    }

    @Override
    public boolean matches(Article article) {
        return article != null;
    }
}
