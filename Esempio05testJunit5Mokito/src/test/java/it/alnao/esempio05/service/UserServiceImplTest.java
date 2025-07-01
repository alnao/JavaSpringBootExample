package it.alnao.esempio05.service;

import it.alnao.esempio05.model.User;
import it.alnao.esempio05.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllUsers() {
        User user1 = new User(1L, "user1", "pass1");
        User user2 = new User(2L, "user2", "pass2");
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<User> users = userService.getAllUsers();
        assertNotNull(users);
        assertEquals(2, users.size());
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user2));
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetUserByIdFound() {
        User user = new User(1L, "testuser", "password");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> foundUser = userService.getUserById(1L);
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getNome());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testGetUserByIdNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<User> foundUser = userService.getUserById(1L);
        assertFalse(foundUser.isPresent());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateUser() {
        User newUser = new User(null, "newuser", "newpass");
        User savedUser = new User(3L, "newuser", "newpass");
        when(userRepository.save(newUser)).thenReturn(savedUser);

        User result = userService.createUser(newUser);
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("newuser", result.getNome());
        verify(userRepository, times(1)).save(newUser);
    }

    @Test
    void testUpdateUserSuccess() {
        User existingUser = new User(1L, "oldname", "oldpass");
        User updatedDetails = new User(null, "newname", "newpass");
        User savedUser = new User(1L, "newname", "newpass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        Optional<User> result = userService.updateUser(1L, updatedDetails);
        assertTrue(result.isPresent());
        assertEquals("newname", result.get().getNome());
        assertEquals("newpass", result.get().getPassword());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(existingUser); // It saves the *modified* existingUser
    }

    @Test
    void testUpdateUserNotFound() {
        User updatedDetails = new User(null, "newname", "newpass");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<User> result = userService.updateUser(1L, updatedDetails);
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDeleteUserSuccess() {
        User userToDelete = new User(1L, "deleteuser", "deletepass");
        when(userRepository.findById(1L)).thenReturn(Optional.of(userToDelete));
        doNothing().when(userRepository).delete(userToDelete);

        boolean result = userService.deleteUser(1L);
        assertTrue(result);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).delete(userToDelete);
    }

    @Test
    void testDeleteUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = userService.deleteUser(1L);
        assertFalse(result);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).delete(any(User.class));
    }
}