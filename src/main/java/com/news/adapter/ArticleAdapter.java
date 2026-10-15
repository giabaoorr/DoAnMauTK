package com.news.adapter;

import com.news.model.Article;

/**
 * [Adapter] Target Interface trong mẫu thiết kế Adapter.
 * Định nghĩa phương thức chuẩn để chuyển đổi đối tượng dữ liệu thô từ nguồn ngoài về lớp Article chung.
 *
 * @param <T> Kiểu dữ liệu thô nguồn (Adaptee)
 */
public interface ArticleAdapter<T> {

    /**
     * Chuyển đổi dữ liệu thô thành đối tượng Article chuẩn hóa.
     *
     * @param rawData Dữ liệu thô từ nguồn tin tức
     * @return Đối tượng Article chuẩn hóa của hệ thống
     */
    Article adapt(T rawData);
}
