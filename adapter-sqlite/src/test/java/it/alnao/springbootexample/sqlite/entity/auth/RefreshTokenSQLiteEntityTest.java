package it.alnao.springbootexample.sqlite.entity.auth;

import it.alnao.springbootexample.core.domain.auth.RefreshToken;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenSQLiteEntityTest {

    private RefreshToken domain() {
        RefreshToken t = new RefreshToken();
        t.setId("id-1");
        t.setToken("token-value");
        t.setUserId("user-1");
        t.setExpiryDate(LocalDateTime.now().plusDays(7));
        t.setCreatedAt(LocalDateTime.now().minusDays(1));
        t.setLastUsed(LocalDateTime.now());
        return t;
    }

    @Test
    void constructorFromDomain_copiesEveryField() {
        RefreshToken source = domain();
        RefreshTokenSQLiteEntity entity = new RefreshTokenSQLiteEntity(source);
        assertEquals(source.getId(), entity.getId());
        assertEquals(source.getToken(), entity.getToken());
        assertEquals(source.getUserId(), entity.getUserId());
        assertEquals(source.getExpiryDate(), entity.getExpiryDate());
        assertEquals(source.getCreatedAt(), entity.getCreatedAt());
        assertEquals(source.getLastUsed(), entity.getLastUsed());
    }

    @Test
    void toDomain_roundTripsEveryField() {
        RefreshToken source = domain();
        RefreshToken result = new RefreshTokenSQLiteEntity(source).toDomain();
        assertEquals(source.getId(), result.getId());
        assertEquals(source.getToken(), result.getToken());
        assertEquals(source.getUserId(), result.getUserId());
        assertEquals(source.getExpiryDate(), result.getExpiryDate());
        assertEquals(source.getCreatedAt(), result.getCreatedAt());
        assertEquals(source.getLastUsed(), result.getLastUsed());
    }

    @Test
    void defaultConstructor_leavesFieldsNull() {
        RefreshTokenSQLiteEntity entity = new RefreshTokenSQLiteEntity();
        assertNull(entity.getId());
        assertNull(entity.getToken());
        assertNull(entity.getUserId());
        assertNull(entity.getExpiryDate());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getLastUsed());
    }

    @Test
    void settersAndGetters_work() {
        RefreshTokenSQLiteEntity entity = new RefreshTokenSQLiteEntity();
        LocalDateTime expiry = LocalDateTime.now().plusHours(2);
        LocalDateTime created = LocalDateTime.now().minusHours(2);
        LocalDateTime used = LocalDateTime.now();
        entity.setId("x");
        entity.setToken("t");
        entity.setUserId("u");
        entity.setExpiryDate(expiry);
        entity.setCreatedAt(created);
        entity.setLastUsed(used);
        assertEquals("x", entity.getId());
        assertEquals("t", entity.getToken());
        assertEquals("u", entity.getUserId());
        assertEquals(expiry, entity.getExpiryDate());
        assertEquals(created, entity.getCreatedAt());
        assertEquals(used, entity.getLastUsed());
    }
}
