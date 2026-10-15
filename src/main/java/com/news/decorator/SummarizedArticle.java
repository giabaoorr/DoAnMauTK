package com.news.decorator;

import com.news.model.Article;

/**
 * [Decorator] Concrete Decorator: Bọc bài viết để cung cấp bản tóm tắt nội dung cô đọng
 * kèm thời gian ước tính đọc bài viết.
 */
public class SummarizedArticle extends ArticleDecorator {

    private final int maxDescriptionLength;

    public SummarizedArticle(Article article) {
        this(article, 140);
    }

    public SummarizedArticle(Article article, int maxDescriptionLength) {
        super(article);
        this.maxDescriptionLength = maxDescriptionLength;
    }

    /**
     * [Decorator] Rút gọn phần mô tả để tạo đoạn tóm tắt súc tích, dễ đọc nhanh.
     */
    @Override
    public String getDescription() {
        String baseDesc = decoratedArticle.getDescription();
        if (baseDesc == null || baseDesc.isEmpty()) {
            return "Không có mô tả.";
        }

        String cleaned = baseDesc.replaceAll("\\s+", " ").trim();
        if (cleaned.length() <= maxDescriptionLength) {
            return cleaned;
        }

        // Cắt đến từ hoàn chỉnh gần nhất
        int cutIndex = cleaned.lastIndexOf(' ', maxDescriptionLength);
        if (cutIndex <= 0) {
            cutIndex = maxDescriptionLength;
        }
        return cleaned.substring(0, cutIndex) + "...";
    }

    /**
     * Phương thức mở rộng: Tính toán thời gian đọc ước tính dựa trên độ dài nội dung.
     */
    public String getEstimatedReadingTime() {
        String content = decoratedArticle.getContent();
        if (content == null || content.isEmpty()) {
            content = decoratedArticle.getDescription();
        }
        if (content == null || content.isEmpty()) {
            return "1 phút đọc";
        }

        int words = content.trim().split("\\s+").length;
        int minutes = Math.max(1, (int) Math.ceil(words / 180.0));
        return minutes + " phút đọc";
    }

    public String getSummary() {
        return "⏱ ~" + getEstimatedReadingTime() + " • " + getDescription();
    }
}
