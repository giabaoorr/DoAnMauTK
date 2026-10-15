package com.news.factory;

import com.news.model.Article;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NewsParserFactoryTest {

    @Test
    @DisplayName("[Factory Method] Kiểm tra tạo đúng Concrete Factory và Concrete Product theo SourceType")
    void testFactoryCreation() {
        // [Factory Method] Kiểm tra với nguồn RSS
        NewsParserFactory rssFactory = NewsParserFactory.getFactory(SourceType.RSS);
        assertThat(rssFactory).isInstanceOf(RssParserFactory.class);
        NewsParser rssParser = rssFactory.createParser();
        assertThat(rssParser).isInstanceOf(RssNewsParser.class);

        // [Factory Method] Kiểm tra với nguồn JSON
        NewsParserFactory jsonFactory = NewsParserFactory.getFactory(SourceType.JSON);
        assertThat(jsonFactory).isInstanceOf(JsonParserFactory.class);
        NewsParser jsonParser = jsonFactory.createParser();
        assertThat(jsonParser).isInstanceOf(JsonNewsParser.class);

        // Kiểm tra xử lý ngoại lệ khi truyền null
        assertThrows(IllegalArgumentException.class, () -> NewsParserFactory.getFactory(null));
    }

    @Test
    @DisplayName("[Factory Method] JsonNewsParser phân tích chuỗi JSON thành công")
    void testJsonNewsParser() throws Exception {
        NewsParser parser = NewsParserFactory.createParser(SourceType.JSON);
        String sampleJson = """
                [
                  {
                    "id": "test-01",
                    "title": "Bài viết thử nghiệm JSON",
                    "description": "Mô tả bài viết thử nghiệm",
                    "url": "https://example.com/test",
                    "source": "Nguồn Test",
                    "category": "Công nghệ"
                  }
                ]
                """;

        try (InputStream is = new ByteArrayInputStream(sampleJson.getBytes(StandardCharsets.UTF_8))) {
            List<Article> articles = parser.parse(is, "Mặc định", "Chung");
            assertThat(articles).hasSize(1);
            Article a = articles.get(0);
            assertThat(a.getId()).isEqualTo("test-01");
            assertThat(a.getTitle()).isEqualTo("Bài viết thử nghiệm JSON");
            assertThat(a.getSource()).isEqualTo("Nguồn Test");
            assertThat(a.getCategory()).isEqualTo("Công nghệ");
        }
    }

    @Test
    @DisplayName("[Factory Method] RssNewsParser phân tích cú pháp XML RSS thành công")
    void testRssNewsParser() throws Exception {
        NewsParser parser = NewsParserFactory.createParser(SourceType.RSS);
        String sampleRssXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <rss version="2.0">
                  <channel>
                    <title>Kênh Tin Tức Mẫu</title>
                    <link>https://vnexpress.net</link>
                    <description>Mô tả kênh</description>
                    <item>
                      <title>Tiêu đề tin tức RSS mẫu</title>
                      <link>https://vnexpress.net/tin-tuc-mau-12345.html</link>
                      <description><![CDATA[<a href="https://vnexpress.net"><img src="https://img.vnexpress.net/photo.jpg" /></a>Nội dung mô tả tóm tắt của tin RSS.]]></description>
                      <pubDate>Wed, 15 Oct 2026 08:00:00 +0700</pubDate>
                      <category>Thời sự</category>
                    </item>
                  </channel>
                </rss>
                """;

        try (InputStream is = new ByteArrayInputStream(sampleRssXml.getBytes(StandardCharsets.UTF_8))) {
            List<Article> articles = parser.parse(is, "VnExpress", "Thời sự");
            assertThat(articles).hasSize(1);
            Article a = articles.get(0);
            assertThat(a.getTitle()).isEqualTo("Tiêu đề tin tức RSS mẫu");
            assertThat(a.getUrl()).isEqualTo("https://vnexpress.net/tin-tuc-mau-12345.html");
            assertThat(a.getSource()).isEqualTo("VnExpress");
            assertThat(a.getDescription()).isEqualTo("Nội dung mô tả tóm tắt của tin RSS.");
            assertThat(a.getImageUrl()).isEqualTo("https://img.vnexpress.net/photo.jpg");
        }
    }
}
