package it.alnao.esempio05.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
//import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import it.alnao.esempio05.model.User;
import it.alnao.esempio05.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoginService loginService;

    @Test
    void testLogin_Success() {
        User user = new User();
        user.setNome("mario");
        user.setPassword("1234");

        Mockito.when(userRepository.findByNome("mario"))
               .thenReturn(Optional.of(user));

        boolean result = loginService.login("mario", "1234");
        Assertions.assertTrue(result);
    }

    @Test
    void testLogin_Fail_WrongPassword() {
        User user = new User();
        user.setNome("mario");
        user.setPassword("1234");

        Mockito.when(userRepository.findByNome("mario"))
               .thenReturn(Optional.of(user));

        boolean result = loginService.login("mario", "wrong");
        Assertions.assertFalse(result);
    }

    @Test
    void testLogin_Fail_UserNotFound() {
        Mockito.when(userRepository.findByNome("giovanni"))
               .thenReturn(Optional.empty());

        boolean result = loginService.login("giovanni", "any");
        Assertions.assertFalse(result);
    }
}
