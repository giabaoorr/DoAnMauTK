package com.news.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Lớp đại diện cho một bài viết tin tức chuẩn hóa trong hệ thống.
 * Mọi nguồn tin (RSS, JSON mẫu, ...) đều được chuyển đổi về đối tượng của lớp này.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Article {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String id;
    private String title;
    private String description;
    private String content;
    private String url;
    private String imageUrl;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime publishedDate;
    private String source;
    private String category;
    private boolean bookmarked;
    private boolean featured;

    public Article() {
        this.publishedDate = LocalDateTime.now();
    }

    public Article(String id, String title, String description, String content,
                   String url, String imageUrl, LocalDateTime publishedDate,
                   String source, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.content = content;
        this.url = url;
        this.imageUrl = imageUrl;
        this.publishedDate = publishedDate != null ? publishedDate : LocalDateTime.now();
        this.source = source;
        this.category = category;
        this.bookmarked = false;
        this.featured = false;
    }

    // Copy constructor phục vụ nhân bản hoặc Decorator
    public Article(Article other) {
        if (other != null) {
            this.id = other.getId();
            this.title = other.getTitle();
            this.description = other.getDescription();
            this.content = other.getContent();
            this.url = other.getUrl();
            this.imageUrl = other.getImageUrl();
            this.publishedDate = other.getPublishedDate();
            this.source = other.getSource();
            this.category = other.getCategory();
            this.bookmarked = other.isBookmarked();
            this.featured = other.isFeatured();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDateTime publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getFormattedDate() {
        if (publishedDate == null) {
            return "";
        }
        return publishedDate.format(FORMATTER);
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public String getBadge() {
        if (this.featured) {
            return "NỔI BẬT";
        }
        return null;
    }

    public String getEstimatedReadingTime() {
        String text = (content != null && !content.isEmpty()) ? content : description;
        if (text == null || text.isEmpty()) {
            return "1 phút đọc";
        }
        int words = text.trim().split("\\s+").length;
        int minutes = Math.max(1, (int) Math.ceil(words / 180.0));
        return minutes + " phút đọc";
    }

    public String getHighlightReason() {
        if (this.featured) {
            return "Tin tiêu điểm trong ngày";
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Article)) return false;
        Article article = (Article) o;
        if (id != null && article.id != null) {
            return Objects.equals(id, article.id);
        }
        return Objects.equals(url, article.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : url);
    }

    @Override
    public String toString() {
        return "Article{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", source='" + source + '\'' +
                ", category='" + category + '\'' +
                ", publishedDate=" + publishedDate +
                '}';
    }
}
