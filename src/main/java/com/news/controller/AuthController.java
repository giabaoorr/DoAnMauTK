package com.news.controller;

import com.news.model.User;
import com.news.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Controller tiếp nhận và điều hướng các thao tác: Đăng nhập, Đăng ký, Đăng xuất.
 * Quản lý phiên người dùng bằng HttpSession thuần của Spring MVC.
 */
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Hiển thị giao diện Đăng Nhập.
     */
    @GetMapping("/login")
    public String showLoginPage(
            @RequestParam(name = "returnUrl", required = false, defaultValue = "/") String returnUrl,
            HttpSession session,
            Model model) {

        // Nếu đã đăng nhập thì chuyển hướng ngay về trang chủ hoặc trang trước đó
        if (session.getAttribute("currentUser") != null) {
            return "redirect:" + (returnUrl.startsWith("/") ? returnUrl : "/");
        }

        model.addAttribute("returnUrl", returnUrl);
        return "login";
    }

    /**
     * Xử lý xác thực Đăng Nhập.
     */
    @PostMapping("/login")
    public String processLogin(
            @RequestParam("usernameOrEmail") String usernameOrEmail,
            @RequestParam("password") String password,
            @RequestParam(name = "returnUrl", required = false, defaultValue = "/") String returnUrl,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        Optional<User> userOpt = userService.authenticate(usernameOrEmail, password);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Lưu thông tin người dùng vào HTTP Session
            session.setAttribute("currentUser", user);
            redirectAttributes.addFlashAttribute("refreshMessage",
                    "Đăng nhập thành công! Chào mừng bạn, " + user.getFullName() + ".");

            String target = (returnUrl != null && returnUrl.startsWith("/")) ? returnUrl : "/";
            return "redirect:" + target;
        }

        model.addAttribute("errorMessage", "Tên đăng nhập / Email hoặc mật khẩu không chính xác!");
        model.addAttribute("usernameOrEmail", usernameOrEmail);
        model.addAttribute("returnUrl", returnUrl);
        return "login";
    }

    /**
     * Hiển thị giao diện Đăng Ký tài khoản mới.
     */
    @GetMapping("/register")
    public String showRegisterPage(HttpSession session) {
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/";
        }
        return "register";
    }

    /**
     * Xử lý tạo tài khoản người dùng mới.
     */
    @PostMapping("/register")
    public String processRegister(
            @RequestParam("fullName") String fullName,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Mật khẩu xác nhận không trùng khớp!");
            model.addAttribute("fullName", fullName);
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "register";
        }

        try {
            User newUser = userService.register(username, password, fullName, email);
            // Tự động đăng nhập sau khi đăng ký thành công
            session.setAttribute("currentUser", newUser);
            redirectAttributes.addFlashAttribute("refreshMessage",
                    "Đăng ký tài khoản thành công! Chào mừng " + newUser.getFullName() + " gia nhập News Reader.");
            return "redirect:/";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("fullName", fullName);
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "register";
        }
    }

    /**
     * Đăng xuất người dùng và hủy bỏ phiên làm việc HTTP Session.
     */
    @GetMapping("/logout")
    public String processLogout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute("currentUser");
        session.invalidate();
        redirectAttributes.addFlashAttribute("refreshMessage", "Bạn đã đăng xuất tài khoản thành công.");
        return "redirect:/";
    }
}
