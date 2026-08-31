package it.alnao.springbootexample.aws.entity.auth;

import it.alnao.springbootexample.core.domain.auth.RefreshToken;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenMySQLEntityTest {

    private RefreshToken domain() {
        RefreshToken t = new RefreshToken();
        t.setId("id-1");
        t.setToken("token-value");
        t.setUserId("user-1");
        t.setExpiryDate(LocalDateTime.of(2026, 12, 31, 23, 59));
        t.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        t.setLastUsed(LocalDateTime.of(2026, 6, 1, 12, 0));
        return t;
    }

    @Test
    void fromDomain_copiesEveryField() {
        RefreshTokenMySQLEntity entity = RefreshTokenMySQLEntity.fromDomain(domain());
        assertEquals("id-1", entity.getId());
        assertEquals("token-value", entity.getToken());
        assertEquals("user-1", entity.getUserId());
        assertEquals(LocalDateTime.of(2026, 12, 31, 23, 59), entity.getExpiryDate());
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), entity.getCreatedAt());
        assertEquals(LocalDateTime.of(2026, 6, 1, 12, 0), entity.getLastUsed());
    }

    @Test
    void toDomain_roundTripsEveryField() {
        RefreshToken source = domain();
        RefreshToken result = RefreshTokenMySQLEntity.fromDomain(source).toDomain();
        assertEquals(source.getId(), result.getId());
        assertEquals(source.getToken(), result.getToken());
        assertEquals(source.getUserId(), result.getUserId());
        assertEquals(source.getExpiryDate(), result.getExpiryDate());
        assertEquals(source.getCreatedAt(), result.getCreatedAt());
        assertEquals(source.getLastUsed(), result.getLastUsed());
    }

    @Test
    void settersAndGettersWork() {
        RefreshTokenMySQLEntity entity = new RefreshTokenMySQLEntity();
        LocalDateTime expiry = LocalDateTime.now().plusDays(1);
        LocalDateTime created = LocalDateTime.now().minusDays(1);
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
