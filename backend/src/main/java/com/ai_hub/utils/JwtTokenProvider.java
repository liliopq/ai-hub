package com.ai_hub.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT Token 提供者
 * 负责 JWT token 的生成、验证和解析
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")              //@Value 注解用于从配置文件中获取 jwt.secret 属性的值
    private String jwtSecret;            // JWT 密钥，用于签名和验证 token

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long jwtRefreshExpiration;      

    /**
     * 生成 JWT Token
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param tokenVersion Token 版本号（用于强制失效旧 Token）
     * @return JWT Token
     */
    public String generateToken(Long userId, String username, Integer tokenVersion) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("tokenVersion", tokenVersion)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从 Token 中获取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    /**
     * 从 Token 中获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("userId", Long.class);
    }

    /**
     * 从 Token 中获取 Token 版本号
     *
     * @param token JWT Token
     * @return Token 版本号
     */
    public Integer getTokenVersionFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("tokenVersion", Integer.class);
    }

    /**
     * 从 Token 中获取过期时间
     *
     * @param token JWT Token
     * @return 过期时间
     */
    public Date getExpirationFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getExpiration();
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取签名密钥
     *
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8);  // 将字符串转换成字节数组
        return Keys.hmacShaKeyFor(keyBytes);        // 创建签名密钥
    }

    /**
     * 获取 Refresh Token 过期时间（毫秒）
     *
     * @return Refresh Token 过期时间
     */
    public long getRefreshTokenExpiration() {
        return jwtRefreshExpiration;
    }

    /**
     * 生成刷新令牌 (Refresh Token)
     * 刷新令牌的有效期更长，用于获取新的访问令牌
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param tokenVersion Token 版本号（用于强制失效旧 Token）
     * @return 刷新令牌
     */
    public String generateRefreshToken(Long userId, String username, Integer tokenVersion) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtRefreshExpiration);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("tokenVersion", tokenVersion)
                .claim("tokenType", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 验证刷新令牌是否有效
     *
     * @param refreshToken 刷新令牌
     * @return 是否有效
     */
    public boolean validateRefreshToken(String refreshToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();

            String tokenType = claims.get("tokenType", String.class);
            if (!"refresh".equals(tokenType)) {
                log.error("Token is not a refresh token");
                return false;
            }
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid refresh token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 判断Access Token是否即将过期（提前判断，用于触发刷新）
     *
     * @param token JWT Token
     * @param thresholdMillis 阈值（毫秒），如果token剩余有效期小于此值，认为即将过期
     * @return 是否即将过期
     */
    public boolean isTokenExpiringSoon(String token, long thresholdMillis) {
        try {
            Date expiration = getExpirationFromToken(token);
            long remainingTime = expiration.getTime() - System.currentTimeMillis();
            return remainingTime < thresholdMillis;
        } catch (Exception e) {
            return true;
        }
    }
}
