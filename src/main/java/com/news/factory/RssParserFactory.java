package com.news.factory;

/**
 * [Factory Method] Concrete Creator tạo ra RssNewsParser để phân tích nguồn RSS XML.
 */
public class RssParserFactory extends NewsParserFactory {

    @Override
    public NewsParser createParser() {
        // [Factory Method] Tạo sản phẩm cụ thể RssNewsParser
        return new RssNewsParser();
    }
}
