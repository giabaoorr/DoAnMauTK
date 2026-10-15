package com.news.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.news.model.Article;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleAdapterTest {

    @Test
    @DisplayName("[Adapter] RssEntryAdapter chuyển đổi SyndEntry thành công và làm sạch dữ liệu")
    void testRssEntryAdapter() {
        // [Adapter] Adaptee thô: SyndEntry từ Rome RSS
        SyndEntry entry = new SyndEntryImpl();
        entry.setTitle("Tin tức mới từ VnExpress");
        entry.setLink("https://vnexpress.net/thoi-su/bai-viet-moi-1234.html");
        entry.setPublishedDate(new Date());

        SyndContentImpl desc = new SyndContentImpl();
        desc.setValue("<a href='https://vnexpress.net'><img src='https://i1-vnexpress.vnecdn.net/2026/10/15/hinh-anh.jpg'></a>Đây là phần mô tả đã được lọc sạch mã HTML.");
        entry.setDescription(desc);

        // [Adapter] Thực thi Adapter
        RssEntryAdapter adapter = new RssEntryAdapter("VnExpress", "Thời sự");
        Article article = adapter.adapt(entry);

        // Kiểm tra kết quả chuyển đổi về Article chuẩn
        assertThat(article).isNotNull();
        assertThat(article.getId()).isNotBlank();
        assertThat(article.getTitle()).isEqualTo("Tin tức mới từ VnExpress");
        assertThat(article.getUrl()).isEqualTo("https://vnexpress.net/thoi-su/bai-viet-moi-1234.html");
        assertThat(article.getSource()).isEqualTo("VnExpress");
        assertThat(article.getDescription()).isEqualTo("Đây là phần mô tả đã được lọc sạch mã HTML.");
        assertThat(article.getImageUrl()).isEqualTo("https://i1-vnexpress.vnecdn.net/2026/10/15/hinh-anh.jpg");
        assertThat(article.getPublishedDate()).isNotNull();
    }

    @Test
    @DisplayName("[Adapter] JsonArticleAdapter chuyển đổi JsonNode thành Article chuẩn")
    void testJsonArticleAdapter() throws Exception {
        String json = """
                {
                    "id": "json-test-99",
                    "title": "Tin từ JSON Adapter",
                    "description": "Mô tả ngắn",
                    "content": "Nội dung đầy đủ chi tiết",
                    "url": "https://tuoitre.vn/bai-viet-99.htm",
                    "imageUrl": "https://cdn.tuoitre.vn/img.png",
                    "publishedDate": "2026-10-15T12:00:00",
                    "source": "Tuổi Trẻ",
                    "category": "Kinh doanh"
                }
                """;

        ObjectMapper mapper = new ObjectMapper();
        var jsonNode = mapper.readTree(json);

        JsonArticleAdapter adapter = new JsonArticleAdapter();
        Article article = adapter.adapt(jsonNode);

        assertThat(article).isNotNull();
        assertThat(article.getId()).isEqualTo("json-test-99");
        assertThat(article.getTitle()).isEqualTo("Tin từ JSON Adapter");
        assertThat(article.getDescription()).isEqualTo("Mô tả ngắn");
        assertThat(article.getContent()).isEqualTo("Nội dung đầy đủ chi tiết");
        assertThat(article.getSource()).isEqualTo("Tuổi Trẻ");
        assertThat(article.getCategory()).isEqualTo("Kinh doanh");
        assertThat(article.getImageUrl()).isEqualTo("https://cdn.tuoitre.vn/img.png");
        assertThat(article.getPublishedDate()).isEqualTo(LocalDateTime.of(2026, 10, 15, 12, 0, 0));
    }
}
