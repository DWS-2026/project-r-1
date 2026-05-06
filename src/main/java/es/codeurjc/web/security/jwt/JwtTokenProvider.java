package es.codeurjc.web.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    // Genera una clave secreta segura automáticamente
    private final SecretKey jwtSecret = Jwts.SIG.HS256.key().build();

    public String generateToken(String username, List<String> roles) {
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000)) // 1 día de validez
                .signWith(jwtSecret)
                .compact();
    }

    public Claims validateToken(String token) {
        return Jwts.parser().verifyWith(jwtSecret).build().parseSignedClaims(token).getPayload();
    }
}