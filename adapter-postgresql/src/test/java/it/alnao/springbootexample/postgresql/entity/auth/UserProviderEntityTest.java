package it.alnao.springbootexample.postgresql.entity.auth;

import it.alnao.springbootexample.core.domain.auth.UserProvider;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class UserProviderEntityTest {

    @Test
    void testDefaultConstructor() {
        UserProviderEntity entity = new UserProviderEntity();
        
        assertNotNull(entity);
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getLastUsed());
    }

    @Test
    void testGettersAndSetters() {
        UserProviderEntity entity = new UserProviderEntity();
        LocalDateTime now = LocalDateTime.now();
        
        entity.setId("prov-1");
        entity.setUserId("user-1");
        entity.setProvider("github");
        entity.setProviderUserId("gh-123");
        entity.setProviderEmail("test@github.com");
        entity.setProviderUsername("testuser");
        entity.setAccessTokenHash("hash123");
        entity.setCreatedAt(now);
        entity.setLastUsed(now);
        
        assertEquals("prov-1", entity.getId());
        assertEquals("user-1", entity.getUserId());
        assertEquals("github", entity.getProvider());
        assertEquals("gh-123", entity.getProviderUserId());
        assertEquals("test@github.com", entity.getProviderEmail());
        assertEquals("testuser", entity.getProviderUsername());
        assertEquals("hash123", entity.getAccessTokenHash());
        assertEquals(now, entity.getCreatedAt());
        assertEquals(now, entity.getLastUsed());
    }

    @Test
    void testFromDomain() {
        UserProvider provider = new UserProvider();
        provider.setId("prov-1");
        provider.setUserId("user-1");
        provider.setProvider("google");
        
        UserProviderEntity entity = UserProviderEntity.fromDomain(provider);
        
        assertEquals("prov-1", entity.getId());
        assertEquals("user-1", entity.getUserId());
        assertEquals("google", entity.getProvider());
    }

    @Test
    void testToDomain() {
        UserProviderEntity entity = new UserProviderEntity();
        entity.setId("prov-1");
        entity.setUserId("user-1");
        entity.setProvider("google");
        
        UserProvider provider = entity.toDomain();
        
        assertEquals("prov-1", provider.getId());
        assertEquals("user-1", provider.getUserId());
        assertEquals("google", provider.getProvider());
    }
}
