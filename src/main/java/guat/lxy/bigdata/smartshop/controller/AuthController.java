package guat.lxy.bigdata.smartshop.controller;

import guat.lxy.bigdata.smartshop.entity.User;
import guat.lxy.bigdata.smartshop.service.EmailService;
import guat.lxy.bigdata.smartshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/login")
    public String loginPage() {
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
        Map<String, Object> result = new HashMap<>();
        try {
            emailService.sendVerificationCode(email, type);
            result.put("success", true);
            result.put("message", "验证码已发送到 " + email);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "发送失败: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/doRegister")
    @ResponseBody
    public Map<String, Object> doRegister(@RequestParam String username,
                                          @RequestParam String password,
                                          @RequestParam String email,
                                          @RequestParam String code) {
        Map<String, Object> result = new HashMap<>();

        if (!emailService.verifyCode(email, code)) {
            result.put("success", false);
            result.put("message", "验证码错误或已过期");
            return result;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);

        if (userService.register(user)) {
            result.put("success", true);
            result.put("message", "注册成功，请登录");
        } else {
            result.put("success", false);
            result.put("message", "用户名已存在");
        }
        return result;
    }

    @PostMapping("/doResetPassword")
    @ResponseBody
    public Map<String, Object> doResetPassword(@RequestParam String email,
                                                @RequestParam String code,
                                                @RequestParam String newPassword) {
        Map<String, Object> result = new HashMap<>();

        if (!emailService.verifyCode(email, code)) {
            result.put("success", false);
            result.put("message", "验证码错误或已过期");
            return result;
        }

        if (userService.resetPassword(email, code, newPassword)) {
            result.put("success", true);
            result.put("message", "密码重置成功，请重新登录");
        } else {
            result.put("success", false);
            result.put("message", "该邮箱未注册");
        }
        return result;
    }
}
