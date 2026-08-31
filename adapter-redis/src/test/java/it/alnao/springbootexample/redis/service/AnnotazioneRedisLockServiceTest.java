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
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class AnnotazioneRedisLockServiceTest {

    @Mock RedissonClient redisson;
    @Mock RLock rLock;
    @Mock RMap<String, String> ownerMap;
    @InjectMocks AnnotazioneRedisLockService service;

    private UUID annotazioneId;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        annotazioneId = UUID.randomUUID();
        when(redisson.getLock(anyString())).thenReturn(rLock);
        when(redisson.getMap(anyString())).thenReturn((RMap) ownerMap);
    }

    // --- acquireLock ---

    @Test
    void acquireLock_whenSuccessful_returnsTrue() throws InterruptedException {
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        boolean result = service.acquireLock(annotazioneId, "user", 30);

        assertTrue(result);
        verify(ownerMap).put(eq(annotazioneId.toString()), eq("user"));
    }

    @Test
    void acquireLock_whenFails_returnsFalse() throws InterruptedException {
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        boolean result = service.acquireLock(annotazioneId, "user", 30);

        assertFalse(result);
        verify(ownerMap, never()).put(any(), any());
    }

    @Test
    void acquireLock_whenInterrupted_returnsFalse() throws InterruptedException {
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenThrow(new InterruptedException());

        boolean result = service.acquireLock(annotazioneId, "user", 30);

        assertFalse(result);
        assertTrue(Thread.currentThread().isInterrupted());
        Thread.interrupted(); // clear flag for subsequent tests
    }

    @Test
    void acquireLock_whenOwnerMapFails_releasesLockAndReturnsFalse() throws InterruptedException {
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        doThrow(new RuntimeException("redis error")).when(ownerMap).put(any(), any());

        boolean result = service.acquireLock(annotazioneId, "user", 30);

        assertFalse(result);
        verify(rLock).unlock();
    }

    // --- releaseLock ---

    @Test
    void releaseLock_whenHeldByCurrentThread_unlocksAndRemovesOwner() {
        when(ownerMap.get(annotazioneId.toString())).thenReturn("user");
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        service.releaseLock(annotazioneId, "user");

        verify(rLock).unlock();
        verify(ownerMap).remove(annotazioneId.toString());
    }

    @Test
    void releaseLock_whenNotOwner_doesNotUnlock() {
        when(ownerMap.get(annotazioneId.toString())).thenReturn("otherUser");

        service.releaseLock(annotazioneId, "user");

        verify(rLock, never()).unlock();
        verify(rLock, never()).forceUnlock();
    }

    @Test
    void releaseLock_whenLockedByOtherThread_andOwnerMatches_forceUnlocks() {
        when(ownerMap.get(annotazioneId.toString())).thenReturn("user");
        when(rLock.isHeldByCurrentThread()).thenReturn(false);
        when(rLock.isLocked()).thenReturn(true);

        service.releaseLock(annotazioneId, "user");

        verify(rLock).forceUnlock();
        verify(ownerMap).remove(annotazioneId.toString());
    }

    @Test
    void releaseLock_whenAlreadyReleased_cleansOwner() {
        when(ownerMap.get(annotazioneId.toString())).thenReturn("user");
        when(rLock.isHeldByCurrentThread()).thenReturn(false);
        when(rLock.isLocked()).thenReturn(false);

        service.releaseLock(annotazioneId, "user");

        verify(ownerMap).remove(annotazioneId.toString());
    }

    // --- isLocked ---

    @Test
    void isLocked_whenLockedWithOwner_returnsTrue() {
        when(rLock.isLocked()).thenReturn(true);
        when(ownerMap.get(annotazioneId.toString())).thenReturn("user");

        boolean result = service.isLocked(annotazioneId);

        assertTrue(result);
    }

    @Test
    void isLocked_whenNotLocked_returnsFalse() {
        when(rLock.isLocked()).thenReturn(false);

        boolean result = service.isLocked(annotazioneId);

        assertFalse(result);
    }

    @Test
    void isLocked_whenLockedButNoOwner_forceUnlocksAndReturnsFalse() {
        when(rLock.isLocked()).thenReturn(true);
        when(ownerMap.get(annotazioneId.toString())).thenReturn(null);

        boolean result = service.isLocked(annotazioneId);

        assertFalse(result);
        verify(rLock).forceUnlock();
    }

    // --- getOwner ---

    @Test
    void getOwner_whenOwnerExists_returnsOwner() {
        when(ownerMap.get(annotazioneId.toString())).thenReturn("user");
        when(rLock.isLocked()).thenReturn(true);

        Optional<String> result = service.getOwner(annotazioneId);

        assertTrue(result.isPresent());
        assertEquals("user", result.get());
    }

    @Test
    void getOwner_whenNoOwnerAndNotLocked_returnsEmpty() {
        when(ownerMap.get(annotazioneId.toString())).thenReturn(null);
        when(rLock.isLocked()).thenReturn(false);

        Optional<String> result = service.getOwner(annotazioneId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getOwner_whenNoOwnerButLocked_forceUnlocksAndReturnsEmpty() {
        when(ownerMap.get(annotazioneId.toString())).thenReturn(null);
        when(rLock.isLocked()).thenReturn(true);

        Optional<String> result = service.getOwner(annotazioneId);

        assertTrue(result.isEmpty());
        verify(rLock).forceUnlock();
    }
}
