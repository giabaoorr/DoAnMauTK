# Ứng Dụng Web Đọc Báo Đa Nguồn (News Reader)
> **Đồ án môn học:** Mẫu Thiết Kế (Design Patterns)  
> **Nền tảng:** Java 17, Spring Boot 3.3.4, Thymeleaf, Rome RSS 2.1.0, Jackson, JUnit 5.  
> **Chạy cục bộ:** `http://localhost:8080` (không cần cơ sở dữ liệu bên ngoài).

---

## I. BẢNG TỔNG HỢP: MẪU -> LỚP -> TÍNH NĂNG

| STT | Mẫu Thiết Kế | Phân Loại GoF | Các Lớp Chính Trong Code | Tính Năng Thực Tế Trong Ứng Dụng |
| :---: | :--- | :--- | :--- | :--- |
| **1** | **Factory Method** | Creational | `NewsParserFactory`, `RssParserFactory`, `JsonParserFactory`, `NewsParser`, `RssNewsParser`, `JsonNewsParser` | Khởi tạo đúng bộ phân tích cú pháp theo loại nguồn (RSS trực tuyến hoặc JSON mẫu) mà không phụ thuộc vào lớp cụ thể. |
| **2** | **Adapter** | Structural | `ArticleAdapter<T>`, `RssEntryAdapter`, `JsonArticleAdapter` | Chuyển đổi dữ liệu thô `SyndEntry` (Rome) và `JsonNode` (Jackson) về thực thể `Article` thống nhất; lọc sạch HTML và bóc tách ảnh. |
| **3** | **Strategy** | Behavioral | `SortStrategy` (`Newest`, `Oldest`, `Title`), `FilterStrategy` (`Category`, `Source`, `Composite`, `AllPass`), `ArticleProcessorContext` | Cho phép người dùng hoán đổi thuật toán sắp xếp (Mới nhất, Cũ nhất, Tiêu đề A-Z) và lọc tin (Chuyên mục, Nguồn báo) động lúc runtime trên web. |
| **4** | **Observer** | Behavioral | `NewsSubject`, `NewsFeed`, `NewsObserver`, `UnreadCounter`, `NewsLogger`, `NewsUpdateEvent` | Khi nhấn nút "Làm mới", `NewsFeed` (Subject) tự động phát tín hiệu cho `UnreadCounter` (đếm tin mới) và `NewsLogger` (ghi nhật ký hệ thống). |
| **5** | **Decorator** | Structural | `ArticleDecorator`, `FeaturedArticle`, `BookmarkedArticle`, `SummarizedArticle` | Bọc đối tượng `Article` để bổ sung tính năng động: gắn nhãn `NỔI BẬT` (tiêu đề sao `★`), trạng thái `ĐÃ LƯU`, và tạo đoạn tóm tắt kèm ước lượng thời gian đọc. |
| **6** | **Singleton** | Creational | `CacheManager`, `CacheEntry` | Quản lý bộ nhớ đệm tin tức duy nhất trong JVM, thread-safe (Double-Checked Locking), lưu trữ tin đã tải và hỗ trợ thời gian hết hạn (TTL). |

---

## II. DANH SÁCH CHỨC NĂNG CỦA ỨNG DỤNG

