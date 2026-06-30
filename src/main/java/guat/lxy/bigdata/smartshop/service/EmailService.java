package guat.lxy.bigdata.smartshop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // 内存存储验证码: email -> code
    private final ConcurrentHashMap<String, String> codeCache = new ConcurrentHashMap<>();
    // 过期时间: email -> expireTime
    private final ConcurrentHashMap<String, Date> expireCache = new ConcurrentHashMap<>();

    @Async
    public void sendVerificationCode(String toEmail, String type) {
        String code = generateCode();

        // 存储验证码，5分钟有效
        codeCache.put(toEmail, code);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 5);
        expireCache.put(toEmail, cal.getTime());

        String subject = "SmartShop - " + (type != null ? type : "验证码");
        String content = buildEmailContent(code, type);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
            System.out.println(">>> 验证码已发送到: " + toEmail + ", code=" + code);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean verifyCode(String email, String code) {
        String cachedCode = codeCache.get(email);
        Date expireTime = expireCache.get(email);

        if (cachedCode == null || expireTime == null) {
            return false;
        }

        // 检查是否过期
        if (new Date().after(expireTime)) {
            codeCache.remove(email);
            expireCache.remove(email);
            return false;
        }

        // 验证成功，删除验证码
        if (cachedCode.equals(code)) {
            codeCache.remove(email);
            expireCache.remove(email);
            return true;
        }

        return false;
    }

    private String generateCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String buildEmailContent(String code, String type) {
        String typeName = (type != null) ? type : "验证码";
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                    <h1 style="color: white; margin: 0; font-size: 24px;">SmartShop</h1>
                </div>
                <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                    <h2 style="color: #333; margin-top: 0;">您的%s</h2>
                    <p style="color: #666; font-size: 16px;">请使用以下验证码完成操作：</p>
                    <div style="background: white; text-align: center; padding: 20px; border-radius: 8px; margin: 20px 0; border: 2px dashed #667eea;">
                        <span style="font-size: 36px; font-weight: bold; color: #667eea; letter-spacing: 8px;">%s</span>
                    </div>
                    <p style="color: #999; font-size: 14px;">验证码5分钟内有效，请勿泄露给他人。</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="color: #999; font-size: 12px; text-align: center;">此邮件由系统自动发送，请勿直接回复。</p>
                </div>
            </div>
            """.formatted(typeName, code);
    }
}
