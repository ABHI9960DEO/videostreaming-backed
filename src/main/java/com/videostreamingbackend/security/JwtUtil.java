package com.videostreamingbackend.security;

import com.videostreamingbackend.config.AppProperties;
import com.videostreamingbackend.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final AppProperties appProperties;

    //--Key
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(appProperties.getJwt().getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //--Generate Access Token
    public String generateAccessToken(User user) {
        long now = System.currentTimeMillis();
        return Jwts.builder().
                subject(user.getEmail()).
                claim("userId", user.getId()).
                claim("role", user.getRole().name()).
                issuedAt(new Date(now)).
                expiration(new Date(now + appProperties.getJwt().getAccessTokenExpiresMs())).
                signWith(getSigningKey()).
                compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    public boolean isTokenValid(String token) {
        try{
            extractAllClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT malformed: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("JWT security invaild: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT empty/null: {}", e.getMessage());
        }
        return false;
    }
}
