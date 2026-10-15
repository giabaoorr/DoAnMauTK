package com.news.observer;

import com.news.model.Article;

import java.util.List;

/**
 * [Observer] Subject Interface trong mẫu thiết kế Observer.
 * Định nghĩa các thao tác đăng ký (attach), hủy đăng ký (detach) và thông báo (notify).
 */
public interface NewsSubject {

    /**
     * Đăng ký một Observer theo dõi cập nhật.
     */
    void attach(NewsObserver observer);

    /**
     * Hủy đăng ký một Observer.
     */
    void detach(NewsObserver observer);

    /**
     * Gửi thông báo đến tất cả các Observer đã đăng ký.
     */
    void notifyObservers(List<Article> newArticles, int totalCount);
}
