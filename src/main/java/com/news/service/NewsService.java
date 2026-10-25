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
    private final BookmarkService bookmarkService;

    // [Observer] Khởi tạo Subject và các Observers bằng Java thuần
    private final com.news.observer.NewsFeed newsFeed = new com.news.observer.NewsFeed();
    private final com.news.observer.UnreadCounter unreadCounter = new com.news.observer.UnreadCounter();
    private final com.news.observer.NewsLogger newsLogger = new com.news.observer.NewsLogger();

    public NewsService(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
        initDefaultFeedSources();

        // [Observer] Đăng ký (attach) các Observer vào Subject
        newsFeed.attach(unreadCounter);
        newsFeed.attach(newsLogger);
    }

    /**
     * [Decorator] Bọc Article bằng các Concrete Decorator tùy theo ngữ cảnh và trạng thái:
     * - BookmarkedArticle: nếu bài viết đã được lưu
     * - FeaturedArticle: nếu bài viết thuộc nhóm tin tiêu điểm / từ khóa nổi bật
     * - SummarizedArticle: tạo đoạn trích dẫn súc tích và tính toán thời gian đọc
     */
    private final Set<String> manualFeaturedIds = Collections.synchronizedSet(new HashSet<>());

    /**
     * [Decorator] Bọc Article bằng các Concrete Decorator tùy theo ngữ cảnh và trạng thái:
     * - BookmarkedArticle: nếu bài viết đã được lưu
     * - FeaturedArticle: nếu bài viết thuộc nhóm tin tiêu điểm / từ khóa nổi bật
     * - SummarizedArticle: tạo đoạn trích dẫn súc tích và tính toán thời gian đọc
     */
    public Article applyDecorators(Article article) {
        if (article == null) return null;
        Article decorated = article;

        // 1. [Decorator] Bọc BookmarkedArticle nếu người dùng đã lưu
        if (bookmarkService != null && bookmarkService.isBookmarked(article.getId())) {
            decorated = new com.news.decorator.BookmarkedArticle(decorated);
        }

        // 2. [Decorator] Bọc FeaturedArticle nếu là tin bài tiêu điểm nổi bật
        if (isHighlightArticle(article)) {
            decorated = new com.news.decorator.FeaturedArticle(decorated);
        }

        // 3. [Decorator] Bọc SummarizedArticle để tối ưu phần mô tả tóm tắt
        decorated = new com.news.decorator.SummarizedArticle(decorated, 160);

        return decorated;
    }

    private boolean isHighlightArticle(Article a) {
        if (a == null) return false;
        if (a.isFeatured() || (a.getId() != null && manualFeaturedIds.contains(a.getId()))) {
            return true;
        }
        if (a.getTitle() == null) return false;
        String t = a.getTitle().toLowerCase();
        return t.contains("kỷ lục") || t.contains("đột phá") || t.contains("khởi công") || t.contains("ai")
                || t.contains("thủ tướng") || t.contains("quốc tế") || t.contains("tiêu điểm") || t.contains("nóng")
                || t.contains("công nghệ") || t.contains("kinh tế") || t.contains("việt nam") || t.contains("đặc biệt")
                || t.contains("bất ngờ") || t.contains("thế giới");
    }

    public boolean toggleFeatured(String articleId) {
        if (articleId == null) return false;
        if (manualFeaturedIds.contains(articleId)) {
            manualFeaturedIds.remove(articleId);
            return false;
        } else {
            manualFeaturedIds.add(articleId);
            return true;
        }
    }

    private void initDefaultFeedSources() {
        // Nguồn VnExpress
        feedSources.add(new FeedSource("VnExpress", "Thời sự", "https://vnexpress.net/rss/thoi-su.rss", SourceType.RSS));
        feedSources.add(new FeedSource("VnExpress", "Thế giới", "https://vnexpress.net/rss/the-gioi.rss", SourceType.RSS));
        feedSources.add(new FeedSource("VnExpress", "Kinh doanh", "https://vnexpress.net/rss/kinh-doanh.rss", SourceType.RSS));
        feedSources.add(new FeedSource("VnExpress", "Công nghệ", "https://vnexpress.net/rss/so-hoa.rss", SourceType.RSS));
        feedSources.add(new FeedSource("VnExpress", "Thể thao", "https://vnexpress.net/rss/the-thao.rss", SourceType.RSS));
        feedSources.add(new FeedSource("VnExpress", "Giải trí", "https://vnexpress.net/rss/giai-tri.rss", SourceType.RSS));
        feedSources.add(new FeedSource("VnExpress", "Sức khỏe", "https://vnexpress.net/rss/suc-khoe.rss", SourceType.RSS));
        feedSources.add(new FeedSource("VnExpress", "Giáo dục", "https://vnexpress.net/rss/giao-duc.rss", SourceType.RSS));
        feedSources.add(new FeedSource("VnExpress", "Du lịch", "https://vnexpress.net/rss/du-lich.rss", SourceType.RSS));
        feedSources.add(new FeedSource("VnExpress", "Pháp luật", "https://vnexpress.net/rss/phap-luat.rss", SourceType.RSS));

        // Nguồn Tuổi Trẻ
        feedSources.add(new FeedSource("Tuổi Trẻ", "Thời sự", "https://tuoitre.vn/rss/thoi-su.rss", SourceType.RSS));
        feedSources.add(new FeedSource("Tuổi Trẻ", "Thế giới", "https://tuoitre.vn/rss/the-gioi.rss", SourceType.RSS));
        feedSources.add(new FeedSource("Tuổi Trẻ", "Kinh doanh", "https://tuoitre.vn/rss/kinh-doanh.rss", SourceType.RSS));
        feedSources.add(new FeedSource("Tuổi Trẻ", "Công nghệ", "https://tuoitre.vn/rss/nhip-song-so.rss", SourceType.RSS));
        feedSources.add(new FeedSource("Tuổi Trẻ", "Thể thao", "https://tuoitre.vn/rss/the-thao.rss", SourceType.RSS));
        feedSources.add(new FeedSource("Tuổi Trẻ", "Giải trí", "https://tuoitre.vn/rss/giai-tri.rss", SourceType.RSS));
        feedSources.add(new FeedSource("Tuổi Trẻ", "Sức khỏe", "https://tuoitre.vn/rss/suc-khoe.rss", SourceType.RSS));
        feedSources.add(new FeedSource("Tuổi Trẻ", "Giáo dục", "https://tuoitre.vn/rss/giao-duc.rss", SourceType.RSS));
        feedSources.add(new FeedSource("Tuổi Trẻ", "Du lịch", "https://tuoitre.vn/rss/du-lich.rss", SourceType.RSS));
        feedSources.add(new FeedSource("Tuổi Trẻ", "Pháp luật", "https://tuoitre.vn/rss/phap-luat.rss", SourceType.RSS));
    }

    @PostConstruct
    public void init() {
        refreshArticles();
    }

    /**
     * Tải lại toàn bộ bài viết từ các nguồn RSS thật.
     * Sử dụng Singleton CacheManager để lưu trữ và quản lý dữ liệu trong bộ nhớ.
     * Nếu lỗi mạng hoặc không có kết nối, tự động kích hoạt chế độ dự phòng đọc từ JSON mẫu.
     *
     * @return Số lượng tin bài mới tải được
     */
    public synchronized int refreshArticles() {
        return refreshArticles(false);
    }

    /**
     * @param useCacheNếu true thì ưu tiên lấy từ CacheManager trước; nếu false thì xóa cache tải mới
     */
    public synchronized int refreshArticles(boolean useCache) {
        // [Singleton] Sử dụng CacheManager
        com.news.cache.CacheManager cacheManager = com.news.cache.CacheManager.getInstance();

        if (useCache) {
            List<Article> cached = cacheManager.get("ALL_ARTICLES");
            if (cached != null && !cached.isEmpty()) {
                logger.info("[Singleton:CacheManager] Tải {} bài viết trực tiếp từ Cache.", cached.size());
                this.articles.clear();
                this.articles.addAll(cached);
                return articles.size();
            }
        }

        logger.info("Bắt đầu nạp tin tức từ các nguồn RSS...");
        Map<String, Article> articleMap = new LinkedHashMap<>();
        int successFeedCount = 0;

        // [Factory Method] Lấy parser phù hợp qua NewsParserFactory
        NewsParser rssParser = NewsParserFactory.createParser(SourceType.RSS);

        for (FeedSource feed : feedSources) {
            try {
                List<Article> feedArticles = null;
                if (useCache) {
                    feedArticles = cacheManager.get(feed.getUrl());
                }

                if (feedArticles == null) {
                    // [Factory Method & Adapter] Parse URL và chuyển đổi về Article qua Adapter
                    feedArticles = rssParser.parseUrl(feed.getUrl(), feed.getName(), feed.getCategory());
                    if (feedArticles != null && !feedArticles.isEmpty()) {
                        // [Singleton] Lưu từng nguồn vào cache
                        cacheManager.put(feed.getUrl(), feedArticles);
                    }
                }

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

        Set<String> oldUrls = articles.stream()
                .map(a -> a != null ? a.getUrl() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (successFeedCount > 0 && !articleMap.isEmpty()) {
            this.fallbackMode = false;

            // Tìm danh sách các bài viết mới chưa có trước đó
            List<Article> newArticles = new ArrayList<>();
            for (Article a : articleMap.values()) {
                if (a.getUrl() != null && !oldUrls.contains(a.getUrl())) {
                    newArticles.add(a);
                }
            }

            this.articles.clear();
            this.articles.addAll(articleMap.values());

            // [Singleton] Lưu danh sách hợp nhất vào CacheManager
            cacheManager.put("ALL_ARTICLES", new ArrayList<>(this.articles));

            logger.info("Hoàn tất tải tin tức trực tuyến: {} bài viết tổng cộng từ {} nguồn RSS", articles.size(), successFeedCount);

            // [Observer] Subject phát thông báo cho UnreadCounter và NewsLogger
            newsFeed.notifyObservers(newArticles, articles.size());

            return articles.size();
        } else {
            // [Chế độ dự phòng] Nếu không có mạng hoặc tất cả nguồn RSS bị lỗi
            logger.warn("Không thể kết nối Internet hoặc các nguồn RSS lỗi. Kích hoạt chế độ dự phòng (fallback) từ sample JSON...");
            int count = loadFallbackArticles();
            cacheManager.put("ALL_ARTICLES", new ArrayList<>(this.articles));
            newsFeed.notifyObservers(articles, articles.size());
            return count;
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
        return articles.stream()
                .map(this::applyDecorators)
                .collect(Collectors.toList());
    }

    /**
     * [Strategy & Decorator] Lấy danh sách bài viết sau khi xử lý qua các chiến lược sắp xếp và lọc,
     * đồng thời áp dụng Decorator pattern cho từng bài viết.
     */
    public List<Article> getProcessedArticles(String category, String source, String sort) {
        return getProcessedArticles(category, source, sort, false);
    }

    public List<Article> getProcessedArticles(String category, String source, String sort, Boolean onlyFeatured) {
        // [Strategy] Khởi tạo Context và nạp các chiến lược theo lựa chọn
        com.news.strategy.ArticleProcessorContext context =
                com.news.strategy.ArticleProcessorContext.fromUserPreferences(category, source, sort);
        List<Article> processed = context.process(new ArrayList<>(this.articles));

        // [Decorator] Áp dụng các decorator cho bài viết kết quả
        List<Article> decorated = processed.stream()
                .map(this::applyDecorators)
                .collect(Collectors.toList());

        if (Boolean.TRUE.equals(onlyFeatured)) {
            return decorated.stream()
                    .filter(a -> a != null && a.isFeatured())
                    .collect(Collectors.toList());
        }

        return decorated;
    }

    /**
    /**
     * [Decorator: FeaturedArticle] Lấy danh sách các bài viết tiêu điểm nổi bật theo Chuyên mục và Nguồn báo.
     * Áp dụng tính năng Tin Tiêu Điểm & Nổi Bật đồng bộ với bộ lọc Strategy của người dùng.
     *
     * @param category Tên chuyên mục (hoặc null/rỗng nếu xem tất cả)
     * @param source Tên nguồn báo (hoặc null/rỗng nếu xem tất cả nguồn)
     * @param limit Số lượng bài tiêu điểm cần lấy (mặc định 3: 1 hero lớn + 2 tin phụ)
     * @return Danh sách bài viết được bọc bởi FeaturedArticle Decorator
     */
    public List<Article> getFeaturedArticles(String category, String source, int limit) {
        if (limit <= 0) limit = 3;

        boolean hasCategory = (category != null && !category.trim().isEmpty());
        boolean hasSource = (source != null && !source.trim().isEmpty());

        List<Article> pool = articles.stream()
                .filter(a -> a != null
                        && (!hasCategory || (category != null && category.equalsIgnoreCase(a.getCategory())))
                        && (!hasSource || (source != null && source.equalsIgnoreCase(a.getSource()))))
                .sorted(Comparator.comparing(
                        a -> a != null ? a.getPublishedDate() : null,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .collect(Collectors.toList());

        List<Article> featured = new ArrayList<>();
        Set<String> pickedCategories = new HashSet<>();
        Set<String> pickedIds = new HashSet<>();

        if (!hasCategory) {
            // Khi xem "Mới nhất" (Toàn trang):
            // Ưu tiên chọn 3 bài tiêu điểm từ 3 chuyên mục KHÁC NHAU để khu vực tiêu điểm trang chủ
            // phong phú đa diện (ví dụ: 1 Thời sự, 1 Thế giới/Kinh doanh, 1 Sức khỏe/Công nghệ/Thể thao),
            // tạo sự khác biệt hoàn toàn với khi người dùng bấm vào xem riêng từng chuyên mục Thời sự.
            for (Article a : pool) {
                if (a == null || a.getId() == null) continue;
                if (featured.size() >= limit) break;
                String cat = a.getCategory() != null ? a.getCategory().trim() : "Khác";
                if (isHighlightArticle(a) && !pickedCategories.contains(cat) && !pickedIds.contains(a.getId())) {
                    featured.add(applyDecorators(a));
                    pickedCategories.add(cat);
                    pickedIds.add(a.getId());
                }
            }
        } else {
            // Khi xem một chuyên mục cụ thể (ví dụ: Thời sự, Kinh doanh, Công nghệ...):
            // Chọn các bài viết tiêu điểm nổi bật nhất trong chính chuyên mục đó.
            for (Article a : pool) {
                if (a == null || a.getId() == null) continue;
                if (featured.size() >= limit) break;
                if (isHighlightArticle(a) && !pickedIds.contains(a.getId())) {
                    featured.add(applyDecorators(a));
                    pickedIds.add(a.getId());
                }
            }
        }

        // Bổ sung các bài từ pool nếu chưa đủ limit
        if (featured.size() < limit && !pool.isEmpty()) {
            for (Article a : pool) {
                if (a == null || a.getId() == null) continue;
                if (featured.size() >= limit) break;
                if (!pickedIds.contains(a.getId())) {
                    Article baseDecorated = applyDecorators(a);
                    Article manuallyFeatured = (baseDecorated instanceof com.news.decorator.FeaturedArticle)
                            ? baseDecorated
                            : new com.news.decorator.FeaturedArticle(baseDecorated);
                    featured.add(manuallyFeatured);
                    pickedIds.add(a.getId());
                }
            }
        }
        return featured;
    }

    public List<Article> getFeaturedArticlesForCategory(String category, int limit) {
        return getFeaturedArticles(category, null, limit);
    }

    /**
     * [Decorator: FeaturedArticle] Lấy danh sách các bài viết tiêu điểm nổi bật toàn trang.
     */
    public List<Article> getFeaturedArticles(int limit) {
        return getFeaturedArticles(null, null, limit);
    }

    /**
     * Lấy các bài viết gom nhóm theo danh mục chuẩn để hiển thị các khối chuyên mục phong cách VnExpress.
     * Áp dụng Decorator cho từng bài viết để đồng bộ các thuộc tính mở rộng.
     *
     * @param perCategoryLimit Số bài tối đa cho mỗi khối chuyên mục (1 bài tiêu biểu + các bài tin vắn)
     * @return Map liên kết: Tên chuyên mục -> Danh sách bài viết
     */
    public Map<String, List<Article>> getCategoryBlocks(int perCategoryLimit) {
        if (perCategoryLimit <= 0) perCategoryLimit = 4;
        Map<String, List<Article>> blocks = new LinkedHashMap<>();
        for (String cat : EDITORIAL_CATEGORY_ORDER) {
            List<Article> catArticles = articles.stream()
                    .filter(a -> a != null && cat.equalsIgnoreCase(a.getCategory()))
                    .map(this::applyDecorators)
                    .limit(perCategoryLimit)
                    .collect(Collectors.toList());
            if (!catArticles.isEmpty()) {
                blocks.put(cat, catArticles);
            }
        }
        return blocks;
    }

    public Optional<Article> getArticleById(String id) {
        if (id == null) return Optional.empty();
        return articles.stream()
                .filter(a -> id.equals(a.getId()))
                .findFirst()
                .map(this::applyDecorators);
    }

    private static final List<String> EDITORIAL_CATEGORY_ORDER = List.of(
            "Thời sự", "Thế giới", "Kinh doanh", "Công nghệ", "Thể thao",
            "Giải trí", "Sức khỏe", "Giáo dục", "Du lịch", "Pháp luật"
    );

    public List<String> getAllCategories() {
        Set<String> found = articles.stream()
                .map(a -> a != null ? a.getCategory() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<String> ordered = new java.util.ArrayList<>();
        for (String cat : EDITORIAL_CATEGORY_ORDER) {
            if (found.contains(cat)) {
                ordered.add(cat);
            }
        }
        found.stream().sorted().forEach(cat -> {
            if (!ordered.contains(cat)) {
                ordered.add(cat);
            }
        });
        return ordered;
    }

    public List<String> getAllSources() {
        return articles.stream()
                .map(a -> a != null ? a.getSource() : null)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public boolean isFallbackMode() {
        return fallbackMode;
    }

    public com.news.observer.NewsFeed getNewsFeed() {
        return newsFeed;
    }

    public com.news.observer.UnreadCounter getUnreadCounter() {
        return unreadCounter;
    }

    public com.news.observer.NewsLogger getNewsLogger() {
        return newsLogger;
    }
}
