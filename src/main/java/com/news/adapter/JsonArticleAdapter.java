package com.news.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.news.model.Article;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * [Adapter] Concrete Adapter chuyển đổi dữ liệu thô JsonNode từ file JSON
 * thành đối tượng Article chuẩn hóa của hệ thống.
 *
 * Mẫu thiết kế:
 * - Target: com.news.adapter.ArticleAdapter
 * - Adaptee: com.fasterxml.jackson.databind.JsonNode
 * - Adapter: com.news.adapter.JsonArticleAdapter
 */
public class JsonArticleAdapter implements ArticleAdapter<JsonNode> {

    @Override
    public Article adapt(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }

        String id = node.hasNonNull("id") ? node.get("id").asText() : "";
        String title = node.hasNonNull("title") ? node.get("title").asText() : "Không có tiêu đề";
        String description = node.hasNonNull("description") ? node.get("description").asText() : "";
        String content = node.hasNonNull("content") ? node.get("content").asText() : description;
        String url = node.hasNonNull("url") ? node.get("url").asText() : "";
        String imageUrl = node.hasNonNull("imageUrl") ? node.get("imageUrl").asText() : null;
        String source = node.hasNonNull("source") ? node.get("source").asText() : "Nguồn khác";
        String category = node.hasNonNull("category") ? node.get("category").asText() : "Tổng hợp";

        LocalDateTime publishedDate = LocalDateTime.now();
        if (node.hasNonNull("publishedDate")) {
            String dateStr = node.get("publishedDate").asText();
            try {
                publishedDate = LocalDateTime.parse(dateStr);
            } catch (Exception ignored) {
                try {
                    publishedDate = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
                } catch (Exception ex) {
                    publishedDate = LocalDateTime.now();
                }
            }
        }

        if (id.isEmpty()) {
            id = "json-" + Integer.toHexString(Math.abs((url + title).hashCode()));
        }

        return new Article(id, title, description, content, url, imageUrl, publishedDate, source, category);
    }
}
