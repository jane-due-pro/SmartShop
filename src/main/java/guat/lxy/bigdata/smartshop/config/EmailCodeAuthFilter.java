package guat.lxy.bigdata.smartshop.config;

import guat.lxy.bigdata.smartshop.entity.User;
import guat.lxy.bigdata.smartshop.service.EmailService;
import guat.lxy.bigdata.smartshop.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Email code authentication filter.
 * Registered before UsernamePasswordAuthenticationFilter in SecurityConfig.
 *
 * Flow: username = "email__email_code__code" -> verify -> set authentication -> redirect
 */
@Component
public class EmailCodeAuthFilter extends OncePerRequestFilter {

    private static final String EMAIL_CODE_MARKER = "__email_code__";
    private static final SecurityContextRepository CONTEXT_REPO = new HttpSessionSecurityContextRepository();

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if ("/doLogin".equals(request.getRequestURI()) && "POST".equalsIgnoreCase(request.getMethod())) {
            String username = request.getParameter("username");

            if (username != null && username.contains(EMAIL_CODE_MARKER)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                String[] parts = username.split(EMAIL_CODE_MARKER, 2);
                String email = parts[0];
                String code = parts[1];

                if (emailService.verifyCode(email, code)) {
                    User user = userService.findByEmail(email);
                    if (user == null) {
                        user = userService.findByUsername(email);
                    }
                    if (user != null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        CONTEXT_REPO.saveContext(SecurityContextHolder.getContext(), request, response);
                        response.sendRedirect(request.getContextPath() + "/index");
                        return;
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
