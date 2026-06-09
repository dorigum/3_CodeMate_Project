package com.codemate.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String TOKEN_VERSION_CLAIM = "tokenVersion";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    private final SecretKey secretKey;
    private final long accessTokenValidityMilliseconds;
    private final long refreshTokenValidityMilliseconds;

    public JwtTokenProvider(
            @Value("${codemate.jwt.secret}") String secret,
            @Value("${codemate.jwt.access-token-validity-milliseconds}") long accessTokenValidityMilliseconds,
            @Value("${codemate.jwt.refresh-token-validity-milliseconds}") long refreshTokenValidityMilliseconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenValidityMilliseconds = accessTokenValidityMilliseconds;
        this.refreshTokenValidityMilliseconds = refreshTokenValidityMilliseconds;
    }

    public String createAccessToken(Authentication authentication) {
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        return createToken(principal, ACCESS_TOKEN_TYPE, accessTokenValidityMilliseconds);
    }

    public String createRefreshToken(Authentication authentication) {
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        return createToken(principal, REFRESH_TOKEN_TYPE, refreshTokenValidityMilliseconds);
    }

    public Claims parseAccessToken(String token) {
        return parseTokenByType(token, ACCESS_TOKEN_TYPE);
    }

    public Claims parseRefreshToken(String token) {
        return parseTokenByType(token, REFRESH_TOKEN_TYPE);
    }

    public String getEmail(Claims claims) {
        return claims.getSubject();
    }

    public long getTokenVersion(Claims claims) {
        return ((Number) claims.get(TOKEN_VERSION_CLAIM)).longValue();
    }

    public LocalDateTime getExpiresAt(Claims claims) {
        return LocalDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneId.systemDefault());
    }

    public long getAccessTokenExpiresInSeconds() {
        return accessTokenValidityMilliseconds / 1000;
    }

    public long getRefreshTokenExpiresInSeconds() {
        return refreshTokenValidityMilliseconds / 1000;
    }

    private String createToken(
            CustomUserDetails principal,
            String tokenType,
            long validityMilliseconds
    ) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + validityMilliseconds);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(principal.getEmail())
                .claim("userId", principal.getId())
                .claim("nickname", principal.getNickname())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .claim(TOKEN_VERSION_CLAIM, principal.getTokenVersion())
                .issuedAt(now)
                .expiration(expiresAt)
                .signWith(secretKey)
                .compact();
    }

    private Claims parseTokenByType(String token, String expectedType) {
        Claims claims = parseClaims(token);
        if (!expectedType.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new JwtException("Unexpected JWT type");
        }
        return claims;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
