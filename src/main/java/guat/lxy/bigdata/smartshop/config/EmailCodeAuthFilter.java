package guat.lxy.bigdata.smartshop.config;

import guat.lxy.bigdata.smartshop.entity.User;
import guat.lxy.bigdata.smartshop.service.EmailService;
import guat.lxy.bigdata.smartshop.service.UserService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(-1)
public class EmailCodeAuthFilter extends OncePerRequestFilter {

    private static final String EMAIL_CODE_MARKER = "__email_code__";

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
            String password = request.getParameter("password");

            if (username != null && username.contains(EMAIL_CODE_MARKER) && SecurityContextHolder.getContext().getAuthentication() == null) {
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
                                new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
