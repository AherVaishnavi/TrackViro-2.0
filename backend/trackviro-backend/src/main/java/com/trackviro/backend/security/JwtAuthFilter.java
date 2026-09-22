package com.trackviro.backend.security;

import com.trackviro.backend.config.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Reads "Authorization: Bearer <token>" on every request and
 * populates SecurityContextHolder. This is the direct replacement for
 * the old CustomUserDetailsService's HttpSession write — there is no
 * session created or read anywhere in this project. Registered before
 * UsernamePasswordAuthenticationFilter in SecurityConfig.
 *
 * OncePerRequestFilter is part of spring-web, not spring-security, so
 * it is unaffected by the Security 6 → 7 changes.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String token = header.substring(7);

            if (jwtUtil.isValid(token)) {
                try {
                    String email = jwtUtil.extractEmail(token);
                    CustomUserDetails user =
                            (CustomUserDetails) userDetailsService.loadUserByUsername(email);

                    if (user.isEnabled()) {
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        user, null, user.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource()
                                .buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                    // user.isEnabled() == false means the account was
                    // deactivated after the token was issued — leave
                    // the context empty, the entry point below returns 401.
                } catch (Exception e) {
                    // user deleted / lookup failed after the token was
                    // issued — same outcome, leave the context empty.
                    SecurityContextHolder.clearContext();
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
