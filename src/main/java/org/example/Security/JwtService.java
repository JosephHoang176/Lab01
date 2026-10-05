package org.example.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.DTO.response.TokenResponse;
import org.example.enums.User_Type;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final Duration accessTokenLifetime;
    private final Duration refreshTokenLifetime;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-token-minutes:15}") long accessTokenMinutes,
            @Value("${security.jwt.refresh-token-days:7}") long refreshTokenDays
    ) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT secret must contain at least 32 bytes");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenLifetime = Duration.ofMinutes(accessTokenMinutes);
        this.refreshTokenLifetime = Duration.ofDays(refreshTokenDays);
    }

    public TokenResponse createTokenPair(String email) {
        return createTokenPair(email, User_Type.STAFF);
    }

    public TokenResponse createTokenPair(String email, User_Type role) {
        return new TokenResponse(
                createToken(email, role, "access", accessTokenLifetime),
                createToken(email, role, "refresh", refreshTokenLifetime)
        );
    }

    public String getEmailFromRefreshToken(String token) {
        Claims claims = parse(token);
        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new IllegalArgumentException("Token is not a refresh token");
        }
        return claims.getSubject();
    }

    public User_Type getRole(String token) {
        String role = parse(token).get("role", String.class);
        return User_Type.valueOf(role);
    }

    public String getEmail(String token) {
        return parse(token).getSubject();
    }

    public String getEmailFromAccessToken(String token) {
        Claims claims = parse(token);
        if (!"access".equals(claims.get("type", String.class))) {
            throw new IllegalArgumentException("Token is not an access token");
        }
        return claims.getSubject();
    }

    private String createToken(String email, User_Type role, String type, Duration lifetime) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claim("type", type)
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(lifetime)))
                .signWith(signingKey)
                .compact();
    }

    private Claims parse(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
