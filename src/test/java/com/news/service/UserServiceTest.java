package com.news.service;

import com.news.model.User;
import com.news.repository.UserRepository;
import com.news.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Đảm bảo UserService đã nạp sample accounts hoặc kiểm tra trong database
    }

    @Test
    @DisplayName("[User] Xác thực thành công tài khoản mẫu khởi tạo sẵn trong H2 Database")
    void testAuthenticateSampleAccount() {
        Optional<User> adminOpt = userService.authenticate("admin", "123456");
        assertThat(adminOpt).isPresent();
        assertThat(adminOpt.get().getUsername()).isEqualTo("admin");
        assertThat(adminOpt.get().getRole()).isEqualTo("ADMIN");

        // Đăng nhập bằng email
        Optional<User> adminEmailOpt = userService.authenticate("admin@newsreader.vn", "123456");
        assertThat(adminEmailOpt).isPresent();
        assertThat(adminEmailOpt.get().getUsername()).isEqualTo("admin");
    }

    @Test
    @DisplayName("[User] Xác thực thất bại khi sai mật khẩu hoặc tên đăng nhập không tồn tại")
    void testAuthenticateFailure() {
        Optional<User> wrongPass = userService.authenticate("admin", "sai_mat_khau");
        assertThat(wrongPass).isEmpty();

        Optional<User> nonExist = userService.authenticate("khong_ton_tai", "123456");
        assertThat(nonExist).isEmpty();
    }

    @Test
    @DisplayName("[User] Đăng ký tài khoản mới thành công và mật khẩu được băm SHA-256 an toàn")
    void testRegisterSuccess() {
        User registered = userService.register("testuser", "matkhau123", "Nguyễn Văn Test", "test@gmail.com");

        assertThat(registered).isNotNull();
        assertThat(registered.getId()).isNotNull();
        assertThat(registered.getUsername()).isEqualTo("testuser");
        assertThat(registered.getEmail()).isEqualTo("test@gmail.com");
        // Mật khẩu trong DB phải được băm, không lưu plain text
        assertThat(registered.getPassword()).isNotEqualTo("matkhau123");
        assertThat(PasswordUtil.verifyPassword("matkhau123", registered.getPassword())).isTrue();

        // Đăng nhập lại với tài khoản vừa tạo
        Optional<User> loginOpt = userService.authenticate("testuser", "matkhau123");
        assertThat(loginOpt).isPresent();
    }

    @Test
    @DisplayName("[User] Đăng ký báo lỗi khi trùng lặp username hoặc email")
    void testRegisterDuplicateError() {
        // Trùng username admin
        assertThatThrownBy(() -> userService.register("admin", "matkhau123", "Người khác", "khac@gmail.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("đã tồn tại");

        // Trùng email
        assertThatThrownBy(() -> userService.register("docgia2", "matkhau123", "Người khác", "docgia@gmail.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("đã được sử dụng");
    }

    @Test
    @DisplayName("[User] Đăng ký báo lỗi khi mật khẩu quá ngắn hoặc email sai định dạng")
    void testRegisterValidationErrors() {
        // Mật khẩu ngắn < 6 ký tự
        assertThatThrownBy(() -> userService.register("shortuser", "123", "Tên", "valid@gmail.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tối thiểu 6 ký tự");

        // Email sai định dạng
        assertThatThrownBy(() -> userService.register("bademailuser", "matkhau123", "Tên", "email-sai-dinh-dang"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email không đúng định dạng");
    }
}
