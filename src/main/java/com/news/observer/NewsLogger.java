package com.news.observer;

import com.news.model.Article;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * [Observer] Concrete Observer: Tự động ghi log khi hệ thống nhận được bài viết mới.
 * Đồng thời lưu lại lịch sử log các lần cập nhật.
 */
public class NewsLogger implements NewsObserver {

    private static final Logger logger = LoggerFactory.getLogger(NewsLogger.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

    private final List<String> logHistory = new CopyOnWriteArrayList<>();

    @Override
    public void onNewsUpdated(NewsUpdateEvent event) {
        if (event == null) return;

        int newCount = event.getNewArticlesCount();
        int total = event.getTotalCount();
        String timeStr = event.getTimestamp().format(TIME_FORMATTER);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%s] [Observer:NewsLogger] Cập nhật tin tức: %d tin mới, tổng cộng: %d tin.",
                timeStr, newCount, total));

        if (newCount > 0) {
            sb.append(" Một số tiêu đề mới: ");
            int previewCount = Math.min(3, newCount);
            for (int i = 0; i < previewCount; i++) {
                Article a = event.getNewArticles().get(i);
                sb.append(String.format("[\"%s\" (%s)], ", a.getTitle(), a.getSource()));
            }
        }

        String logMessage = sb.toString();
        logger.info(logMessage);
        logHistory.add(logMessage);

        // Giữ tối đa 50 dòng log gần nhất trong bộ nhớ
        while (logHistory.size() > 50) {
            logHistory.remove(0);
        }
    }

    public List<String> getLogHistory() {
        return Collections.unmodifiableList(new ArrayList<>(logHistory));
    }

    public String getLastLog() {
        if (logHistory.isEmpty()) return null;
        return logHistory.get(logHistory.size() - 1);
    }
}
