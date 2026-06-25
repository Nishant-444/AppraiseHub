package dev.nishants.appraisal.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration}")
  private long expiration;

  //generates a secret key for signing JWT tokens
  private SecretKey getSigningKey() {
    byte[] keyBytes = java.util.HexFormat.of().parseHex(secret);
    return Keys.hmacShaKeyFor(keyBytes);
  }


  // generates a JWT token
  public String generateToken(UserDetails userDetails) {
    return Jwts.builder()
            .subject(userDetails.getUsername())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey())
            .compact();
  }

  //  helper method to extract email from JWT token
  public String extractEmail(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  //  helper method to check if JWT token is valid
  public boolean isTokenValid(String token, UserDetails userDetails) {
    return extractEmail(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
  }

  //  helper method to check if JWT token is expired
  private boolean isTokenExpired(String token) {
    return extractClaim(token, Claims::getExpiration).before(new Date());
  }

  private <T> T extractClaim(String token, Function<Claims, T> resolver) {
    Claims claims = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    return resolver.apply(claims);
  }
}
