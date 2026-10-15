package com.news.decorator;

import com.news.model.Article;

import java.time.LocalDateTime;

/**
 * [Decorator] Base Decorator trong mẫu thiết kế Decorator.
 * Kế thừa lớp Article và đồng thời nắm giữ một tham chiếu (delegate) đến một đối tượng Article khác.
 * Mọi lời gọi phương thức mặc định sẽ được ủy quyền (forward) cho Article bên trong.
 */
public abstract class ArticleDecorator extends Article {

    protected final Article decoratedArticle;

    public ArticleDecorator(Article decoratedArticle) {
        if (decoratedArticle == null) {
            throw new IllegalArgumentException("Article được trang trí không được để null");
        }
        this.decoratedArticle = decoratedArticle;
    }

    public Article getDecoratedArticle() {
        return decoratedArticle;
    }

    @Override
    public String getId() {
        return decoratedArticle.getId();
    }

    @Override
    public void setId(String id) {
        decoratedArticle.setId(id);
    }

    @Override
    public String getTitle() {
        return decoratedArticle.getTitle();
    }

    @Override
    public void setTitle(String title) {
        decoratedArticle.setTitle(title);
    }

    @Override
    public String getDescription() {
        return decoratedArticle.getDescription();
    }

    @Override
    public void setDescription(String description) {
        decoratedArticle.setDescription(description);
    }

    @Override
    public String getContent() {
        return decoratedArticle.getContent();
    }

    @Override
    public void setContent(String content) {
        decoratedArticle.setContent(content);
    }

    @Override
    public String getUrl() {
        return decoratedArticle.getUrl();
    }

    @Override
    public void setUrl(String url) {
        decoratedArticle.setUrl(url);
    }

    @Override
    public String getImageUrl() {
        return decoratedArticle.getImageUrl();
    }

    @Override
    public void setImageUrl(String imageUrl) {
        decoratedArticle.setImageUrl(imageUrl);
    }

    @Override
    public LocalDateTime getPublishedDate() {
        return decoratedArticle.getPublishedDate();
    }

    @Override
    public void setPublishedDate(LocalDateTime publishedDate) {
        decoratedArticle.setPublishedDate(publishedDate);
    }

    @Override
    public String getFormattedDate() {
        return decoratedArticle.getFormattedDate();
    }

    @Override
    public String getSource() {
        return decoratedArticle.getSource();
    }

    @Override
    public void setSource(String source) {
        decoratedArticle.setSource(source);
    }

    @Override
    public String getCategory() {
        return decoratedArticle.getCategory();
    }

    @Override
    public void setCategory(String category) {
        decoratedArticle.setCategory(category);
    }

    @Override
    public boolean isBookmarked() {
        return decoratedArticle.isBookmarked();
    }

    @Override
    public void setBookmarked(boolean bookmarked) {
        decoratedArticle.setBookmarked(bookmarked);
    }

    @Override
    public boolean isFeatured() {
        return decoratedArticle.isFeatured();
    }

    @Override
    public void setFeatured(boolean featured) {
        decoratedArticle.setFeatured(featured);
    }

    @Override
    public String getBadge() {
        return decoratedArticle.getBadge();
    }
}
