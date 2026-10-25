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

    @Test
    @DisplayName("GĐ4 - Gửi POST /refresh kích hoạt làm mới và chuyển hướng về trang chủ")
    void testRefreshEndpoint() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/refresh"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("refreshMessage"));

        // Xác nhận Observer UnreadCounter và NewsLogger đã ghi nhận
        assertThat(newsService.getUnreadCounter()).isNotNull();
        assertThat(newsService.getNewsLogger().getLogHistory()).isNotEmpty();
    }

    @Test
    @DisplayName("GĐ5 - Truy cập trang /bookmarks và thực hiện toggle bookmark")
    void testBookmarksEndpoint() throws Exception {
        mockMvc.perform(get("/bookmarks"))
                .andExpect(status().isOk())
                .andExpect(view().name("bookmarks"))
                .andExpect(model().attributeExists("articles"))
                .andExpect(model().attributeExists("bookmarkCount"));

        List<Article> articles = newsService.getAllArticles();
        assertThat(articles).isNotEmpty();
        String articleId = articles.get(0).getId();

        // Toggle bookmark
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/bookmarks/toggle")
                        .param("articleId", articleId)
                        .param("returnUrl", "/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("GĐ2 - Lọc kết hợp Chuyên mục Thời sự và Nguồn Tuổi Trẻ: Cả Tiêu điểm và Grid đều phải thuộc Tuổi Trẻ")
    @SuppressWarnings("unchecked")
    void testFilterCategoryAndSource() throws Exception {
        var result = mockMvc.perform(get("/")
                        .param("category", "Thời sự")
                        .param("source", "Tuổi Trẻ"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("selectedCategory", "Thời sự"))
                .andExpect(model().attribute("selectedSource", "Tuổi Trẻ"))
                .andReturn();

        List<Article> featured = (List<Article>) result.getModelAndView().getModel().get("featuredArticles");
        System.out.println("DEBUG: featured size = " + (featured != null ? featured.size() : "null"));
        assertThat(featured).isNotEmpty();
        for (Article a : featured) {
            System.out.println("DEBUG: featured article = " + a.getTitle() + " | source=" + a.getSource() + " | cat=" + a.getCategory());
            assertThat(a.getSource()).isEqualTo("Tuổi Trẻ");
            assertThat(a.getCategory()).isEqualTo("Thời sự");
        }

        List<Article> grid = (List<Article>) result.getModelAndView().getModel().get("articles");
        System.out.println("DEBUG: grid size = " + (grid != null ? grid.size() : "null"));
        assertThat(grid).isNotEmpty();
        for (Article a : grid) {
            assertThat(a.getSource()).isEqualTo("Tuổi Trẻ");
            assertThat(a.getCategory()).isEqualTo("Thời sự");
        }
    }
}
