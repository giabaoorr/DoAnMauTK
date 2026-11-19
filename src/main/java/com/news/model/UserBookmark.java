package com.news.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Thực thể lưu trữ bài viết đã đánh dấu của từng người dùng trong H2 Database.
 */
@Entity
@Table(name = "user_bookmarks", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "article_id"})
})
public class UserBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "article_id", nullable = false, length = 100)
    private String articleId;

    @Column(length = 500)
    private String title;

    @Column(length = 500)
    private String url;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 100)
    private String source;

    @Column(length = 100)
    private String category;

    @Column(length = 1000)
    private String description;

    private LocalDateTime savedAt = LocalDateTime.now();

    public UserBookmark() {
    }

    public UserBookmark(Long userId, String articleId, String title, String url, String imageUrl, String source, String category, String description) {
        this.userId = userId;
        this.articleId = articleId;
        this.title = title;
        this.url = url;
        this.imageUrl = imageUrl;
        this.source = source;
        this.category = category;
        this.description = description;
        this.savedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(LocalDateTime savedAt) {
        this.savedAt = savedAt;
    }
}
