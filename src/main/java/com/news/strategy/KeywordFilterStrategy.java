package com.news.strategy;

import com.news.model.Article;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * [Strategy] Concrete Strategy: Lọc bài viết theo từ khóa tìm kiếm (trong tiêu đề, tóm tắt hoặc nội dung).
 * Hỗ trợ tìm kiếm thông minh không phân biệt hoa thường và không phân biệt dấu tiếng Việt.
 */
public class KeywordFilterStrategy implements FilterStrategy {

    private final String keyword;
    private final String normalizedKeyword;

    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    public KeywordFilterStrategy(String keyword) {
        this.keyword = (keyword != null) ? keyword.trim() : "";
        this.normalizedKeyword = removeAccents(this.keyword).toLowerCase();
    }

    @Override
    public List<Article> filter(List<Article> articles) {
        if (articles == null) return List.of();
        if (keyword.isEmpty()) {
            return articles;
        }
        return articles.stream()
                .filter(this::matches)
                .collect(Collectors.toList());
    }

    @Override
    public boolean matches(Article article) {
        if (article == null || keyword.isEmpty()) return true;

        String title = article.getTitle() != null ? article.getTitle() : "";
        String desc = article.getDescription() != null ? article.getDescription() : "";
        String content = article.getContent() != null ? article.getContent() : "";

        // 1. Kiểm tra khớp trực tiếp (case-insensitive)
        String lowerKeyword = keyword.toLowerCase();
        if (title.toLowerCase().contains(lowerKeyword)
                || desc.toLowerCase().contains(lowerKeyword)
                || content.toLowerCase().contains(lowerKeyword)) {
            return true;
        }

        // 2. Kiểm tra khớp không dấu (Vietnamese accent-insensitive)
        if (!normalizedKeyword.isEmpty()) {
            String normTitle = removeAccents(title).toLowerCase();
            String normDesc = removeAccents(desc).toLowerCase();
            String normContent = removeAccents(content).toLowerCase();

            return normTitle.contains(normalizedKeyword)
                    || normDesc.contains(normalizedKeyword)
                    || normContent.contains(normalizedKeyword);
        }

        return false;
    }

    /**
     * Tiện ích loại bỏ dấu tiếng Việt để tìm kiếm linh hoạt ("Ha Noi" khớp với "Hà Nội").
     */
    public static String removeAccents(String text) {
        if (text == null) return "";
        String nfd = Normalizer.normalize(text, Normalizer.Form.NFD);
        return DIACRITICS_PATTERN.matcher(nfd)
                .replaceAll("")
                .replace('đ', 'd')
                .replace('Đ', 'D');
    }

    public String getKeyword() {
        return keyword;
    }
}
