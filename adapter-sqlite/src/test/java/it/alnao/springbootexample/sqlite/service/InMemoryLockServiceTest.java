package it.alnao.springbootexample.sqlite.service;

import it.alnao.springbootexample.core.service.AnnotazioneLockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryLockServiceTest {

    private InMemoryLockService service;

    @BeforeEach
    void setup() {
        service = new InMemoryLockService();
    }

    @Test
    void acquireLock_whenFree_returnsTrue() {
        UUID id = UUID.randomUUID();
        assertTrue(service.acquireLock(id, "user", 60));
    }

    @Test
    void acquireLock_whenAlreadyLockedBySameUser_returnsTrue() {
        UUID id = UUID.randomUUID();
        service.acquireLock(id, "user", 60);
        assertTrue(service.acquireLock(id, "user", 60));
    }

    @Test
    void acquireLock_whenLockedByOtherUser_returnsFalse() {
        UUID id = UUID.randomUUID();
        service.acquireLock(id, "user1", 60);
        assertFalse(service.acquireLock(id, "user2", 60));
    }

    @Test
    void releaseLock_byOwner_releasesSuccessfully() {
        UUID id = UUID.randomUUID();
        service.acquireLock(id, "user", 60);
        service.releaseLock(id, "user");
        assertFalse(service.isLocked(id));
    }

    @Test
    void releaseLock_byNonOwner_doesNotRelease() {
        UUID id = UUID.randomUUID();
        service.acquireLock(id, "user1", 60);
        service.releaseLock(id, "user2");
        assertTrue(service.isLocked(id));
    }

    @Test
    void isLocked_whenNotLocked_returnsFalse() {
        UUID id = UUID.randomUUID();
        assertFalse(service.isLocked(id));
    }

    @Test
    void isLocked_whenLocked_returnsTrue() {
        UUID id = UUID.randomUUID();
        service.acquireLock(id, "user", 60);
        assertTrue(service.isLocked(id));
    }

    @Test
    void getOwner_whenLocked_returnsOwner() {
        UUID id = UUID.randomUUID();
        service.acquireLock(id, "testuser", 60);
        Optional<String> owner = service.getOwner(id);
        assertTrue(owner.isPresent());
        assertEquals("testuser", owner.get());
    }

    @Test
    void getOwner_whenNotLocked_returnsEmpty() {
        UUID id = UUID.randomUUID();
        Optional<String> owner = service.getOwner(id);
        assertTrue(owner.isEmpty());
    }

    @Test
    void acquireLock_afterRelease_canBeAcquiredByOtherUser() {
        UUID id = UUID.randomUUID();
        service.acquireLock(id, "user1", 60);
        service.releaseLock(id, "user1");
        assertTrue(service.acquireLock(id, "user2", 60));
    }

    @Test
    void acquireLock_expiredLock_canBeAcquiredByOtherUser() throws InterruptedException {
        UUID id = UUID.randomUUID();
        // acquire with 0 seconds timeout (expires immediately)
        service.acquireLock(id, "user1", 0);
        // cleanExpiredLocks is called on next isLocked/acquireLock
        Thread.sleep(100);
        assertTrue(service.acquireLock(id, "user2", 60));
    }

    @Test
    void releaseLock_whenNotLocked_doesNotThrow() {
        UUID id = UUID.randomUUID();
        assertDoesNotThrow(() -> service.releaseLock(id, "user"));
    }
}
