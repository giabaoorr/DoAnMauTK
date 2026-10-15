package com.news.factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.news.adapter.JsonArticleAdapter;
import com.news.model.Article;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * [Factory Method] Concrete Product xử lý đọc và phân tích tin từ nguồn JSON mẫu hoặc JSON API.
 * Kết hợp với JsonArticleAdapter để tạo danh sách Article.
 */
public class JsonNewsParser implements NewsParser {

    private static final int TIMEOUT_MS = 4000;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<Article> parse(InputStream inputStream, String defaultSource, String defaultCategory) throws Exception {
        List<Article> result = new ArrayList<>();
        JsonNode rootNode = objectMapper.readTree(inputStream);
        JsonArticleAdapter adapter = new JsonArticleAdapter();

        if (rootNode != null && rootNode.isArray()) {
            for (JsonNode itemNode : rootNode) {
                Article article = adapter.adapt(itemNode);
                if (article != null) {
                    if (article.getSource() == null || article.getSource().isEmpty()) {
                        article.setSource(defaultSource);
                    }
                    if (article.getCategory() == null || article.getCategory().isEmpty()) {
                        article.setCategory(defaultCategory);
                    }
                    result.add(article);
                }
            }
        }
        return result;
    }

    @Override
    public List<Article> parseUrl(String urlString, String defaultSource, String defaultCategory) throws Exception {
        URL url = URI.create(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("HTTP lỗi phản hồi: " + responseCode + " từ " + urlString);
        }

        try (InputStream inputStream = conn.getInputStream()) {
            return parse(inputStream, defaultSource, defaultCategory);
        } finally {
            conn.disconnect();
        }
    }
}
