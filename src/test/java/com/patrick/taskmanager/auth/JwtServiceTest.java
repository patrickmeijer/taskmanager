package com.patrick.taskmanager.auth;

import com.patrick.taskmanager.user.UserRole;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String testSecret = "mytestsecretkeymustbelongenoughforhmacsha256";

    @BeforeEach
    void setUp() {
        long expirationMs = 120000;
        jwtService = new JwtService(testSecret, expirationMs);
    }

    @Test
    void whenMalformedToken_thenThrowJwtException() {
        assertThrows(JwtException.class, () -> jwtService.extractUsername("this.is.a.malformed.token"));
    }

    @Test
    void whenExpiredToken_thenValidateTokenReturnsFalse() {
        String expiredToken = Jwts.builder()
                .subject("testuser")
                .expiration(new Date(System.currentTimeMillis() - 10000))
                .signWith(Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();

        boolean isValid = jwtService.validateToken(expiredToken, "testuser");

        assertFalse(isValid, "Expired token should be invalid");
    }

    @Test
    void whenInvalidSignature_thenThrowJwtException() {
        String invalidToken = Jwts.builder()
                .subject("testuser")
                .signWith(Keys.hmacShaKeyFor("thisisaninvalidsecretthatwillthereforenotvalidate".getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();

        assertThrows(JwtException.class, () -> jwtService.extractUsername(invalidToken));
    }

    @Test
    void whenTokenHasNullRole_thenReturnNull() {
        String nullRoleToken = Jwts.builder()
                .subject("testuser")
                .claim("role", null)
                .signWith(Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();

        String extractedRole = jwtService.extractRole(nullRoleToken);
        assertNull(extractedRole, "Extracted role should be null if not present in claims");
    }

    @Test
    void whenTokenIsValid_thenExtractCorrectly() {
        String token = jwtService.generateToken("testuser", UserRole.ROLE_USER);

        assertEquals("testuser", jwtService.extractUsername(token));
        assertEquals("ROLE_USER", jwtService.extractRole(token));
        assertTrue(jwtService.validateToken(token, "testuser"));
    }
}