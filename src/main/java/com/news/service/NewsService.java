package com.news.service;

import com.news.factory.NewsParser;
import com.news.factory.NewsParserFactory;
import com.news.factory.SourceType;
import com.news.model.Article;
import com.news.model.FeedSource;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Service quản lý tin tức trung tâm của hệ thống.
 * Kết nối các mẫu thiết kế: Factory Method, Adapter, Strategy, Observer, Decorator, Cache.
 */
@Service
public class NewsService {
    private static final Logger logger = LoggerFactory.getLogger(NewsService.class);

    private final List<Article> articles = new CopyOnWriteArrayList<>();
    private final List<FeedSource> feedSources = new ArrayList<>();
    private boolean fallbackMode = false;

    public NewsService() {
        initDefaultFeedSources();
    }

    private void initDefaultFeedSources() {
        // Nguồn VnExpress
        feedSources.add(new FeedSource("VnExpress", "Thời sự", "https://vnexpress.net/rss/tin-moi-nhat.rss", SourceType.RSS));
        feedSources.add(new FeedSource("VnExpress", "Kinh doanh", "https://vnexpress.net/rss/kinh-doanh.rss", SourceType.RSS));
        feedSources.add(new FeedSource("VnExpress", "Công nghệ", "https://vnexpress.net/rss/so-hoa.rss", SourceType.RSS));
        feedSources.add(new FeedSource("VnExpress", "Thể thao", "https://vnexpress.net/rss/the-thao.rss", SourceType.RSS));
        feedSources.add(new FeedSource("VnExpress", "Giải trí", "https://vnexpress.net/rss/giai-tri.rss", SourceType.RSS));

        // Nguồn Tuổi Trẻ
        feedSources.add(new FeedSource("Tuổi Trẻ", "Thời sự", "https://tuoitre.vn/rss/tin-moi-nhat.rss", SourceType.RSS));
        feedSources.add(new FeedSource("Tuổi Trẻ", "Kinh doanh", "https://tuoitre.vn/rss/kinh-doanh.rss", SourceType.RSS));
        feedSources.add(new FeedSource("Tuổi Trẻ", "Công nghệ", "https://tuoitre.vn/rss/nhip-song-so.rss", SourceType.RSS));
        feedSources.add(new FeedSource("Tuổi Trẻ", "Thể thao", "https://tuoitre.vn/rss/the-thao.rss", SourceType.RSS));
        feedSources.add(new FeedSource("Tuổi Trẻ", "Giải trí", "https://tuoitre.vn/rss/giai-tri.rss", SourceType.RSS));
    }

    @PostConstruct
    public void init() {
        refreshArticles();
    }

    /**
     * Tải lại toàn bộ bài viết từ các nguồn RSS thật.
     * Nếu lỗi mạng hoặc không có kết nối, tự động kích hoạt chế độ dự phòng đọc từ JSON mẫu.
     *
     * @return Số lượng tin bài mới tải được
     */
    public synchronized int refreshArticles() {
        logger.info("Bắt đầu nạp tin tức từ các nguồn RSS...");
        Map<String, Article> articleMap = new LinkedHashMap<>();
        int successFeedCount = 0;

        // [Factory Method] Lấy parser phù hợp qua NewsParserFactory
        NewsParser rssParser = NewsParserFactory.createParser(SourceType.RSS);

        for (FeedSource feed : feedSources) {
            try {
                // [Factory Method & Adapter] Parse URL và chuyển đổi về Article qua Adapter
                List<Article> feedArticles = rssParser.parseUrl(feed.getUrl(), feed.getName(), feed.getCategory());
                if (feedArticles != null && !feedArticles.isEmpty()) {
                    for (Article a : feedArticles) {
                        if (a.getUrl() != null && !a.getUrl().isEmpty()) {
                            articleMap.putIfAbsent(a.getUrl(), a);
                        } else {
                            articleMap.putIfAbsent(a.getId(), a);
                        }
                    }
                    successFeedCount++;
                    logger.info("Đã tải {} bài viết từ [{} - {}]", feedArticles.size(), feed.getName(), feed.getCategory());
                }
            } catch (Exception e) {
                logger.warn("Không thể tải RSS từ [{} - {}]: {}", feed.getName(), feed.getUrl(), e.getMessage());
            }
        }

        if (successFeedCount > 0 && !articleMap.isEmpty()) {
            this.fallbackMode = false;
            this.articles.clear();
            this.articles.addAll(articleMap.values());
            logger.info("Hoàn tất tải tin tức trực tuyến: {} bài viết tổng cộng từ {} nguồn RSS", articles.size(), successFeedCount);
            return articles.size();
        } else {
            // [Chế độ dự phòng] Nếu không có mạng hoặc tất cả nguồn RSS bị lỗi
            logger.warn("Không thể kết nối Internet hoặc các nguồn RSS lỗi. Kích hoạt chế độ dự phòng (fallback) từ sample JSON...");
            return loadFallbackArticles();
        }
    }

    /**
     * Chế độ dự phòng: Sử dụng Factory Method để lấy JsonNewsParser và đọc file news-sample.json.
     */
    public synchronized int loadFallbackArticles() {
        try {
            // [Factory Method] Tạo bộ phân tích JsonNewsParser bằng Factory Method
            NewsParser jsonParser = NewsParserFactory.createParser(SourceType.JSON);

            ClassPathResource resource = new ClassPathResource("sample/news-sample.json");
            try (InputStream inputStream = resource.getInputStream()) {
                List<Article> sampleList = jsonParser.parse(inputStream, "Mẫu Tin Tức", "Tổng hợp");
                this.fallbackMode = true;
                this.articles.clear();
                if (sampleList != null) {
                    this.articles.addAll(sampleList);
                }
                logger.info("Đã kích hoạt chế độ dự phòng thành công: nạp {} bài viết mẫu từ news-sample.json", articles.size());
                return articles.size();
            }
        } catch (Exception e) {
            logger.error("Lỗi nghiêm trọng khi nạp dữ liệu mẫu dự phòng: {}", e.getMessage(), e);
            return 0;
        }
    }

    public List<Article> getAllArticles() {
        return Collections.unmodifiableList(new ArrayList<>(articles));
    }

    public Optional<Article> getArticleById(String id) {
        if (id == null) return Optional.empty();
        return articles.stream()
                .filter(a -> id.equals(a.getId()))
                .findFirst();
    }

    public List<String> getAllCategories() {
        return articles.stream()
                .map(Article::getCategory)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getAllSources() {
        return articles.stream()
                .map(Article::getSource)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public boolean isFallbackMode() {
        return fallbackMode;
    }
}
