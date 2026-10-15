package com.news.adapter;

import com.news.model.Article;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * [Adapter] Concrete Adapter chuyển đổi đối tượng SyndEntry (từ thư viện Rome RSS)
 * thành lớp Article chuẩn của hệ thống.
 *
 * Mẫu thiết kế:
 * - Target: com.news.adapter.ArticleAdapter
 * - Adaptee: com.rometools.rome.feed.synd.SyndEntry
 * - Adapter: com.news.adapter.RssEntryAdapter
 */
public class RssEntryAdapter implements ArticleAdapter<SyndEntry> {

    private static final Pattern IMG_SRC_PATTERN = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE);
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");

    private final String defaultSource;
    private final String defaultCategory;

    public RssEntryAdapter() {
        this("Tin Tức", "Tổng hợp");
    }

    public RssEntryAdapter(String defaultSource, String defaultCategory) {
        this.defaultSource = defaultSource;
        this.defaultCategory = defaultCategory;
    }

    @Override
    public Article adapt(SyndEntry entry) {
        if (entry == null) {
            return null;
        }

        // 1. Trích xuất tiêu đề
        String title = entry.getTitle() != null ? cleanHtml(entry.getTitle().trim()) : "Không có tiêu đề";

        // 2. Trích xuất liên kết
        String url = entry.getLink() != null ? entry.getLink().trim() : "";

        // 3. Tạo ID an toàn từ URL hoặc tiêu đề
        String id = generateArticleId(url, title);

        // 4. Trích xuất mô tả thô và ảnh đại diện
        String rawDescription = entry.getDescription() != null ? entry.getDescription().getValue() : "";
        String imageUrl = extractImageUrl(entry, rawDescription);
        String cleanDescription = cleanHtml(rawDescription);

        // 5. Nội dung: nếu RSS có content thì lấy, không thì dùng description
        String content = cleanDescription;
        if (entry.getContents() != null && !entry.getContents().isEmpty()) {
            content = cleanHtml(entry.getContents().get(0).getValue());
        }

        // 6. Trích xuất ngày xuất bản
        LocalDateTime publishedDate = convertToLocalDateTime(entry.getPublishedDate());

        // 7. Xác định Nguồn báo (Source)
        String source = detectSource(url, defaultSource);

        // 8. Xác định Chuyên mục (Category)
        String category = detectCategory(entry, defaultCategory);

        Article article = new Article(id, title, cleanDescription, content, url, imageUrl, publishedDate, source, category);
        return article;
    }

    private String generateArticleId(String url, String title) {
        String base = (url != null && !url.isEmpty()) ? url : title;
        int hash = Math.abs(base.hashCode());
        return "art-" + Integer.toHexString(hash);
    }

    private String extractImageUrl(SyndEntry entry, String rawDescription) {
        // Ưu tiên 1: Lấy từ thẻ enclosures (Rome)
        List<SyndEnclosure> enclosures = entry.getEnclosures();
        if (enclosures != null) {
            for (SyndEnclosure enclosure : enclosures) {
                if (enclosure.getUrl() != null &&
                        (enclosure.getType() == null || enclosure.getType().startsWith("image") || enclosure.getUrl().matches("(?i).*\\.(jpg|jpeg|png|webp|gif).*"))) {
                    return enclosure.getUrl().trim();
                }
            }
        }

        // Ưu tiên 2: Trích xuất từ thẻ <img src="..."> trong description HTML (phổ biến ở VnExpress, Tuổi Trẻ)
        if (rawDescription != null) {
            Matcher matcher = IMG_SRC_PATTERN.matcher(rawDescription);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        }

        return null;
    }

    private String cleanHtml(String html) {
        if (html == null) return "";
        String text = HTML_TAG_PATTERN.matcher(html).replaceAll("");
        return text.replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&#39;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .trim();
    }

    private LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) {
            return LocalDateTime.now();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private String detectSource(String url, String fallback) {
        if (url != null) {
            String lower = url.toLowerCase();
            if (lower.contains("vnexpress.net")) return "VnExpress";
            if (lower.contains("tuoitre.vn")) return "Tuổi Trẻ";
            if (lower.contains("thanhnien.vn")) return "Thanh Niên";
            if (lower.contains("dantri.com.vn")) return "Dân Trí";
        }
        return fallback;
    }

    private String detectCategory(SyndEntry entry, String fallback) {
        if (entry.getCategories() != null && !entry.getCategories().isEmpty()) {
            String catName = entry.getCategories().get(0).getName();
            if (catName != null && !catName.trim().isEmpty()) {
                return catName.trim();
            }
        }
        return fallback;
    }
}
