package com.project.ragqa.security;

import com.project.ragqa.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Encoders;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static String newSecret() {
        return Encoders.BASE64.encode(Jwts.SIG.HS256.key().build().getEncoded());
    }

    private final User user = new User("abhay@mail.com", "irrelevant-hash");

    @Test
    void validTokenRoundTrips() {
        JwtService service = new JwtService(newSecret(), 60);

        String token = service.generateToken(user);
        Claims claims = service.parse(token);

        assertEquals("abhay@mail.com", claims.getSubject());
        assertEquals("USER", claims.get("role", String.class));
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService service = new JwtService(newSecret(), -1);   // already expired

        String token = service.generateToken(user);

        assertThrows(JwtException.class, () -> service.parse(token));
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        JwtService attacker = new JwtService(newSecret(), 60);
        JwtService server = new JwtService(newSecret(), 60);

        String forged = attacker.generateToken(user);

        assertThrows(JwtException.class, () -> server.parse(forged));
    }
}