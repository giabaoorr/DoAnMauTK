package com.news.controller;

import com.news.model.Article;
import com.news.service.NewsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Controller xử lý các request giao diện web Thymeleaf.
 * Controller chỉ gọi Service theo đúng kiến trúc phân lớp.
 */
@Controller
public class NewsController {

    private final NewsService newsService;
    private final com.news.service.BookmarkService bookmarkService;

    public NewsController(NewsService newsService, com.news.service.BookmarkService bookmarkService) {
        this.newsService = newsService;
        this.bookmarkService = bookmarkService;
    }

    /**
     * Trang chủ hiển thị danh sách tin tức với bộ lọc và sắp xếp (Strategy Pattern),
     * kết hợp bài viết tiêu điểm nổi bật (FeaturedArticle Decorator) theo từng chủ đề.
     */
    @GetMapping("/")
    public String index(
            @org.springframework.web.bind.annotation.RequestParam(name = "category", required = false) String category,
            @org.springframework.web.bind.annotation.RequestParam(name = "source", required = false) String source,
            @org.springframework.web.bind.annotation.RequestParam(name = "sort", required = false, defaultValue = "newest") String sort,
            Model model) {

        // [Strategy] Lấy danh sách bài viết đã qua lọc và sắp xếp
        List<Article> processedArticles = newsService.getProcessedArticles(category, source, sort);

        // [Decorator: FeaturedArticle] Lấy 3 bài tiêu điểm nổi bật phù hợp cho cả chủ đề VÀ nguồn báo đang lọc
        List<Article> featuredArticles = newsService.getFeaturedArticles(category, source, 3);
        Set<String> featuredIds = featuredArticles.stream()
                .filter(java.util.Objects::nonNull)
                .map(a -> a.getId())
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        // Danh sách bài viết còn lại cho Grid (không lặp lại bài viết đã nằm ở vị trí Tiêu điểm hàng đầu)
        List<Article> gridArticles = processedArticles.stream()
                .filter(a -> a != null && a.getId() != null && !featuredIds.contains(a.getId()))
                .collect(java.util.stream.Collectors.toList());

        model.addAttribute("featuredArticles", featuredArticles);
        model.addAttribute("articles", gridArticles.isEmpty() ? processedArticles : gridArticles);
        model.addAttribute("categories", newsService.getAllCategories());
        model.addAttribute("sources", newsService.getAllSources());
        model.addAttribute("totalArticles", newsService.getAllArticles().size());
        model.addAttribute("filteredCount", processedArticles.size());
        model.addAttribute("selectedCategory", category != null ? category : "");
        model.addAttribute("selectedSource", source != null ? source : "");
        model.addAttribute("selectedSort", sort != null ? sort : "newest");
        model.addAttribute("categoryBlocks", newsService.getCategoryBlocks(4));
        model.addAttribute("fallbackMode", newsService.isFallbackMode());
        model.addAttribute("bookmarkCount", bookmarkService.getBookmarkCount());
        return "index";
    }

    /**
     * Xem chi tiết một bài viết kèm danh sách bài viết liên quan.
     */
    @GetMapping("/article/{id}")
    public String viewArticle(@PathVariable("id") String id, Model model) {
        Optional<Article> articleOpt = newsService.getArticleById(id);
        if (articleOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Không tìm thấy bài viết yêu cầu!");
            model.addAttribute("bookmarkCount", bookmarkService.getBookmarkCount());
            return "error/404";
        }
        Article currentArticle = articleOpt.get();
        model.addAttribute("article", currentArticle);
        model.addAttribute("bookmarkCount", bookmarkService.getBookmarkCount());

        // Lấy 4 bài viết liên quan cùng chuyên mục
        List<Article> related = newsService.getAllArticles().stream()
                .filter(a -> a.getCategory() != null && a.getCategory().equalsIgnoreCase(currentArticle.getCategory()))
                .filter(a -> !id.equals(a.getId()))
                .limit(4)
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("relatedArticles", related);

        // Lấy 5 bài viết thịnh hành cho Sidebar chi tiết
        List<Article> trending = newsService.getAllArticles().stream()
                .filter(a -> a != null && !id.equals(a.getId()))
                .limit(5)
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("trendingArticles", trending);

        return "detail";
    }

