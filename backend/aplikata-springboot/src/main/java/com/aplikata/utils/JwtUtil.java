package com.aplikata.utils;
import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.aplikata.security.CustomUserDetails;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    // 注意：这里的 expiration 是**天数**，例如 7 表示 7 天后过期
    @Value("${jwt.expiration-days}")
    private long expirationDays;

    // 生成 Token
    public String generateToken(CustomUserDetails userDetails) {        
        // 将天数转换为毫秒
        long expirationMillis = expirationDays * 24 * 60 * 60 * 1000L;
        return JWT.create()
                .withSubject(userDetails.getUsername())
                .withClaim("userId", userDetails.getId())
                .withClaim("projectId", userDetails.getProjectId())
                .withClaim("domainId", userDetails.getDomainId())
                .withClaim("roles", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationMillis))
                .sign(Algorithm.HMAC256(secret));
    }

    // 从 Token 中提取用户名
    public String extractUsername(String token) {
        DecodedJWT decodedJWT = decodeToken(token);
        return decodedJWT.getSubject();
    }

    // 验证 Token 是否有效
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            DecodedJWT decodedJWT = decodeToken(token);
            String username = decodedJWT.getSubject();
            boolean isTokenExpired = decodedJWT.getExpiresAt().before(new Date());
            return username.equals(userDetails.getUsername()) && !isTokenExpired;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    // 解析并验证 token 签名
    private DecodedJWT decodeToken(String token) throws JWTVerificationException {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(token);
    }
    
    public static void main(String[] args) {
//        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc3MzIxOTg1NywiZXhwIjoxNzczODI0NjU3fQ.FKpSUv5XZ4eOmaM2V_pjcOhfvY7v7bvRwaMcbgeJr-4"; // 从 Postman 复制的 token
//        String secret = "your-super-secret-key-very-long-and-secure"; // 与配置文件相同的密钥
//        Algorithm algorithm = Algorithm.HMAC256(secret);
//        JWTVerifier verifier = JWT.require(algorithm).build();
//        DecodedJWT decodedJWT = verifier.verify(token);
//        System.out.println("Subject: " + decodedJWT.getSubject());
//        System.out.println("Expires: " + decodedJWT.getExpiresAt());
    	
    	System.out.println(new BCryptPasswordEncoder().encode("123456"));
    }
}
