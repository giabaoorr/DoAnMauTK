package com.news.service;

import com.news.model.User;
import com.news.repository.UserRepository;
import com.news.util.PasswordUtil;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service xử lý logic nghiệp vụ tài khoản: Đăng ký, Đăng nhập, Xác thực.
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Khởi tạo sẵn một số tài khoản mẫu trong H2 Database để phục vụ kiểm thử và thuyết trình đồ án.
     */
    @PostConstruct
    public void initSampleAccounts() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User(
                    "admin",
                    PasswordUtil.hashPassword("123456"),
                    "Quản Trị Viên",
                    "admin@newsreader.vn",
                    "ADMIN"
            );
            userRepository.save(admin);
            logger.info("Đã khởi tạo tài khoản mẫu [admin / 123456] trong H2 Database.");
        }

        if (!userRepository.existsByUsername("docgia")) {
            User reader = new User(
                    "docgia",
                    PasswordUtil.hashPassword("123456"),
                    "Độc Giả Thân Thiết",
                    "docgia@gmail.com",
                    "USER"
            );
            userRepository.save(reader);
            logger.info("Đã khởi tạo tài khoản mẫu [docgia / 123456] trong H2 Database.");
        }
    }

    /**
     * Đăng ký tài khoản người dùng mới.
     *
     * @param username Tên đăng nhập
     * @param rawPassword Mật khẩu chưa băm
     * @param fullName Họ và tên
     * @param email Địa chỉ email
     * @return Đối tượng User vừa tạo
     */
    @Transactional
    public User register(String username, String rawPassword, String fullName, String email) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống!");
        }
        String cleanUsername = username.trim().toLowerCase();
        if (cleanUsername.length() < 3) {
            throw new IllegalArgumentException("Tên đăng nhập phải có ít nhất 3 ký tự!");
        }
        if (cleanUsername.contains(" ")) {
            throw new IllegalArgumentException("Tên đăng nhập không được chứa khoảng trắng!");
        }

        if (rawPassword == null || rawPassword.trim().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có độ dài tối thiểu 6 ký tự!");
        }

        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ và tên không được để trống!");
        }

        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Địa chỉ email không đúng định dạng!");
        }
        String cleanEmail = email.trim().toLowerCase();

        if (userRepository.existsByUsername(cleanUsername)) {
            throw new IllegalArgumentException("Tên đăng nhập '" + cleanUsername + "' đã tồn tại trên hệ thống!");
        }

        if (userRepository.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("Email '" + cleanEmail + "' đã được sử dụng!");
        }

        String hashedPassword = PasswordUtil.hashPassword(rawPassword);
        User user = new User(cleanUsername, hashedPassword, fullName.trim(), cleanEmail, "USER");
        User saved = userRepository.save(user);

        logger.info("Đăng ký thành công tài khoản mới: id={}, username='{}', email='{}'",
                saved.getId(), saved.getUsername(), saved.getEmail());
        return saved;
    }

    /**
     * Xác thực thông tin đăng nhập theo tên đăng nhập hoặc email.
     *
     * @param usernameOrEmail Tên đăng nhập hoặc địa chỉ email
     * @param rawPassword Mật khẩu người dùng nhập
     * @return Optional chứa User nếu đăng nhập thành công, hoặc rỗng nếu sai thông tin
     */
    public Optional<User> authenticate(String usernameOrEmail, String rawPassword) {
        if (usernameOrEmail == null || rawPassword == null) {
            return Optional.empty();
        }

        String query = usernameOrEmail.trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByUsername(query);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(query);
        }

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (PasswordUtil.verifyPassword(rawPassword, user.getPassword())) {
                logger.info("Xác thực đăng nhập thành công cho người dùng: '{}'", user.getUsername());
                return Optional.of(user);
            }
        }

        logger.warn("Xác thực thất bại cho thông tin: '{}'", usernameOrEmail);
        return Optional.empty();
    }

    public Optional<User> findById(Long id) {
        if (id == null) return Optional.empty();
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return userRepository.findByUsername(username.trim().toLowerCase());
    }
}
