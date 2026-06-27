package com.gateway.api_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

// Versione ridotta di JwtUtil rispetto ad auth-service.
// Il Gateway NON genera token — si limita a leggere e verificare quelli già esistenti.
// Per questo motivo sono presenti solo extractUsername() e isTokenValid(),
// mentre generateToken() è stato volutamente omesso.
@Component
public class JwtUtil {

    // La chiave segreta per verificare la firma del token.
    // Deve corrispondere esattamente a quella usata da auth-service per firmare —
    // diversamente ogni verifica fallirebbe (firma non riconosciuta → 401).
    private final Key signingKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        // Converte la stringa del segreto in una chiave crittografica HMAC-SHA
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Estrae tutti i claims (payload) dal token.
    // Lancia JwtException se il token è malformato, scaduto o la firma non è valida.
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Estrae lo username (campo "sub" = subject) dal payload del token
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Verifica che il token sia valido: firma corretta E non scaduto.
    // Restituisce false invece di lanciare eccezione, così il filtro può
    // rispondere con 401 in modo pulito senza try/catch nel chiamante.
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            // Il token è valido solo se la data di scadenza è nel futuro
            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            // Token malformato, firma errata, scaduto: in ogni caso non valido
            return false;
        }
    }
}
