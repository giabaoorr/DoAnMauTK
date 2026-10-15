package com.news.controller;

import com.news.model.Article;
import com.news.service.NewsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

/**
 * Controller xử lý các request giao diện web Thymeleaf.
 * Controller chỉ gọi Service theo đúng kiến trúc phân lớp.
 */
@Controller
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    /**
     * Trang chủ hiển thị danh sách tin tức với bộ lọc và sắp xếp (Strategy Pattern).
     */
    @GetMapping("/")
    public String index(
            @org.springframework.web.bind.annotation.RequestParam(name = "category", required = false) String category,
            @org.springframework.web.bind.annotation.RequestParam(name = "source", required = false) String source,
            @org.springframework.web.bind.annotation.RequestParam(name = "sort", required = false, defaultValue = "newest") String sort,
            Model model) {

        // [Strategy] Lấy danh sách bài viết đã qua lọc và sắp xếp
        List<Article> processedArticles = newsService.getProcessedArticles(category, source, sort);

        model.addAttribute("articles", processedArticles);
        model.addAttribute("categories", newsService.getAllCategories());
        model.addAttribute("sources", newsService.getAllSources());
        model.addAttribute("totalArticles", newsService.getAllArticles().size());
        model.addAttribute("filteredCount", processedArticles.size());
        model.addAttribute("selectedCategory", category != null ? category : "");
        model.addAttribute("selectedSource", source != null ? source : "");
        model.addAttribute("selectedSort", sort != null ? sort : "newest");
        model.addAttribute("fallbackMode", newsService.isFallbackMode());
        return "index";
    }

    /**
     * Xem chi tiết một bài viết.
     */
    @GetMapping("/article/{id}")
    public String viewArticle(@PathVariable("id") String id, Model model) {
        Optional<Article> articleOpt = newsService.getArticleById(id);
        if (articleOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Không tìm thấy bài viết yêu cầu!");
            return "error/404";
        }
        model.addAttribute("article", articleOpt.get());
        return "detail";
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
