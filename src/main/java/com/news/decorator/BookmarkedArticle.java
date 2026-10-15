package com.news.decorator;

import com.news.model.Article;

/**
 * [Decorator] Concrete Decorator: Trang trí bài viết thành bài viết "ĐÃ ĐÁNH DẤU / ĐÃ LƯU".
 * Ghi đè phương thức isBookmarked() trả về true và bổ sung nhãn ĐÃ LƯU vào getBadge().
 */
public class BookmarkedArticle extends ArticleDecorator {

    public BookmarkedArticle(Article article) {
        super(article);
    }

    @Override
    public boolean isBookmarked() {
        // [Decorator] Cung cấp trạng thái đánh dấu thông qua bọc đối tượng
        return true;
    }

    @Override
    public String getBadge() {
        String baseBadge = decoratedArticle.getBadge();
        if (baseBadge != null && !baseBadge.isEmpty()) {
            return baseBadge.contains("ĐÃ LƯU") ? baseBadge : baseBadge + " • ĐÃ LƯU";
        }
        return "ĐÃ LƯU";
    }
}
