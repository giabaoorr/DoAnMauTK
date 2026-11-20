package com.news.controller;

import com.news.model.Article;
import com.news.model.User;
import com.news.service.BookmarkService;
import com.news.service.NewsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller xử lý các request giao diện web Thymeleaf.
 * Controller chỉ gọi Service theo đúng kiến trúc phân lớp.
 */
@Controller
public class NewsController {

    private final NewsService newsService;
    private final BookmarkService bookmarkService;

    public NewsController(NewsService newsService, BookmarkService bookmarkService) {
        this.newsService = newsService;
        this.bookmarkService = bookmarkService;
    }

    /**
     * Trang chủ hiển thị danh sách tin tức với bộ lọc và sắp xếp (Strategy Pattern),
     * kết hợp bài viết tiêu điểm nổi bật (FeaturedArticle Decorator) theo từng chủ đề.
     */
    @GetMapping("/")
    public String index(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "source", required = false) String source,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "sort", required = false, defaultValue = "newest") String sort,
            HttpSession session,
            Model model) {

        User currentUser = (User) session.getAttribute("currentUser");
        Long currentUserId = (currentUser != null) ? currentUser.getId() : null;

        // [Strategy] Lấy danh sách bài viết đã qua lọc và sắp xếp (kết hợp Chuyên mục, Nguồn báo, Từ khóa tìm kiếm)
        List<Article> processedArticles = newsService.getProcessedArticles(category, source, keyword, sort, currentUserId);

        // [Decorator: FeaturedArticle] Lấy 3 bài tiêu điểm nổi bật phù hợp cho cả chủ đề, nguồn báo và từ khóa
        List<Article> featuredArticles = newsService.getFeaturedArticles(category, source, keyword, 3, currentUserId);
        Set<String> featuredIds = featuredArticles.stream()
                .filter(Objects::nonNull)
                .map(Article::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Danh sách bài viết còn lại cho Grid (không lặp lại bài viết đã nằm ở vị trí Tiêu điểm hàng đầu)
        List<Article> gridArticles = processedArticles.stream()
                .filter(a -> a != null && a.getId() != null && !featuredIds.contains(a.getId()))
                .collect(Collectors.toList());

        model.addAttribute("featuredArticles", featuredArticles);
        model.addAttribute("articles", gridArticles.isEmpty() ? processedArticles : gridArticles);
        model.addAttribute("categories", newsService.getAllCategories());
        model.addAttribute("sources", newsService.getAllSources());
        model.addAttribute("totalArticles", newsService.getAllArticles().size());
        model.addAttribute("filteredCount", processedArticles.size());
        model.addAttribute("selectedCategory", category != null ? category : "");
        model.addAttribute("selectedSource", source != null ? source : "");
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("selectedSort", sort != null ? sort : "newest");
        model.addAttribute("categoryBlocks", newsService.getCategoryBlocks(4));
        model.addAttribute("fallbackMode", newsService.isFallbackMode());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("bookmarkCount", bookmarkService.getBookmarkCount(currentUserId));
        return "index";
    }

    /**
     * Xem chi tiết một bài viết kèm danh sách bài viết liên quan.
     */
    @GetMapping("/article/{id}")
    public String viewArticle(@PathVariable("id") String id, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        Long currentUserId = (currentUser != null) ? currentUser.getId() : null;

        Optional<Article> articleOpt = newsService.getArticleById(id, currentUserId);
        if (articleOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Không tìm thấy bài viết yêu cầu!");
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("bookmarkCount", bookmarkService.getBookmarkCount(currentUserId));
            return "error/404";
        }
        Article currentArticle = articleOpt.get();
        model.addAttribute("article", currentArticle);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("bookmarkCount", bookmarkService.getBookmarkCount(currentUserId));

        // Lấy 4 bài viết liên quan cùng chuyên mục
        List<Article> related = newsService.getAllArticles().stream()
                .filter(a -> a.getCategory() != null && a.getCategory().equalsIgnoreCase(currentArticle.getCategory()))
                .filter(a -> !id.equals(a.getId()))
                .limit(4)
                .collect(Collectors.toList());
        model.addAttribute("relatedArticles", related);

        // Lấy 5 bài viết thịnh hành cho Sidebar chi tiết
        List<Article> trending = newsService.getAllArticles().stream()
                .filter(a -> a != null && !id.equals(a.getId()))
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("trendingArticles", trending);

        return "detail";
    }

    /**
     * [Decorator] Trang hiển thị danh sách bài viết đã đánh dấu (BookmarkedArticle).
     */
    @GetMapping("/bookmarks")
    public String bookmarks(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        Long currentUserId = (currentUser != null) ? currentUser.getId() : null;

        List<Article> bookmarkedArticles = bookmarkService.getBookmarkedArticles(currentUserId);
        model.addAttribute("articles", bookmarkedArticles);
        model.addAttribute("bookmarkCount", bookmarkedArticles.size());
        model.addAttribute("currentUser", currentUser);
        return "bookmarks";
    }

    /**
     * [Decorator & H2 Storage / JSON File Storage] Thao tác Đánh dấu / Bỏ đánh dấu bài viết.
     */
    @PostMapping("/bookmarks/toggle")
    public String toggleBookmark(
            @RequestParam("articleId") String articleId,
            @RequestParam(name = "returnUrl", defaultValue = "/") String returnUrl,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");
        Long currentUserId = (currentUser != null) ? currentUser.getId() : null;

        // Tìm bài viết trong NewsService hoặc từ BookmarkService
        Optional<Article> articleOpt = newsService.getArticleById(articleId, currentUserId);
        if (articleOpt.isEmpty()) {
            articleOpt = bookmarkService.getBookmarkedArticles(currentUserId).stream()
                    .filter(a -> articleId.equals(a.getId()))
                    .findFirst();
        }

        if (articleOpt.isPresent()) {
            boolean isBookmarked = bookmarkService.toggleBookmark(articleOpt.get(), currentUserId);
            if (isBookmarked) {
                String targetInfo = (currentUser != null) ? " vào tài khoản @" + currentUser.getUsername() : "";
                redirectAttributes.addFlashAttribute("message", "Đã lưu bài viết vào danh sách đánh dấu" + targetInfo + "!");
            } else {
                redirectAttributes.addFlashAttribute("message", "Đã gỡ bài viết khỏi danh sách đánh dấu!");
            }
        }

        return "redirect:" + (returnUrl.startsWith("/") ? returnUrl : "/");
    }

    /**
     * [AJAX API] Đánh dấu / Bỏ đánh dấu bài viết không cần tải lại toàn bộ trang web.
     * Trả về JSON để hiển thị Toast popup và cập nhật trạng thái nút bấm, số lượng tức thì.
     */
    @PostMapping("/api/bookmarks/toggle")
    @ResponseBody
    public Map<String, Object> toggleBookmarkAjax(
            @RequestParam("articleId") String articleId,
            HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUser");
        Long currentUserId = (currentUser != null) ? currentUser.getId() : null;

        Optional<Article> articleOpt = newsService.getArticleById(articleId, currentUserId);
        if (articleOpt.isEmpty()) {
            articleOpt = bookmarkService.getBookmarkedArticles(currentUserId).stream()
                    .filter(a -> articleId.equals(a.getId()))
                    .findFirst();
        }

        Map<String, Object> response = new HashMap<>();
        if (articleOpt.isPresent()) {
            boolean isBookmarked = bookmarkService.toggleBookmark(articleOpt.get(), currentUserId);
            int count = bookmarkService.getBookmarkCount(currentUserId);
            String targetInfo = (currentUser != null) ? " (tài khoản @" + currentUser.getUsername() + ")" : "";
            String message = isBookmarked
                    ? "Đã lưu bài viết vào danh sách đánh dấu" + targetInfo + "!"
                    : "Đã gỡ bài viết khỏi danh sách đánh dấu!";

            response.put("success", true);
            response.put("bookmarked", isBookmarked);
            response.put("count", count);
            response.put("message", message);
            return response;
        }

        response.put("success", false);
        response.put("message", "Không tìm thấy bài viết!");
        return response;
    }

    /**
     * [Decorator: FeaturedArticle] Thao tác Đánh dấu / Bỏ đánh dấu bài viết Nổi Bật.
     */
    @PostMapping("/featured/toggle")
    public String toggleFeatured(
            @RequestParam("articleId") String articleId,
            @RequestParam(name = "returnUrl", defaultValue = "/") String returnUrl,
            RedirectAttributes redirectAttributes) {

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
    @PostMapping("/refresh")
    public String refresh(RedirectAttributes redirectAttributes) {
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
