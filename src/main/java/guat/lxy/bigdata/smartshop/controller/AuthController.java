package guat.lxy.bigdata.smartshop.controller;

import guat.lxy.bigdata.smartshop.entity.User;
import guat.lxy.bigdata.smartshop.service.EmailService;
import guat.lxy.bigdata.smartshop.service.UserService;
import guat.lxy.bigdata.smartshop.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/login")
    public String loginPage(HttpServletRequest request) {
        // 检查用户是否已登录，如果已登录则自动清除登录状态
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            new SecurityContextLogoutHandler().logout(request, null, auth);
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/resetPassword")
    public String resetPasswordPage() {
        return "resetPassword";
    }

    @PostMapping("/sendCode")
    @ResponseBody
    public Map<String, Object> sendCode(@RequestParam String email, @RequestParam String type) {
        try {
            emailService.sendVerificationCode(email, type);
            return Result.success("验证码已发送到 " + email);
        } catch (Exception e) {
            return Result.fail("发送失败: " + e.getMessage());
        }
    }

    @PostMapping("/doRegister")
    @ResponseBody
    public Map<String, Object> doRegister(@RequestParam String username,
                                          @RequestParam String password,
                                          @RequestParam String email,
                                          @RequestParam String code) {
        if (!emailService.verifyCode(email, code)) {
            return Result.fail("验证码错误或已过期");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);

        return userService.register(user)
                ? Result.success("注册成功，请登录")
                : Result.fail("用户名已存在");
    }

    @PostMapping("/doResetPassword")
    @ResponseBody
    public Map<String, Object> doResetPassword(@RequestParam String email,
                                                @RequestParam String code,
                                                @RequestParam String newPassword) {
        if (!emailService.verifyCode(email, code)) {
            return Result.fail("验证码错误或已过期");
        }

        return userService.resetPassword(email, newPassword)
                ? Result.success("密码重置成功，请重新登录")
                : Result.fail("该邮箱未注册");
    }
}