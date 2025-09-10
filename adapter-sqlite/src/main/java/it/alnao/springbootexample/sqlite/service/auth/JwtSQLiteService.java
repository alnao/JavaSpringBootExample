package it.alnao.springbootexample.sqlite.service.auth;

import it.alnao.springbootexample.port.domain.auth.RefreshToken;
import it.alnao.springbootexample.port.domain.auth.User;
import it.alnao.springbootexample.port.service.auth.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Service
@Profile("sqlite")
public class JwtSQLiteService implements JwtService {
	private static final Logger logger = LoggerFactory.getLogger(JwtSQLiteService.class);

	@Value("${gestione-personale.jwt.secret:defaultSecretKeyForSQLite}")
	private String secret;

	@Value("${gestione-personale.jwt.expiration:3600}")
	private long expirationSeconds;

	private SecretKey getSecretKey() {
		return Keys.hmacShaKeyFor(secret.getBytes());
	}

    @Override
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("id", user.getId());
        claims.put("roles", user.getRole());
        return createToken(claims, user.getUsername());

    }

	private String createToken(Map<String, Object> claims, String subject) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + expirationSeconds * 1000);
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(subject)
				.setIssuedAt(now)
				.setExpiration(expiryDate)
				.signWith(getSecretKey())
				.compact();
	}

    @Override
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
    private <T> T getClaimFromToken(String token, ClaimsResolver<T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.resolve(claims);
    }
    
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public boolean validateToken(String token, User user) {
        final String username = getUsernameFromToken(token);
        return (username.equals(user.getUsername()) && !isTokenExpired(token));
    }

	@Override
	public boolean isTokenExpired(String token) {
		return getExpirationDateFromToken(token).before(new Date());
	}
    private Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

	@Override
	public void cleanExpiredTokens() {
		// No-op for stateless JWTs in Replit profile
	}

	// Add missing interface methods
	@Override
	public RefreshToken generateRefreshToken(User user) {
		// TODO: Implement refresh token generation for Replit
		throw new UnsupportedOperationException("Refresh tokens not implemented for Replit profile");
	}
	@Override
	public String getUserIdFromToken(String token) {
		return getClaimFromToken(token, claims -> claims.get("id", String.class));
	}

	@Override
	public Optional<String> refreshToken(String refreshToken) {
		// TODO: Implement token refresh for Replit
		return Optional.empty();
	}

	@Override
	public void invalidateRefreshToken(String refreshToken) {
		// No-op for stateless JWTs in Replit profile
	}

	@Override
	public void invalidateAllUserTokens(String userId) {
		// No-op for stateless JWTs in Replit profile
	}

    @FunctionalInterface
    private interface ClaimsResolver<T> {
        T resolve(Claims claims);
    }
    private SecretKey getSignKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
