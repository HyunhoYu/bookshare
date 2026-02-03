package my.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import my.domain.user.UserVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.expiration}")
    private Long exp;

    private SecretKey key;


    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(UserVO userVO) {
        Long userId = userVO.getId();
        String userRole = userVO.getRole().name();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + exp);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", userRole)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public Long getUserId(String token) {
        Claims claims = parseClaims(token);

        return Long.parseLong(claims.getSubject());
    }

    public String getRole(String token) {

        Claims claims = parseClaims(token);

        return claims.get("role", String.class);
    }


    public boolean validateToken(String token) {

        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }

    }


    private Claims parseClaims(String token) {

        JwtParser jwtParse = Jwts.parser()
                .verifyWith(key)
                .build();

        Jws<Claims> claims = jwtParse.parseSignedClaims(token);

        return claims.getPayload();
    }


}