1. **Trang chủ đa nguồn:** Tổng hợp tin tức từ các báo điện tử uy tín (VnExpress, Tuổi Trẻ) qua chuẩn RSS Feed.
2. **Lọc tin tức linh hoạt:** Lọc theo Chuyên mục (Thời sự, Kinh doanh, Công nghệ, Thể thao, Giải trí) và theo Nguồn báo (VnExpress, Tuổi Trẻ) hoặc kết hợp cả hai.
3. **Sắp xếp đa tiêu chí:** Sắp xếp tin mới nhất, cũ nhất, hoặc theo thứ tự chữ cái tiêu đề (A-Z).
4. **Xem chi tiết bài viết:** Hiển thị bài viết đầy đủ với ảnh lớn, chuyên mục, ngày giờ xuất bản và liên kết xem bài gốc trên trang báo.
5. **Đánh dấu bài viết (Bookmark):** Đánh dấu bài viết yêu thích bằng 1 cú nhấp chuột; quản lý danh sách tại trang `/bookmarks`. Dữ liệu được lưu trữ bền vững ra file JSON (`data/bookmarks.json`), không bị mất khi khởi động lại ứng dụng.
6. **Làm mới dữ liệu & Thông báo (Refresh & Observer):** Nút "Làm mới" kéo dữ liệu mới nhất từ mạng; tự động cập nhật số lượng tin mới và ghi log chi tiết.
7. **Chế độ dự phòng (Offline Fallback):** Khi mất mạng hoặc các nguồn RSS bị lỗi, ứng dụng tự động chuyển sang đọc dữ liệu mẫu cục bộ trong `src/main/resources/sample/news-sample.json`, không bao giờ bị sập ứng dụng.

---

## III. YÊU CẦU MÔI TRƯỜNG & CÀI ĐẶT

- **Java Development Kit (JDK):** Java 17 hoặc mới hơn (đã kiểm thử tương thích hoàn hảo trên Java 17, 21, 25).
- **Apache Maven:** Phiên bản 3.8+ (đã tích hợp cấu hình sẵn).

---

## IV. HƯỚNG DẪN KHỞI CHẠY

