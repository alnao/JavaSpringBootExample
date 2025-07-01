package it.alnao.esempio05.repository;

import it.alnao.esempio05.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest //indispensabile per usare il application-test.properties
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveUser() {
        // Arrange
        User user = new User();
        user.setNome("mario");
        user.setPassword("1234");

        // Act
        User savedUser = userRepository.save(user);

        // Assert
        assertNotNull(savedUser.getId());
        assertEquals("mario", savedUser.getNome());
        assertEquals("1234", savedUser.getPassword());
    }

    @Test
    void testFindByNome_UserExists() {
        // Arrange
        User user = new User();
        user.setNome("giulia");
        user.setPassword("abcd");
        userRepository.save(user);

        // Act
        Optional<User> result = userRepository.findByNome("giulia");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("abcd", result.get().getPassword());
    }

    @Test
    void testFindByNome_UserNotFound() {
        // Act
        Optional<User> result = userRepository.findByNome("inesistente");

        // Assert
        assertFalse(result.isPresent());
    }
}
