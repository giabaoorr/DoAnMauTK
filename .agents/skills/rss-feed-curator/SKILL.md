---
name: rss-feed-curator
description: Hướng dẫn quản trị, bóc tách và tích hợp nguồn RSS trực tuyến cho các báo điện tử Việt Nam (VnExpress, Tuổi Trẻ, v.v.).
---

# RSS Feed Curator - Cẩm Nang Quản Trị Nguồn Báo Chí

Skill này hướng dẫn quy trình kiểm tra, bóc tách an toàn và mở rộng các kênh RSS đa nguồn trong ứng dụng Java sử dụng thư viện ROME và Jsoup.

## 1. Danh Sách Kênh RSS Đã Xác Thực Khả Dụng (100% Hoạt Động)

### Nguồn VnExpress:
- Thời sự: `https://vnexpress.net/rss/thoi-su.rss`
- Thế giới: `https://vnexpress.net/rss/the-gioi.rss`
- Kinh doanh: `https://vnexpress.net/rss/kinh-doanh.rss`
- Công nghệ: `https://vnexpress.net/rss/so-hoa.rss`
- Thể thao: `https://vnexpress.net/rss/the-thao.rss`
- Giải trí: `https://vnexpress.net/rss/giai-tri.rss`
- Sức khỏe: `https://vnexpress.net/rss/suc-khoe.rss`
- Giáo dục: `https://vnexpress.net/rss/giao-duc.rss`
- Du lịch: `https://vnexpress.net/rss/du-lich.rss`
- Pháp luật: `https://vnexpress.net/rss/phap-luat.rss`

### Nguồn Tuổi Trẻ:
- Thời sự: `https://tuoitre.vn/rss/thoi-su.rss`
- Thế giới: `https://tuoitre.vn/rss/the-gioi.rss`
- Kinh doanh: `https://tuoitre.vn/rss/kinh-doanh.rss`
- Công nghệ: `https://tuoitre.vn/rss/nhip-song-so.rss`
- Thể thao: `https://tuoitre.vn/rss/the-thao.rss`
- Giải trí: `https://tuoitre.vn/rss/giai-tri.rss`
- Sức khỏe: `https://tuoitre.vn/rss/suc-khoe.rss`
- Giáo dục: `https://tuoitre.vn/rss/giao-duc.rss`
- Du lịch: `https://tuoitre.vn/rss/du-lich.rss`
- Pháp luật: `https://tuoitre.vn/rss/phap-luat.rss`

## 2. Kỹ Thuật Bóc Tách Hình Ảnh và Mô Tả

RSS của các báo Việt Nam thường đặt ảnh đại diện trong:
1. Thẻ `<enclosure url="..." type="image/..." />`
2. Hoặc nằm bên trong mã HTML của thẻ `<description><![CDATA[<a href="..."><img src="..."/></a>...]]></description>`

**Quy trình xử lý an toàn bằng Jsoup trong Adapter:**
```java
// Bóc tách URL hình ảnh từ chuỗi HTML CDATA
Document doc = Jsoup.parse(rawDescription);
Element img = doc.selectFirst("img");
if (img != null) {
    imageUrl = img.attr("src");
}
// Làm sạch mô tả để chỉ lấy văn bản thuần
String cleanText = doc.text();
```

## 3. Cơ Chế Dự Phòng Ngoại Tuyến (Offline Fallback)

Nếu gặp sự cố mạng hoặc tường lửa chặn RSS:
- `NewsService` bắt ngoại lệ `Exception`.
- Tự động gọi `NewsParserFactory.createParser(SourceType.JSON)`.
- Nạp bộ tin tức mẫu từ `sample/news-sample.json` để ứng dụng luôn chạy trơn tru, không sập khi thuyết trình.
