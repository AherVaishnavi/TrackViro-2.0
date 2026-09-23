package com.trackviro.backend.config;

import com.trackviro.backend.security.JwtAuthEntryPoint;
import com.trackviro.backend.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;

import java.util.List;

/**
 * MODIFIED from com.example.demo.config.SecurityConfig — rewritten,
 * not ported, since almost nothing about the old approach carries
 * over to a stateless JWT API. Specifically:
 *
 *  - REMOVED: formLogin(), the successHandler/failureHandler role
 *    redirects, logout() with AntPathRequestMatcher (unsupported in
 *    Security 7 — see below), and sessionManagement().maximumSessions(1)
 *    — none of these make sense without a session.
 *  - CHANGED: sessionManagement() now sets STATELESS instead of
 *    configuring session behaviour.
 *  - CHANGED: authenticationProvider() — the old code called
 *    `new DaoAuthenticationProvider()` (no-arg) then
 *    `.setUserDetailsService(...)`. In Spring Security 7,
 *    DaoAuthenticationProvider has exactly ONE constructor —
 *    DaoAuthenticationProvider(UserDetailsService) — and
 *    setUserDetailsService() no longer exists at all. The old code, as
 *    written, does not compile under Security 7. This class uses the
 *    constructor-based form.
 *  - ADDED: JwtAuthFilter registered before
 *    UsernamePasswordAuthenticationFilter, JwtAuthEntryPoint for 401s,
 *    CORS configuration for the future React dev server, and
 *    /api/auth/** permitAll (the forgot-password endpoints go here in
 *    Step 6 — Step 5's instruction to "keep forgot-password endpoints
 *    public" is satisfied by this rule existing now, ready for them).
 *
 * All URL patterns below end in "/**" with a single wildcard segment,
 * which is required under Security 7's PathPatternParser-based
 * matching (AntPathRequestMatcher/MvcRequestMatcher no longer exist;
 * a pattern like "/api/**\/admin" with a wildcard in the middle, or
 * more than one wildcard segment, is rejected at startup). Every
 * pattern here was written with that constraint in mind, not
 * retrofitted.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthFilter jwtAuthFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          PasswordEncoder passwordEncoder,
                          JwtAuthFilter jwtAuthFilter,
                          JwtAuthEntryPoint jwtAuthEntryPoint) {
        this.userDetailsService = userDetailsService;
        // PasswordEncoder is NOT redefined here — it is the same bean
        // PasswordEncoderConfig already provides (added in Step 4,
        // required by UserServiceImpl/ProfileServiceImpl). Defining a
        // second PasswordEncoder bean here would create an ambiguous
        // bean conflict; this class only reuses the existing one.
        this.passwordEncoder = passwordEncoder;
        this.jwtAuthFilter = jwtAuthFilter;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(
                Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .toList()
        );
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Content-Disposition"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Safe specifically because this API is stateless and JWT
                // travels in an Authorization header, never a cookie — CSRF
                // exists to protect cookie-based sessions from
                // cross-origin form submissions, which doesn't apply here.
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(eh -> eh.authenticationEntryPoint(jwtAuthEntryPoint))

                .authenticationProvider(authenticationProvider())

                .authorizeHttpRequests(auth -> auth
                        // CORS preflight requests carry no Authorization header
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public: login, forgot-password/OTP. No controller
                        // lives here yet (Step 6), but the rule is in place now
                        // so those endpoints are public the moment they exist —
                        // this is the direct fix for the old app's bug where
                        // /profile/forgotPassword/** fell under
                        // anyRequest().authenticated() and never actually
                        // worked while logged out.
                        .requestMatchers("/api/auth/**").permitAll()

                        // Added in Step 7. Without these, Swagger UI and its
                        // JSON spec both fall under anyRequest().authenticated()
                        // below and return 401 instead of rendering — Swagger's
                        // own static assets and generated spec carry no JWT,
                        // so they must be explicitly public, the same reasoning
                        // /api/auth/** already gets.
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        .requestMatchers("/api/employee/**").hasRole("EMPLOYEE")
                        .requestMatchers("/api/manager/**").hasRole("MANAGER")
                        .requestMatchers("/api/finance/**").hasRole("FINANCE")

                        // Any other endpoint (e.g. /api/profile/**, shared by
                        // all three roles) just needs a valid token.
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}