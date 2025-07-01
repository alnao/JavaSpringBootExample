package it.alnao.esempio05.controller;

import it.alnao.esempio05.model.User;
import it.alnao.esempio05.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllUsers() {
        User user1 = new User(1L, "user1", "pass1");
        User user2 = new User(2L, "user2", "pass2");
        when(userService.getAllUsers()).thenReturn(Arrays.asList(user1, user2));

        List<User> users = userController.getAllUsers();
        assertNotNull(users);
        assertEquals(2, users.size());
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user2));
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void testGetUserByIdFound() {
        User user = new User(1L, "testuser", "password");
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        ResponseEntity<User> response = userController.getUser(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getNome());
        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void testGetUserByIdNotFound() {
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        ResponseEntity<User> response = userController.getUser(1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void testCreateUser() {
        User newUser = new User(null, "newuser", "newpass");
        User savedUser = new User(3L, "newuser", "newpass");
        when(userService.createUser(newUser)).thenReturn(savedUser);

        User result = userController.createUser(newUser);
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("newuser", result.getNome());
        verify(userService, times(1)).createUser(newUser);
    }

    @Test
    void testUpdateUserSuccess() {
        User updatedDetails = new User(null, "newname", "newpass");
        User savedUser = new User(1L, "newname", "newpass");

        when(userService.updateUser(1L, updatedDetails)).thenReturn(Optional.of(savedUser));

        ResponseEntity<User> response = userController.updateUser(1L, updatedDetails);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("newname", response.getBody().getNome());
        verify(userService, times(1)).updateUser(1L, updatedDetails);
    }

    @Test
    void testUpdateUserNotFound() {
        User updatedDetails = new User(null, "newname", "newpass");
        when(userService.updateUser(1L, updatedDetails)).thenReturn(Optional.empty());

        ResponseEntity<User> response = userController.updateUser(1L, updatedDetails);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService, times(1)).updateUser(1L, updatedDetails);
    }

    @Test
    void testDeleteUserSuccess() {
        when(userService.deleteUser(1L)).thenReturn(true);

        ResponseEntity<Void> response = userController.deleteUser(1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void testDeleteUserNotFound() {
        when(userService.deleteUser(1L)).thenReturn(false);

        ResponseEntity<Void> response = userController.deleteUser(1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService, times(1)).deleteUser(1L);
    }
}