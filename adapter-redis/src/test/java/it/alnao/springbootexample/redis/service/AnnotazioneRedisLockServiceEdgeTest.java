package it.alnao.springbootexample.redis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Copre i rami di pulizia e di forzatura non verificati in
 * {@link AnnotazioneRedisLockServiceTest}.
 */
@SuppressWarnings("unchecked")
class AnnotazioneRedisLockServiceEdgeTest {

    @Mock RedissonClient redisson;
    @Mock RLock rLock;
    @Mock RMap<String, String> ownerMap;
    @InjectMocks AnnotazioneRedisLockService service;

    private UUID annotazioneId;
    private String key;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        annotazioneId = UUID.randomUUID();
        key = annotazioneId.toString();
        lenient().when(redisson.getLock(anyString())).thenReturn(rLock);
        lenient().when(redisson.getMap(anyString())).thenReturn((RMap) ownerMap);
    }

    // ---------- releaseLock ----------

    @Test
    void releaseLock_whenHeldByAnotherUser_doesNothing() {
        when(ownerMap.get(key)).thenReturn("luigi");

        service.releaseLock(annotazioneId, "mario");

        verify(rLock, never()).unlock();
        verify(rLock, never()).forceUnlock();
        verify(ownerMap, never()).remove(anyString());
    }

    @Test
    void releaseLock_whenHeldByAnotherThreadButSameOwner_forcesTheUnlock() {
        when(ownerMap.get(key)).thenReturn("mario");
        when(rLock.isHeldByCurrentThread()).thenReturn(false);
        when(rLock.isLocked()).thenReturn(true);

        service.releaseLock(annotazioneId, "mario");

        verify(rLock).forceUnlock();
        verify(ownerMap).remove(key);
    }

    @Test
    void releaseLock_whenLockedButNoOwnerRegistered_doesNotForceTheUnlock() {
        when(ownerMap.get(key)).thenReturn(null);
        when(rLock.isHeldByCurrentThread()).thenReturn(false);
        when(rLock.isLocked()).thenReturn(true);

        service.releaseLock(annotazioneId, "mario");

        verify(rLock, never()).forceUnlock();
        verify(ownerMap, never()).remove(anyString());
    }

    @Test
    void releaseLock_whenAlreadyReleased_cleansUpTheLeftoverOwner() {
        when(ownerMap.get(key)).thenReturn("mario");
        when(rLock.isHeldByCurrentThread()).thenReturn(false);
        when(rLock.isLocked()).thenReturn(false);

        service.releaseLock(annotazioneId, "mario");

        verify(ownerMap).remove(key);
        verify(rLock, never()).forceUnlock();
    }

    @Test
    void releaseLock_whenAlreadyReleasedAndNoOwner_doesNothing() {
        when(ownerMap.get(key)).thenReturn(null);
        when(rLock.isHeldByCurrentThread()).thenReturn(false);
        when(rLock.isLocked()).thenReturn(false);

        service.releaseLock(annotazioneId, "mario");

        verify(ownerMap, never()).remove(anyString());
    }

    @Test
    void releaseLock_whenUnlockThrowsIllegalMonitorState_cleansUpTheOwner() {
        when(ownerMap.get(key)).thenReturn("mario");
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        doThrow(new IllegalMonitorStateException("gia rilasciato")).when(rLock).unlock();

        service.releaseLock(annotazioneId, "mario");

        verify(ownerMap).remove(key);
    }

    // ---------- isLocked ----------

    @Test
    void isLocked_whenNotLocked_cleansUpTheLeftoverOwner() {
        when(rLock.isLocked()).thenReturn(false);
        when(ownerMap.remove(key)).thenReturn("mario");

        assertFalse(service.isLocked(annotazioneId));

        verify(ownerMap).remove(key);
    }

    @Test
    void isLocked_whenNotLockedAndNoOwner_returnsFalse() {
        when(rLock.isLocked()).thenReturn(false);
        when(ownerMap.remove(key)).thenReturn(null);

        assertFalse(service.isLocked(annotazioneId));
    }

    @Test
    void isLocked_whenLockedWithoutOwner_forcesTheUnlockAndReturnsFalse() {
        when(rLock.isLocked()).thenReturn(true);
        when(ownerMap.get(key)).thenReturn(null);

        assertFalse(service.isLocked(annotazioneId));

        verify(rLock).forceUnlock();
    }

    @Test
    void isLocked_whenForceUnlockThrows_stillReturnsFalse() {
        when(rLock.isLocked()).thenReturn(true);
        when(ownerMap.get(key)).thenReturn(null);
        doThrow(new IllegalMonitorStateException("non posseduto")).when(rLock).forceUnlock();

        assertFalse(service.isLocked(annotazioneId));
    }

    @Test
    void isLocked_whenLockedWithAnOwner_returnsTrue() {
        when(rLock.isLocked()).thenReturn(true);
        when(ownerMap.get(key)).thenReturn("mario");

        assertTrue(service.isLocked(annotazioneId));

        verify(rLock, never()).forceUnlock();
    }

    // ---------- getOwner ----------

    @Test
    void getOwner_whenPresent_returnsIt() {
        when(ownerMap.get(key)).thenReturn("mario");
        assertEquals(Optional.of("mario"), service.getOwner(annotazioneId));
        verify(rLock, never()).forceUnlock();
    }

    @Test
    void getOwner_whenAbsentAndLockStillActive_forcesTheUnlock() {
        when(ownerMap.get(key)).thenReturn(null);
        when(rLock.isLocked()).thenReturn(true);

        assertTrue(service.getOwner(annotazioneId).isEmpty());

        verify(rLock).forceUnlock();
    }

    @Test
    void getOwner_whenAbsentAndForceUnlockThrows_stillReturnsEmpty() {
        when(ownerMap.get(key)).thenReturn(null);
        when(rLock.isLocked()).thenReturn(true);
        doThrow(new IllegalMonitorStateException("non posseduto")).when(rLock).forceUnlock();

        assertTrue(service.getOwner(annotazioneId).isEmpty());
    }

    @Test
    void getOwner_whenAbsentAndLockNotActive_returnsEmpty() {
        when(ownerMap.get(key)).thenReturn(null);
        when(rLock.isLocked()).thenReturn(false);

        assertTrue(service.getOwner(annotazioneId).isEmpty());

        verify(rLock, never()).forceUnlock();
    }
}
