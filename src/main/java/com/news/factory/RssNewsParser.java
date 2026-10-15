package com.news.factory;

import com.news.adapter.RssEntryAdapter;
import com.news.model.Article;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * [Factory Method] Concrete Product xử lý đọc và phân tích tin từ nguồn RSS XML.
 * Sử dụng thư viện Rome và kết hợp với RssEntryAdapter để tạo danh sách Article.
 */
public class RssNewsParser implements NewsParser {

    private static final int TIMEOUT_MS = 4000;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    @Override
    public List<Article> parse(InputStream inputStream, String defaultSource, String defaultCategory) throws Exception {
        List<Article> result = new ArrayList<>();
        SyndFeedInput feedInput = new SyndFeedInput();
        // Cho phép xử lý XML linh hoạt kể cả khi định dạng feed có cảnh báo
        feedInput.setAllowDoctypes(true);

        try (XmlReader reader = new XmlReader(inputStream)) {
            SyndFeed feed = feedInput.build(reader);
            RssEntryAdapter adapter = new RssEntryAdapter(defaultSource, defaultCategory);

            for (SyndEntry entry : feed.getEntries()) {
                Article article = adapter.adapt(entry);
                if (article != null && article.getTitle() != null && !article.getTitle().isEmpty()) {
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
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept", "application/rss+xml, application/xml, text/xml, */*");

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
