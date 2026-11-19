package com.news.repository;

import com.news.model.UserBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository quản lý bài viết đã lưu theo từng tài khoản người dùng trong H2.
 */
@Repository
public interface UserBookmarkRepository extends JpaRepository<UserBookmark, Long> {

    List<UserBookmark> findByUserIdOrderBySavedAtDesc(Long userId);

    Optional<UserBookmark> findByUserIdAndArticleId(Long userId, String articleId);

    boolean existsByUserIdAndArticleId(Long userId, String articleId);

    void deleteByUserIdAndArticleId(Long userId, String articleId);

    long countByUserId(Long userId);
}
