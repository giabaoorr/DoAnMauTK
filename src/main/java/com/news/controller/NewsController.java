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
     * Trang chủ hiển thị danh sách tin tức.
     */
    @GetMapping("/")
    public String index(Model model) {
        List<Article> articles = newsService.getAllArticles();
        model.addAttribute("articles", articles);
        model.addAttribute("categories", newsService.getAllCategories());
        model.addAttribute("sources", newsService.getAllSources());
        model.addAttribute("totalArticles", articles.size());
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
}
