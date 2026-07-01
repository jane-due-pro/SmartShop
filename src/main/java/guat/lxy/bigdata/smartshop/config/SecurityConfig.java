package guat.lxy.bigdata.smartshop.config;

import guat.lxy.bigdata.smartshop.service.impl.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private EmailCodeAuthFilter emailCodeAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .addFilterBefore(emailCodeAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/login", "/register", "/resetPassword",
                    "/doLogin", "/logout",
                    "/sendCode", "/doRegister", "/doResetPassword",
                    "/css/**", "/js/**", "/img/**", "/favicon.ico"
                ).permitAll()
                .requestMatchers("/welcome", "/index", "/", "/product/list", "/category/list").authenticated()
                .requestMatchers("/product/add", "/product/edit/**", "/product/delete/**").hasRole("admin")
                .requestMatchers("/category/add", "/category/edit/**", "/category/delete/**").hasRole("admin")
                .requestMatchers("/admin/**").hasRole("admin")
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
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .csrf(csrf -> csrf.disable())
            .userDetailsService(userDetailsService);
        return http.build();
    }
}