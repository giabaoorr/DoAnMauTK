package com.news.observer;

import com.news.model.Article;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * [Observer] Concrete Subject trong mẫu thiết kế Observer.
 * Quản lý danh sách các Observer đăng ký và phát thông báo khi có tin tức mới được nạp.
 */
public class NewsFeed implements NewsSubject {

    private final List<NewsObserver> observers = new CopyOnWriteArrayList<>();

    @Override
    public void attach(NewsObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void detach(NewsObserver observer) {
        if (observer != null) {
            observers.remove(observer);
        }
    }

    @Override
    public void notifyObservers(List<Article> newArticles, int totalCount) {
        NewsUpdateEvent event = new NewsUpdateEvent(newArticles, totalCount);
        // [Observer] Duyệt qua toàn bộ danh sách observers và gửi thông báo
        for (NewsObserver observer : observers) {
            observer.onNewsUpdated(event);
        }
    }

    public List<NewsObserver> getObservers() {
        return List.copyOf(observers);
    }
}
