---
name: editorial-ui-system
description: Quy chuẩn và hướng dẫn thiết kế giao diện báo chí điện tử hiện đại (Editorial Design System) cho ứng dụng đọc báo tin tức.
---

# Editorial UI System - Quy Chuẩn Thiết Kế Báo Chí

Skill này định nghĩa các nguyên tắc, bảng mã màu, phân cấp kiểu chữ và bố cục lưới dành riêng cho các ứng dụng web báo chí, tin tức đa nguồn.

## 1. Hệ thống Phân cấp Chữ (Typography Scale)

Sử dụng Google Font **Plus Jakarta Sans** (hoặc Merriweather / Inter):

- **Kicker Tag (Nhãn chuyên mục):** `font-size: 0.72rem - 0.75rem`, `font-weight: 700`, `text-transform: uppercase`, `letter-spacing: 0.5px`.
- **Hero Headline (Tiêu đề tin chính):** `font-size: 1.5rem - 1.75rem`, `font-weight: 800`, `line-height: 1.3`, `letter-spacing: -0.3px`.
- **Card Headline (Tiêu đề thẻ bài viết):** `font-size: 1.05rem - 1.15rem`, `font-weight: 700`, `line-height: 1.35`.
- **Sapo / Lead Excerpt (Đoạn tóm tắt mở đầu):** `font-size: 0.95rem - 1.05rem`, `font-weight: 500`, `line-height: 1.6`, `color: var(--text-muted)`.
- **Body Text (Nội dung chi tiết bài viết):** `font-size: 1.05rem (17px)`, `line-height: 1.85`, tối đa `65-75 ký tự/dòng` để chống mỏi mắt.
- **Metadata (Tác giả, nguồn, thời gian đọc):** `font-size: 0.76rem - 0.8rem`, `color: var(--text-light)`.

## 2. Bảng Màu Báo Chí Chuẩn (Editorial Light Palette)

```css
:root {
    --primary: #1d4ed8;            /* Xanh dương báo chí chuẩn (Royal Blue) */
    --primary-hover: #1e40af;
    --primary-light: #eff6ff;
    --text-main: #0f172a;          /* Than chì đậm (Slate 900) - Độ tương phản cao */
    --text-muted: #475569;         /* Chữ mô tả (Slate 600) */
    --text-light: #64748b;         /* Thời gian, tác giả (Slate 500) */
    --bg-body: #f8fafc;            /* Nền trang êm mắt */
    --bg-card: #ffffff;            /* Nền thẻ tin tức */
    --border: #e2e8f0;             /* Viền mảnh tinh tế */
}
```

## 3. Quy chuẩn Bố cục Hình ảnh (Aspect Ratio)

- **Hero Spotlight:** Tỷ lệ `16:9` hoặc `16:10` với lớp phủ gradient tinh tế (`linear-gradient(to top, rgba(15,23,42,0.95), transparent)`).
- **Lưới bài viết thông thường (Grid Card):** Tỷ lệ `16:9`, bo góc `6px` (`--radius-sm`).
- **Thẻ tin phụ (Sidebar / Sub-card):** Kích thước cố định `140px x 105px`, `object-fit: cover`.

## 4. Nguyên tắc Tương tác & Micro-interactions

- Luôn có hiệu ứng phóng to nhẹ ảnh khi rê chuột (`transform: scale(1.04); transition: 0.3s ease;`).
- Nút chia sẻ, sao chép liên kết luôn đi kèm phản hồi trực quan (Toast hoặc đổi nhãn tạm thời).
- Tránh sử dụng màu neon hoặc các dải ruy-băng xoay chéo lòe loẹt gây mất tập trung khi đọc tin.
