package com.news.factory;

/**
 * [Factory Method] Concrete Creator tạo ra JsonNewsParser để phân tích nguồn dữ liệu JSON.
 */
public class JsonParserFactory extends NewsParserFactory {

    @Override
    public NewsParser createParser() {
        // [Factory Method] Tạo sản phẩm cụ thể JsonNewsParser
        return new JsonNewsParser();
    }
}
