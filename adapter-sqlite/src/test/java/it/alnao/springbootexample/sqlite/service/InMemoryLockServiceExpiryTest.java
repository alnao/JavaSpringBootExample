package it.alnao.springbootexample.sqlite.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Copre la pulizia dei lock scaduti o inconsistenti, non raggiunta dai test
 * sul percorso normale in {@link InMemoryLockServiceTest}.
 */
class InMemoryLockServiceExpiryTest {

    private InMemoryLockService service;
    private UUID annotazioneId;

    @SuppressWarnings("unchecked")
    private Map<UUID, String> locks() {
        return (Map<UUID, String>) ReflectionTestUtils.getField(service, "locks");
    }

    @SuppressWarnings("unchecked")
    private Map<UUID, Long> timestamps() {
        return (Map<UUID, Long>) ReflectionTestUtils.getField(service, "lockTimestamps");
    }

    @SuppressWarnings("unchecked")
    private Map<UUID, Long> timeouts() {
        return (Map<UUID, Long>) ReflectionTestUtils.getField(service, "lockTimeouts");
    }

    @BeforeEach
    void setup() {
        service = new InMemoryLockService();
        annotazioneId = UUID.randomUUID();
    }

    @Test
    void unLockWithoutTimestamp_isRemovedAsInconsistent() {
        locks().put(annotazioneId, "mario");
        // nessun timestamp associato: il lock e' inconsistente

        assertFalse(service.isLocked(annotazioneId));
        assertTrue(locks().isEmpty());
    }

    @Test
    void getOwner_alsoDropsALockWithoutTimestamp() {
        locks().put(annotazioneId, "mario");
        assertTrue(service.getOwner(annotazioneId).isEmpty());
    }

    @Test
    void anExpiredLockIsRemoved() {
        locks().put(annotazioneId, "mario");
        timestamps().put(annotazioneId, System.currentTimeMillis() - 60_000);
        timeouts().put(annotazioneId, 1_000L);

        assertFalse(service.isLocked(annotazioneId));
        assertTrue(locks().isEmpty());
    }

    @Test
    void aStillValidLockIsKept() {
        locks().put(annotazioneId, "mario");
        timestamps().put(annotazioneId, System.currentTimeMillis());
        timeouts().put(annotazioneId, 60_000L);

        assertTrue(service.isLocked(annotazioneId));
        assertEquals("mario", service.getOwner(annotazioneId).orElse(null));
    }

    @Test
    void withoutAConfiguredTimeoutTheDefaultOfFiveMinutesApplies() {
        locks().put(annotazioneId, "mario");
        // timestamp recente, nessun timeout: vale il default di 5 minuti
        timestamps().put(annotazioneId, System.currentTimeMillis());

        assertTrue(service.isLocked(annotazioneId));
    }

    @Test
    void withoutAConfiguredTimeoutAnOldLockStillExpires() {
        locks().put(annotazioneId, "mario");
        timestamps().put(annotazioneId, System.currentTimeMillis() - (10 * 60 * 1000));

        assertFalse(service.isLocked(annotazioneId));
    }
}
