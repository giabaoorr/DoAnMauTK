package com.news.factory;

import com.news.model.Article;

import java.io.InputStream;
import java.util.List;

/**
 * [Factory Method] Product Interface đại diện cho bộ phân tích cú pháp tin tức.
 */
public interface NewsParser {

    /**
     * Phân tích luồng dữ liệu thành danh sách bài viết.
     *
     * @param inputStream Luồng dữ liệu thô (XML RSS hoặc JSON)
     * @param defaultSource Tên nguồn mặc định (nếu không tự phát hiện được)
     * @param defaultCategory Chuyên mục mặc định
     * @return Danh sách bài viết chuẩn hóa
     * @throws Exception nếu xảy ra lỗi đọc/phân tích
     */
    List<Article> parse(InputStream inputStream, String defaultSource, String defaultCategory) throws Exception;

    /**
     * Tải và phân tích dữ liệu trực tiếp từ URL.
     *
     * @param url Đường dẫn đến nguồn RSS hoặc JSON
     * @param defaultSource Tên nguồn mặc định
     * @param defaultCategory Chuyên mục mặc định
     * @return Danh sách bài viết chuẩn hóa
     * @throws Exception nếu xảy ra lỗi mạng hoặc phân tích
     */
    List<Article> parseUrl(String url, String defaultSource, String defaultCategory) throws Exception;
}
