package com.news.cache;

import com.news.model.Article;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Đối tượng gói dữ liệu bài viết kèm thời gian hết hạn (TTL) trong Cache.
 */
public class CacheEntry {
    private final List<Article> articles;
    private final Instant createdAt;
    private final Duration ttl;

    public CacheEntry(List<Article> articles, Duration ttl) {
        this.articles = articles != null ? List.copyOf(articles) : List.of();
        this.createdAt = Instant.now();
        this.ttl = ttl != null ? ttl : Duration.ofMinutes(10);
    }

    public List<Article> getArticles() {
        return articles;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(createdAt.plus(ttl));
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Duration getTtl() {
        return ttl;
    }
}
