package com.news.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.news.model.Article;
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
 * Service quản lý tin tức chính của hệ thống.
 * Đóng vai trò cầu nối tích hợp các mẫu thiết kế: Factory, Adapter, Strategy,
 * Observer, Decorator, Cache.
 */
@Service
public class NewsService {
    private static final Logger logger = LoggerFactory.getLogger(NewsService.class);

    private final List<Article> articles = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper;

    public NewsService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @PostConstruct
    public void init() {
        loadSampleArticles();
    }

    /**
     * Tải dữ liệu dự phòng từ file JSON mẫu trong resources/sample/
     */
    public synchronized void loadSampleArticles() {
        try {
            ClassPathResource resource = new ClassPathResource("sample/news-sample.json");
            try (InputStream inputStream = resource.getInputStream()) {
                List<Article> sampleList = objectMapper.readValue(inputStream, new TypeReference<List<Article>>() {});
                articles.clear();
                if (sampleList != null) {
                    articles.addAll(sampleList);
                }
                logger.info("Đã nạp thành công {} bài viết mẫu từ news-sample.json", articles.size());
            }
        } catch (Exception e) {
            logger.error("Lỗi khi đọc file news-sample.json: {}", e.getMessage(), e);
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
}
