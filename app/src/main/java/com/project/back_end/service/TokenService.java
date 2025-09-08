package com.project.back_end.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class TokenService {
    
    @Value("${jwt.secret:mySecretKey123456789012345678901234567890}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
    private Long jwtExpiration;
    
    /**
     * Generate JWT token using user's email and role
     */
    public String generateToken(String email, String role, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", userId);
        claims.put("email", email);
        
        return createToken(claims, email);
    }
    
    /**
     * Generate JWT token with user's email only
     */
    public String generateToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        
        return createToken(claims, email);
    }
    
    /**
     * Create JWT token with claims and subject
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Get the signing key for JWT
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Extract email from token
     */
    public String getEmailFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Extract role from token
     */
    public String getRoleFromToken(String token) {
        return extractClaim(token, claims -> (String) claims.get("role"));
    }
    
    /**
     * Extract user ID from token
     */
    public Long getUserIdFromToken(String token) {
        return extractClaim(token, claims -> {
            Object userId = claims.get("userId");
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            } else if (userId instanceof Long) {
                return (Long) userId;
            }
            return null;
        });
    }
    
    /**
     * Extract expiration date from token
     */
    public Date getExpirationDateFromToken(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * Extract issued date from token
     */
    public Date getIssuedDateFromToken(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }
    
    /**
     * Generic method to extract claims from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * Check if user is admin from token
     */
    public boolean isAdmin(String token) {
        try {
            String role = getRoleFromToken(token);
            return "ADMIN".equals(role);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if user is doctor from token
     */
    public boolean isDoctor(String token) {
        try {
            String role = getRoleFromToken(token);
            return "DOCTOR".equals(role);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if user is patient from token
     */
    public boolean isPatient(String token) {
        try {
            String role = getRoleFromToken(token);
            return "PATIENT".equals(role);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Refresh token if it's about to expire
     */
    public String refreshToken(String token) {
        try {
            if (validateToken(token) && !isTokenExpired(token)) {
                String email = getEmailFromToken(token);
                String role = getRoleFromToken(token);
                Long userId = getUserIdFromToken(token);
                
                return generateToken(email, role, userId);
            }
        } catch (Exception e) {
            // Token is invalid, cannot refresh
        }
        return null;
    }
    
    /**
     * Get token remaining validity time in milliseconds
     */
    public long getTokenRemainingTime(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.getTime() - new Date().getTime();
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Extract specific claim by name
     */
    public Object getClaimFromToken(String token, String claimName) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.get(claimName);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Create token for password reset (shorter expiration)
     */
    public String generatePasswordResetToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("type", "password_reset");
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 3600000); // 1 hour
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Validate password reset token
     */
    public boolean validatePasswordResetToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String tokenType = (String) claims.get("type");
            return "password_reset".equals(tokenType) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
