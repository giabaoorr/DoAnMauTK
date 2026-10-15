package com.news.observer;

import com.news.model.Article;

import java.time.LocalDateTime;
import java.util.List;

/**
 * [Observer] Đối tượng mang thông tin sự kiện khi có cập nhật tin tức mới.
 */
public class NewsUpdateEvent {
    private final List<Article> newArticles;
    private final int totalCount;
    private final LocalDateTime timestamp;

    public NewsUpdateEvent(List<Article> newArticles, int totalCount) {
        this.newArticles = newArticles != null ? List.copyOf(newArticles) : List.of();
        this.totalCount = totalCount;
        this.timestamp = LocalDateTime.now();
    }

    public List<Article> getNewArticles() {
        return newArticles;
    }

    public int getNewArticlesCount() {
        return newArticles.size();
    }

    public int getTotalCount() {
        return totalCount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
