package com.trackviro.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Written against jjwt 0.12.x (Jwts.builder().claims()/subject()/
 * signWith(key) and Jwts.parser().verifyWith(key)). This is a
 * different, newer API than jjwt 0.11.x's setClaims/setSubject/
 * parserBuilder() — the two are not interchangeable. jjwt is
 * independent of Spring Security's own version, so this class is
 * unaffected by the Security 6 → 7 changes elsewhere in this step.
 *
 * REQUIRES pom.xml additions and application.properties additions —
 * see the Step 5 summary for both, neither of which exists in the
 * project yet.
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        // secret must be a Base64 string decoding to at least 32 bytes for HS256
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public String generateToken(CustomUserDetails user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid",  user.getId());
        claims.put("role", user.getRole());
        claims.put("name", user.getName());
        if (user.getDepartmentId() != null) {
            claims.put("dept", user.getDepartmentId());
        }

        Date now = new Date();
        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {
        return parse(token).getSubject();
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
