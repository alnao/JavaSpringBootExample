package it.alnao.springbootexample.postgresql.entity.auth;

import it.alnao.springbootexample.core.domain.auth.RefreshToken;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenEntityTest {

    @Test
    void testDefaultConstructor() {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        
        assertNotNull(entity);
        assertNotNull(entity.getCreatedAt());
    }

    @Test
    void testGettersAndSetters() {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = now.plusDays(30);
        
        entity.setId("token-1");
        entity.setToken("abc123token");
        entity.setUserId("user-1");
        entity.setExpiryDate(expiry);
        entity.setCreatedAt(now);
        entity.setLastUsed(now);
        
        assertEquals("token-1", entity.getId());
        assertEquals("abc123token", entity.getToken());
        assertEquals("user-1", entity.getUserId());
        assertEquals(expiry, entity.getExpiryDate());
        assertEquals(now, entity.getCreatedAt());
        assertEquals(now, entity.getLastUsed());
    }

    @Test
    void testFromDomain() {
        RefreshToken token = new RefreshToken();
        token.setId("token-1");
        token.setToken("xyz789");
        token.setUserId("user-1");
        
        RefreshTokenEntity entity = RefreshTokenEntity.fromDomain(token);
        
        assertEquals("token-1", entity.getId());
        assertEquals("xyz789", entity.getToken());
        assertEquals("user-1", entity.getUserId());
    }

    @Test
    void testToDomain() {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setId("token-1");
        entity.setToken("xyz789");
        entity.setUserId("user-1");
        
        RefreshToken token = entity.toDomain();
        
        assertEquals("token-1", token.getId());
        assertEquals("xyz789", token.getToken());
        assertEquals("user-1", token.getUserId());
    }
}