### 1. Chạy ứng dụng bằng một lệnh duy nhất
Tại thư mục gốc của dự án, mở terminal và gõ:
```bash
mvn spring-boot:run
```
Sau khi màn hình hiện thông báo `Started NewsApplication`, mở trình duyệt web và truy cập:
👉 **[http://localhost:8080](http://localhost:8080)**

*(Lưu ý: Nếu cổng 8080 trên máy của bạn đang bị ứng dụng khác chiếm dụng, có thể chạy với cổng khác bằng lệnh: `mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081` và truy cập [http://localhost:8081](http://localhost:8081))*.

### 2. Chạy kiểm thử tự động (Unit Test & Integration Test)
Dự án có **31 bài kiểm thử tự động** bao phủ toàn bộ 6 mẫu thiết kế và các chức năng:
```bash
mvn clean verify
```
Kết quả kiểm thử:
```text
[INFO] Running com.news.adapter.ArticleAdapterTest -> Tests run: 2, Failures: 0, Errors: 0
[INFO] Running com.news.factory.NewsParserFactoryTest -> Tests run: 3, Failures: 0, Errors: 0
[INFO] Running com.news.strategy.StrategyPatternTest -> Tests run: 7, Failures: 0, Errors: 0
[INFO] Running com.news.observer.ObserverPatternTest -> Tests run: 2, Failures: 0, Errors: 0
[INFO] Running com.news.decorator.DecoratorPatternTest -> Tests run: 5, Failures: 0, Errors: 0
[INFO] Running com.news.cache.CacheManagerTest -> Tests run: 5, Failures: 0, Errors: 0
[INFO] Running com.news.service.BookmarkServiceTest -> Tests run: 1, Failures: 0, Errors: 0
[INFO] Running com.news.NewsApplicationTests -> Tests run: 6, Failures: 0, Errors: 0
[INFO] Tests run: 31, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## V. CẤU TRÚC THƯ MỤC DỰ ÁN

```text
├── pom.xml                                   # File cấu hình Maven (Spring Boot, Rome, Thymeleaf, JUnit 5)
├── README.md                                 # Hướng dẫn cài đặt, chạy và bảng tổng hợp mẫu thiết kế
├── data/
│   └── bookmarks.json                        # Tệp tin JSON lưu trữ các bài viết đã đánh dấu
├── docs/
│   ├── bao-cao.md                            # Dàn ý báo cáo đồ án chi tiết đầy đủ 6 mẫu thiết kế
│   └── uml/                                  # Thư mục chứa sơ đồ PlantUML (.puml) và Mermaid (.md)
│       ├── system-overview.puml
│       ├── factory-method.puml
│       ├── adapter.puml
│       ├── strategy.puml
│       ├── observer.puml
│       ├── decorator.puml
│       ├── singleton.puml
│       └── README.md
└── src/
    ├── main/
    │   ├── java/com/news/
    │   │   ├── NewsApplication.java          # Điểm khởi chạy Spring Boot
    │   │   ├── adapter/                      # [Adapter Pattern]
    │   │   │   ├── ArticleAdapter.java
    │   │   │   ├── RssEntryAdapter.java
    │   │   │   └── JsonArticleAdapter.java
    │   │   ├── cache/                        # [Singleton Pattern]
    │   │   │   ├── CacheManager.java
    │   │   │   └── CacheEntry.java
    │   │   ├── controller/
    │   │   │   └── NewsController.java       # Controller Thymeleaf xử lý request
    │   │   ├── decorator/                    # [Decorator Pattern]
    │   │   │   ├── ArticleDecorator.java
    │   │   │   ├── FeaturedArticle.java
    │   │   │   ├── BookmarkedArticle.java
    │   │   │   └── SummarizedArticle.java
    │   │   ├── factory/                      # [Factory Method Pattern]
    │   │   │   ├── NewsParser.java
    │   │   │   ├── NewsParserFactory.java
    │   │   │   ├── RssNewsParser.java
    │   │   │   ├── JsonNewsParser.java
    │   │   │   ├── RssParserFactory.java
    │   │   │   ├── JsonParserFactory.java
    │   │   │   └── SourceType.java
    │   │   ├── model/
    │   │   │   ├── Article.java              # Lớp thực thể bài viết chuẩn hóa
    │   │   │   └── FeedSource.java           # Định nghĩa nguồn cấp tin RSS
    │   │   ├── observer/                     # [Observer Pattern]
    │   │   │   ├── NewsSubject.java
    │   │   │   ├── NewsFeed.java
    │   │   │   ├── NewsObserver.java
    │   │   │   ├── UnreadCounter.java
    │   │   │   ├── NewsLogger.java
    │   │   │   └── NewsUpdateEvent.java
    │   │   ├── service/
    │   │   │   ├── NewsService.java          # Cầu nối điều phối giữa các mẫu thiết kế
    │   │   │   └── BookmarkService.java      # Quản lý lưu trữ bookmark ra JSON
    │   │   └── strategy/                     # [Strategy Pattern]
    │   │       ├── SortStrategy.java
    │   │       ├── NewestSortStrategy.java
    │   │       ├── OldestSortStrategy.java
    │   │       ├── TitleSortStrategy.java
    │   │       ├── FilterStrategy.java
    │   │       ├── CategoryFilterStrategy.java
    │   │       ├── SourceFilterStrategy.java
    │   │       ├── CompositeFilterStrategy.java
    │   │       ├── AllPassFilterStrategy.java
    │   │       └── ArticleProcessorContext.java
    │   └── resources/
    │       ├── application.properties
    │       ├── sample/
    │       │   └── news-sample.json          # Dữ liệu tin tức mẫu dự phòng khi offline
    │       ├── static/css/
    │       │   └── style.css                 # Giao diện sạch sẽ, responsive, typography chuẩn
    │       └── templates/
    │           ├── index.html                # Trang chủ danh sách tin tức
    │           ├── detail.html               # Trang xem chi tiết bài viết
    │           ├── bookmarks.html            # Trang danh sách bài viết đã lưu
    │           ├── fragments/
    │           │   ├── header.html
    │           │   └── footer.html
    │           └── error/
    │               └── 404.html
    └── test/java/com/news/
        ├── NewsApplicationTests.java
        ├── adapter/ArticleAdapterTest.java
        ├── cache/CacheManagerTest.java
        ├── decorator/DecoratorPatternTest.java
        ├── factory/NewsParserFactoryTest.java
        ├── observer/ObserverPatternTest.java
        ├── service/BookmarkServiceTest.java
        └── strategy/StrategyPatternTest.java
```
