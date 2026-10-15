package com.news.factory;

/**
 * [Factory Method] Abstract Creator trong mẫu thiết kế Factory Method.
 * Khai báo factory method createParser() trả về đối tượng NewsParser.
 * Các lớp con cụ thể sẽ quyết định lớp sản phẩm cụ thể (RssNewsParser hoặc JsonNewsParser) được khởi tạo.
 */
public abstract class NewsParserFactory {

    /**
     * [Factory Method] Phương thức xuất xưởng bộ phân tích tin tức.
     *
     * @return Một thể hiện của NewsParser
     */
    public abstract NewsParser createParser();

    /**
     * Phương thức tiện ích lấy Concrete Factory tương ứng với SourceType.
     *
     * @param type Loại nguồn dữ liệu (RSS hoặc JSON)
     * @return Concrete Factory thích hợp
     */
    public static NewsParserFactory getFactory(SourceType type) {
        if (type == null) {
            throw new IllegalArgumentException("SourceType không được để null");
        }
        return switch (type) {
            case RSS -> new RssParserFactory();
            case JSON -> new JsonParserFactory();
        };
    }

    /**
     * Tiện ích gọi nhanh Factory Method theo loại nguồn.
     */
    public static NewsParser createParser(SourceType type) {
        return getFactory(type).createParser();
    }
}
