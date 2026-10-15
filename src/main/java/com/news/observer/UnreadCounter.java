package com.news.observer;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [Observer] Concrete Observer: Đếm số lượng tin tức mới nhận được sau mỗi lần làm mới.
 */
public class UnreadCounter implements NewsObserver {

    private final AtomicInteger lastNewCount = new AtomicInteger(0);
    private final AtomicInteger totalUnread = new AtomicInteger(0);
    private volatile LocalDateTime lastUpdateTime;

    @Override
    public void onNewsUpdated(NewsUpdateEvent event) {
        if (event != null) {
            int newCount = event.getNewArticlesCount();
            lastNewCount.set(newCount);
            totalUnread.addAndGet(newCount);
            lastUpdateTime = event.getTimestamp();
        }
    }

    public int getLastNewCount() {
        return lastNewCount.get();
    }

    public int getTotalUnread() {
        return totalUnread.get();
    }

    public void resetTotalUnread() {
        totalUnread.set(0);
    }

    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }
}
