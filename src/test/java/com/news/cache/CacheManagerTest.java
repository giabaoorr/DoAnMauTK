package com.news.cache;

import com.news.model.Article;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

class CacheManagerTest {

    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager = CacheManager.getInstance();
        cacheManager.clear();
    }

    @Test
    @DisplayName("[Singleton] Đảm bảo chỉ có duy nhất một thể hiện của CacheManager được tạo ra")
    void testSingletonInstanceIdentity() {
        CacheManager instance1 = CacheManager.getInstance();
        CacheManager instance2 = CacheManager.getInstance();

        assertThat(instance1).isNotNull();
        assertThat(instance2).isNotNull();
        // Kiểm tra cùng tham chiếu trong bộ nhớ (same instance)
        assertThat(instance1).isSameAs(instance2);
    }

    @Test
    @DisplayName("[Singleton] Kiểm tra Thread-safety: 50 luồng đồng thời gọi getInstance() trả về cùng một instance")
    void testThreadSafetySingletonConcurrentAccess() throws InterruptedException {
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        List<CacheManager> instances = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    // Đợi cờ hiệu phát lệnh để tất cả luồng chạy đồng thời cùng 1 microsecond
                    startLatch.await();
                    CacheManager inst = CacheManager.getInstance();
                    instances.add(inst);
                } catch (InterruptedException ignored) {
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // Bắn tín hiệu bắt đầu cho tất cả luồng cùng lao vào lấy instance
        startLatch.countDown();
        boolean completed = finishLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).isTrue();
        assertThat(instances).hasSize(threadCount);

        CacheManager first = instances.get(0);
        for (CacheManager inst : instances) {
            assertThat(inst).isSameAs(first);
        }
    }

    @Test
    @DisplayName("[Singleton] Kiểm tra Thread-safety: nhiều luồng đồng thời ghi và đọc cache không bị xung đột")
    void testConcurrentPutAndGetOperations() throws InterruptedException {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int id = i;
            executor.submit(() -> {
                try {
                    String key = "feed-url-" + (id % 5);
                    Article art = new Article("art-" + id, "Tiêu đề " + id, "Mô tả", "Nội dung",
                            "http://test.com/" + id, null, LocalDateTime.now(), "Nguồn", "Chuyên mục");

                    cacheManager.put(key, List.of(art));
                    List<Article> cached = cacheManager.get(key);
                    assertThat(cached).isNotNull();
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean finished = latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(finished).isTrue();
        assertThat(cacheManager.size()).isGreaterThan(0);
    }

    @Test
    @DisplayName("[Cache] Kiểm tra cơ chế tự động hết hạn (TTL) và xóa cache")
    void testTtlExpiration() throws InterruptedException {
        Article art = new Article("exp-1", "Tin hết hạn", "Mô tả", "Nội dung",
                "http://test.com/exp", null, LocalDateTime.now(), "Nguồn", "Mục");

        String testKey = "temp-short-lived-key";
        // Đặt TTL cực ngắn: 100ms
        cacheManager.put(testKey, List.of(art), Duration.ofMillis(100));

        // Kiểm tra ngay lập tức: còn trong cache
        assertThat(cacheManager.contains(testKey)).isTrue();
        assertThat(cacheManager.get(testKey)).isNotEmpty();

        // Chờ 200ms để TTL hết hạn
        Thread.sleep(200);

        // Sau khi hết hạn: get() trả về null và entry bị xóa
        assertThat(cacheManager.get(testKey)).isNull();
        assertThat(cacheManager.contains(testKey)).isFalse();
    }

    @Test
    @DisplayName("[Cache] Invalidate và Clear xóa chính xác dữ liệu")
    void testInvalidateAndClear() {
        Article art = new Article("1", "Tin 1", "M", "N", "u1", null, LocalDateTime.now(), "S", "C");

        cacheManager.put("KEY_1", List.of(art));
        cacheManager.put("KEY_2", List.of(art));

        assertThat(cacheManager.size()).isEqualTo(2);

        cacheManager.invalidate("KEY_1");
        assertThat(cacheManager.contains("KEY_1")).isFalse();
        assertThat(cacheManager.contains("KEY_2")).isTrue();

        cacheManager.clear();
        assertThat(cacheManager.size()).isEqualTo(0);
    }
}
