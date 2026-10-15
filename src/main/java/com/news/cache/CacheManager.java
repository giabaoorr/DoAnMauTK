package com.news.cache;

import com.news.model.Article;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * [Singleton] CacheManager tự xây dựng bằng Java thuần, đảm bảo thread-safe.
 * Áp dụng kỹ thuật Double-Checked Locking với biến volatile để đảm bảo an toàn đa luồng.
 * Lưu trữ danh sách tin tức đã tải để tối ưu hiệu năng và giảm tải kết nối mạng.
 */
public class CacheManager {

    private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);

    // [Singleton] Biến instance volatile đảm bảo tính hiển thị giữa các luồng
    private static volatile CacheManager instance;

    // Bộ nhớ cache lưu theo key (thread-safe ConcurrentHashMap)
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    // Thống kê hiệu năng
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);

    // Thời gian hết hạn mặc định: 15 phút
    private Duration defaultTtl = Duration.ofMinutes(15);

    // [Singleton] Constructor private ngăn chặn khởi tạo từ bên ngoài
    private CacheManager() {
        if (instance != null) {
            throw new IllegalStateException("Singleton CacheManager đã tồn tại! Vui lòng dùng getInstance().");
        }
        logger.info("[Singleton] Khởi tạo thể hiện duy nhất của CacheManager thành công.");
    }

    /**
     * [Singleton] Điểm truy cập toàn cục lấy thể hiện duy nhất (Double-Checked Locking).
     *
     * @return Thể hiện duy nhất của CacheManager
     */
    public static CacheManager getInstance() {
        if (instance == null) {
            synchronized (CacheManager.class) {
                if (instance == null) {
                    instance = new CacheManager();
                }
            }
        }
        return instance;
    }

    /**
     * Lưu danh sách bài viết vào cache với TTL mặc định.
     */
    public void put(String key, List<Article> articles) {
        put(key, articles, defaultTtl);
    }

    /**
     * Lưu danh sách bài viết vào cache với TTL tùy chọn.
     */
    public void put(String key, List<Article> articles, Duration ttl) {
        if (key == null || articles == null) return;
        cache.put(key, new CacheEntry(articles, ttl));
        logger.debug("[Singleton:CacheManager] Đã lưu vào cache key='{}' với {} bài viết, TTL={}s",
                key, articles.size(), ttl.toSeconds());
    }

    /**
     * Lấy danh sách bài viết từ cache nếu còn hạn (TTL).
     *
     * @param key Khóa cache
     * @return Danh sách bài viết nếu tìm thấy và chưa hết hạn, hoặc null nếu không có / hết hạn
     */
    public List<Article> get(String key) {
        if (key == null) return null;

        CacheEntry entry = cache.get(key);
        if (entry == null) {
            missCount.incrementAndGet();
            return null;
        }

        if (entry.isExpired()) {
            cache.remove(key);
            missCount.incrementAndGet();
            logger.debug("[Singleton:CacheManager] Cache entry key='{}' đã hết hạn và bị xóa.", key);
            return null;
        }

        hitCount.incrementAndGet();
        return entry.getArticles();
    }

    public boolean contains(String key) {
        if (key == null) return false;
        CacheEntry entry = cache.get(key);
        if (entry == null) return false;
        if (entry.isExpired()) {
            cache.remove(key);
            return false;
        }
        return true;
    }

    public void invalidate(String key) {
        if (key != null) {
            cache.remove(key);
            logger.debug("[Singleton:CacheManager] Đã xóa cache key='{}'", key);
        }
    }

    public void clear() {
        cache.clear();
        logger.info("[Singleton:CacheManager] Đã làm rỗng toàn bộ cache.");
    }

    public int size() {
        return cache.size();
    }

    public long getHitCount() {
        return hitCount.get();
    }

    public long getMissCount() {
        return missCount.get();
    }

    public double getHitRate() {
        long hits = hitCount.get();
        long misses = missCount.get();
        long total = hits + misses;
        if (total == 0) return 0.0;
        return (double) hits / total;
    }

    public void setDefaultTtl(Duration defaultTtl) {
        this.defaultTtl = defaultTtl;
    }

    public Duration getDefaultTtl() {
        return defaultTtl;
    }
}
