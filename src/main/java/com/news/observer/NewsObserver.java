package com.news.observer;

/**
 * [Observer] Observer Interface trong mẫu thiết kế Observer.
 * Định nghĩa phương thức cập nhật khi Subject (NewsFeed) phát tín hiệu có tin mới.
 */
public interface NewsObserver {

    /**
     * Phương thức được Subject tự động gọi khi có bài viết mới.
     *
     * @param event Thông tin sự kiện cập nhật tin tức
     */
    void onNewsUpdated(NewsUpdateEvent event);
}
