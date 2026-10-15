package com.news.observer;

import com.news.model.Article;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ObserverPatternTest {

    @Test
    @DisplayName("[Observer] Subject thông báo đúng tới các Observer đã đăng ký")
    void testSubjectNotifiesRegisteredObservers() {
        // [Observer] Khởi tạo Subject và 2 Concrete Observers
        NewsFeed newsFeed = new NewsFeed();
        UnreadCounter counter = new UnreadCounter();
        NewsLogger logger = new NewsLogger();

        newsFeed.attach(counter);
        newsFeed.attach(logger);

        assertThat(newsFeed.getObservers()).hasSize(2);

        // Tạo dữ liệu bài viết mới mô phỏng
        Article a1 = new Article("test-1", "Tiêu đề tin mới 1", "Mô tả 1", "Nội dung",
                "http://test.com/1", null, LocalDateTime.now(), "VnExpress", "Thời sự");
        Article a2 = new Article("test-2", "Tiêu đề tin mới 2", "Mô tả 2", "Nội dung",
                "http://test.com/2", null, LocalDateTime.now(), "Tuổi Trẻ", "Công nghệ");
        List<Article> newArticles = List.of(a1, a2);

        // [Observer] Subject phát thông báo có 2 tin mới, tổng 50 tin
        newsFeed.notifyObservers(newArticles, 50);

        // 1. Kiểm tra UnreadCounter nhận thông báo
        assertThat(counter.getLastNewCount()).isEqualTo(2);
        assertThat(counter.getTotalUnread()).isEqualTo(2);
        assertThat(counter.getLastUpdateTime()).isNotNull();

        // 2. Kiểm tra NewsLogger nhận thông báo và ghi nhận log
        assertThat(logger.getLogHistory()).isNotEmpty();
        String lastLog = logger.getLastLog();
        assertThat(lastLog).contains("2 tin mới");
        assertThat(lastLog).contains("tổng cộng: 50 tin");
        assertThat(lastLog).contains("Tiêu đề tin mới 1");
    }

    @Test
    @DisplayName("[Observer] Hủy đăng ký (detach) ngăn Observer nhận các thông báo tiếp theo")
    void testDetachObserver() {
        NewsFeed newsFeed = new NewsFeed();
        UnreadCounter counter = new UnreadCounter();
        newsFeed.attach(counter);

        // Đợt thông báo 1: 3 bài viết
        newsFeed.notifyObservers(List.of(
                new Article("1", "Tin 1", "M", "N", "url1", null, LocalDateTime.now(), "S", "C"),
                new Article("2", "Tin 2", "M", "N", "url2", null, LocalDateTime.now(), "S", "C"),
                new Article("3", "Tin 3", "M", "N", "url3", null, LocalDateTime.now(), "S", "C")
        ), 3);
        assertThat(counter.getLastNewCount()).isEqualTo(3);

        // [Observer] Hủy đăng ký
        newsFeed.detach(counter);
        assertThat(newsFeed.getObservers()).doesNotContain(counter);

        // Đợt thông báo 2: 5 bài viết
        newsFeed.notifyObservers(List.of(
                new Article("4", "Tin 4", "M", "N", "url4", null, LocalDateTime.now(), "S", "C")
        ), 4);

        // Số đếm của counter phải giữ nguyên đợt 1, không nhận đợt 2
        assertThat(counter.getLastNewCount()).isEqualTo(3);
    }
}
