package com.news.strategy;

import com.news.model.Article;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * [Strategy] Concrete Strategy: Kết hợp nhiều chiến lược lọc khác nhau (Logical AND).
 * Cho phép người dùng vừa lọc theo Chuyên mục vừa lọc theo Nguồn báo cùng lúc.
 */
public class CompositeFilterStrategy implements FilterStrategy {

    private final List<FilterStrategy> strategies = new ArrayList<>();

    public CompositeFilterStrategy() {
    }

    public CompositeFilterStrategy(List<FilterStrategy> strategies) {
        if (strategies != null) {
            this.strategies.addAll(strategies);
        }
    }

    public CompositeFilterStrategy addStrategy(FilterStrategy strategy) {
        if (strategy != null) {
            this.strategies.add(strategy);
        }
        return this;
    }

    @Override
    public List<Article> filter(List<Article> articles) {
        if (articles == null) return List.of();
        if (strategies.isEmpty()) return articles;

        return articles.stream()
                .filter(this::matches)
                .collect(Collectors.toList());
    }

    @Override
    public boolean matches(Article article) {
        if (article == null) return false;
        for (FilterStrategy strategy : strategies) {
            if (!strategy.matches(article)) {
                return false;
            }
        }
        return true;
    }

    public List<FilterStrategy> getStrategies() {
        return List.copyOf(strategies);
    }
}
