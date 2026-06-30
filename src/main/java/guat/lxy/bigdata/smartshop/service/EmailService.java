package guat.lxy.bigdata.smartshop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 邮件验证码服务
 *
 * 验证码存储从原 ConcurrentHashMap 改为 Redis，TTL 5 分钟：
 * - 重启服务验证码仍然有效
 * - 多实例部署时验证码共享
 * - 自动过期，无需手动清理
 *
 * Redis key: smartshop:verify:code:{email}
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private static final String CODE_KEY_PREFIX = "smartshop:verify:code:";

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${smartshop.cache.ttl.verification-code}")
    private long codeTtlSeconds;

    @Async
    public void sendVerificationCode(String toEmail, String type) {
        String code = generateCode();

        // 存 Redis，5 分钟过期
        redisTemplate.opsForValue().set(CODE_KEY_PREFIX + toEmail, code, codeTtlSeconds, TimeUnit.SECONDS);
        log.info("[EmailService] 验证码已生成并写入 Redis, email={}, ttl={}s", toEmail, codeTtlSeconds);

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
            log.error("[EmailService] 发送邮件失败, email={}", toEmail, e);
        }
    }

    public boolean verifyCode(String email, String code) {
        String key = CODE_KEY_PREFIX + email;
        Object cachedCode = redisTemplate.opsForValue().get(key);
        if (cachedCode == null) {
            log.info("[EmailService] verifyCode 失败：验证码不存在或已过期, email={}", email);
            return false;
        }
        if (cachedCode.toString().equals(code)) {
            redisTemplate.delete(key); // 一次性使用
            log.info("[EmailService] verifyCode 成功, email={}", email);
            return true;
        }
        log.info("[EmailService] verifyCode 失败：验证码不匹配, email={}, expected={}, got={}",
                email, cachedCode, code);
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