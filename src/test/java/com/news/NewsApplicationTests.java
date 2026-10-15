package com.news;

import com.news.model.Article;
import com.news.service.NewsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class NewsApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NewsService newsService;

    @Test
    @DisplayName("GĐ1 - Khởi tạo Spring Context và nạp dữ liệu mẫu thành công")
    void contextLoadsAndSampleDataIsLoaded() {
        assertThat(newsService).isNotNull();
        List<Article> articles = newsService.getAllArticles();
        assertThat(articles).isNotEmpty();
        assertThat(articles.size()).isGreaterThanOrEqualTo(5);

        // Kiểm tra dữ liệu bài viết đầu tiên
        Article first = articles.get(0);
        assertThat(first.getId()).isNotBlank();
        assertThat(first.getTitle()).isNotBlank();
        assertThat(first.getSource()).isNotBlank();
        assertThat(first.getCategory()).isNotBlank();
        assertThat(first.getPublishedDate()).isNotNull();
    }

    @Test
    @DisplayName("GĐ1 - Truy cập trang chủ '/' trả về mã 200 và view index")
    void testIndexPageLoads() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("articles"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("sources"));
    }

    @Test
    @DisplayName("GĐ1 - Truy cập chi tiết bài viết hợp lệ")
    void testArticleDetailPage() throws Exception {
        List<Article> articles = newsService.getAllArticles();
        assertThat(articles).isNotEmpty();
        String validId = articles.get(0).getId();

        mockMvc.perform(get("/article/" + validId))
                .andExpect(status().isOk())
                .andExpect(view().name("detail"))
                .andExpect(model().attributeExists("article"));
    }

    @Test
    @DisplayName("GĐ1 - Truy cập bài viết không tồn tại trả về 404 view")
    void testArticleNotFound() throws Exception {
        mockMvc.perform(get("/article/non-existent-id-999"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attributeExists("errorMessage"));
    }
}
