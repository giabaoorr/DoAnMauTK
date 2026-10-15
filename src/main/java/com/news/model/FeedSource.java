package com.news.model;

import com.news.factory.SourceType;

/**
 * Định nghĩa một kênh RSS hoặc nguồn cấp tin tức.
 */
public class FeedSource {
    private final String name;
    private final String category;
    private final String url;
    private final SourceType type;

    public FeedSource(String name, String category, String url, SourceType type) {
        this.name = name;
        this.category = category;
        this.url = url;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getUrl() {
        return url;
    }

    public SourceType getType() {
        return type;
    }
}
