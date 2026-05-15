package es.codeurjc.web.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    // FIX JWT: The key is no longer generated randomly on every startup.
    // Now it is read from application.properties, so tokens remain valid after restarts.
    @Value("${jwt.secret}")
    private String jwtSecretString;

    private SecretKey jwtSecret;

    @PostConstruct
    public void init() {
        this.jwtSecret = Keys.hmacShaKeyFor(jwtSecretString.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username, List<String> roles) {
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day of validity
                .signWith(jwtSecret)
                .compact();
    }

    public Claims validateToken(String token) {
        return Jwts.parser().verifyWith(jwtSecret).build().parseSignedClaims(token).getPayload();
    }
}