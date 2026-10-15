---
name: thymeleaf-pattern-components
description: Cẩm nang xây dựng các thành phần giao diện (UI Components & Fragments) chuẩn mực bằng Thymeleaf trong Spring Boot, kết hợp hiển thị các mẫu thiết kế GoF.
---

# Thymeleaf Pattern Components - Hướng Dẫn Xây Dựng Component

Skill này hướng dẫn cách tổ chức và tái sử dụng các mẫu giao diện phía server bằng Spring Boot + Thymeleaf mà không cần sử dụng bất kỳ thư viện hay framework JavaScript phức tạp nào.

## 1. Tổ chức Cấu trúc Thư mục Templates

```
src/main/resources/templates/
├── fragments/
│   ├── header.html        # Thanh điều hướng trên cùng, tìm kiếm nhanh
│   ├── footer.html        # Chân trang, toast popup, script tương tác
│   ├── article-card.html  # Component thẻ bài viết đơn lẻ
│   └── decorator-badge.html # Component hiển thị huy hiệu Decorator
├── index.html             # Trang chủ và danh sách tin tức theo chuyên mục
├── detail.html            # Trang đọc bài viết chi tiết
└── bookmarks.html         # Trang quản lý các bài viết đã đánh dấu
```

## 2. Cách Định Nghĩa và Tái Sử Dụng Fragment

### Cú pháp khai báo Fragment:
```html
<div th:fragment="card(article)" class="article-card">
    <div class="card-image-wrap">
        <img th:src="${article.imageUrl}" th:alt="${article.title}" class="card-image">
    </div>
    <div class="card-body">
        <h3 class="card-title">
            <a th:href="@{'/article/' + ${article.id}}" th:text="${article.title}">Tiêu đề</a>
        </h3>
        <p class="card-excerpt" th:text="${article.description}">Tóm tắt...</p>
    </div>
</div>
```

### Cú pháp nhúng Fragment với tham số:
```html
<div th:replace="~{fragments/header :: header('home')}"></div>
```

## 3. Quy Tắc Xử Lý An Toàn Trực Tiếp Trong Thymeleaf

- **Tránh cú pháp inline JavaScript nguy hiểm:** Không chèn trực tiếp Thymeleaf expression vào `th:onclick` (dễ bị chặn do bảo mật CSP của Spring Boot).
- **Giải pháp chuẩn:** Sử dụng thuộc tính `th:data-*` kết hợp hàm JS thuần:
  ```html
  <button type="button" th:data-url="@{'/article/' + ${article.id}}" 
          onclick="copyArticleLink(this.getAttribute('data-url'), event)">
      Chia sẻ
  </button>
  ```
- **Xử lý ảnh lỗi (Fallback Image):** Luôn gắn `onerror="this.onerror=null; this.src='...';"` để tránh lỗi hiển thị khi link ảnh từ nguồn RSS bên ngoài bị chặn hoặc quá hạn.
