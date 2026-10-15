package com.news.decorator;

import com.news.model.Article;

/**
 * [Decorator] Concrete Decorator: Trang trí bài viết thành bài viết "NỔI BẬT".
 * Thêm nhãn NỔI BẬT và làm nổi bật tiêu đề bài viết.
 */
public class FeaturedArticle extends ArticleDecorator {

    public FeaturedArticle(Article article) {
        super(article);
    }

    @Override
    public boolean isFeatured() {
        return true;
    }

    @Override
    public String getBadge() {
        String baseBadge = decoratedArticle.getBadge();
        if (baseBadge != null && !baseBadge.isEmpty()) {
            return baseBadge.contains("NỔI BẬT") ? baseBadge : "NỔI BẬT • " + baseBadge;
        }
        return "NỔI BẬT";
    }

    @Override
    public String getTitle() {
        String baseTitle = decoratedArticle.getTitle();
        if (baseTitle != null && !baseTitle.startsWith("★")) {
            return "★ " + baseTitle;
        }
        return baseTitle;
    }
}
