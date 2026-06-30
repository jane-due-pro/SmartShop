package guat.lxy.bigdata.smartshop.config;

import guat.lxy.bigdata.smartshop.service.impl.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("noop", new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword.toString().equals(encodedPassword);
            }
        });
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        return new DelegatingPasswordEncoder("noop", encoders);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // 静态资源 + 登录/注册/验证码相关全部匿名放行
                .requestMatchers(
                    "/login", "/register", "/resetPassword",
                    "/doLogin", "/logout",
                    "/sendCode", "/doRegister", "/doResetPassword",
                    "/css/**", "/js/**", "/img/**", "/favicon.ico"
                ).permitAll()
                // 业务页面：登录后即可访问（iframe 内的 /welcome、/product/**、/category/** 都走这里）
                .requestMatchers("/welcome", "/index", "/", "/product/**", "/category/**").authenticated()
                // 管理后台保留 admin 角色
                .requestMatchers("/admin/**").hasRole("admin")
                // 其他全部需要登录
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/doLogin")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/index", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // iframe 内嵌场景下允许同源加载，避免被 frame-ancestors 拒掉
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            )
            .csrf(csrf -> csrf.disable())
            .userDetailsService(userDetailsService);
        return http.build();
    }
}