    /**
     * [Decorator] Trang hiển thị danh sách bài viết đã đánh dấu (BookmarkedArticle).
     */
    @GetMapping("/bookmarks")
    public String bookmarks(Model model) {
        List<Article> bookmarkedArticles = bookmarkService.getBookmarkedArticles();
        model.addAttribute("articles", bookmarkedArticles);
        model.addAttribute("bookmarkCount", bookmarkedArticles.size());
        return "bookmarks";
    }

    /**
     * [Decorator & JSON File Storage] Thao tác Đánh dấu / Bỏ đánh dấu bài viết.
     */
    @org.springframework.web.bind.annotation.PostMapping("/bookmarks/toggle")
    public String toggleBookmark(
            @org.springframework.web.bind.annotation.RequestParam("articleId") String articleId,
            @org.springframework.web.bind.annotation.RequestParam(name = "returnUrl", defaultValue = "/") String returnUrl,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        // Tìm bài viết trong NewsService hoặc từ BookmarkService
        Optional<Article> articleOpt = newsService.getArticleById(articleId);
        if (articleOpt.isEmpty()) {
            // Có thể đang ở trang bookmarks mà tin chưa được tải trong bộ nhớ chính
            articleOpt = bookmarkService.getBookmarkedArticles().stream()
                    .filter(a -> articleId.equals(a.getId()))
                    .findFirst();
        }

        if (articleOpt.isPresent()) {
            boolean isBookmarked = bookmarkService.toggleBookmark(articleOpt.get());
            if (isBookmarked) {
                redirectAttributes.addFlashAttribute("message", "Đã lưu bài viết vào danh sách đánh dấu!");
            } else {
                redirectAttributes.addFlashAttribute("message", "Đã gỡ bài viết khỏi danh sách đánh dấu!");
            }
        }

        return "redirect:" + (returnUrl.startsWith("/") ? returnUrl : "/");
    }

    /**
     * [Decorator: FeaturedArticle] Thao tác Đánh dấu / Bỏ đánh dấu bài viết Nổi Bật.
     */
    @org.springframework.web.bind.annotation.PostMapping("/featured/toggle")
    public String toggleFeatured(
            @org.springframework.web.bind.annotation.RequestParam("articleId") String articleId,
            @org.springframework.web.bind.annotation.RequestParam(name = "returnUrl", defaultValue = "/") String returnUrl,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        boolean isFeatured = newsService.toggleFeatured(articleId);
        if (isFeatured) {
            redirectAttributes.addFlashAttribute("message", "Đã ghim bài viết vào danh sách [NỔI BẬT] (FeaturedArticle Decorator)!");
        } else {
            redirectAttributes.addFlashAttribute("message", "Đã bỏ ghim bài viết khỏi danh sách [NỔI BẬT]!");
        }

        return "redirect:" + (returnUrl.startsWith("/") ? returnUrl : "/");
    }

    /**
     * [Observer] Làm mới dữ liệu tin tức.
     * Khi nhấn nút, Service làm mới tin và Subject (NewsFeed) sẽ thông báo cho các Observer.
     */
    @org.springframework.web.bind.annotation.PostMapping("/refresh")
    public String refresh(org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        int total = newsService.refreshArticles();
        int newCount = newsService.getUnreadCounter().getLastNewCount();
        if (newCount > 0) {
            redirectAttributes.addFlashAttribute("refreshMessage",
                    "Đã làm mới thành công! [Observer] UnreadCounter ghi nhận " + newCount + " tin tức mới (Tổng cộng: " + total + " bài viết).");
        } else {
            redirectAttributes.addFlashAttribute("refreshMessage",
                    "Đã đồng bộ tin tức mới nhất từ các nguồn RSS (Tổng cộng: " + total + " bài viết).");
        }
        return "redirect:/";
    }
}
