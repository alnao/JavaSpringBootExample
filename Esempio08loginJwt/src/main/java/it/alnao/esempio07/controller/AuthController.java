package it.alnao.esempio07.controller;

import it.alnao.esempio07.dto.LoginRequest;
import it.alnao.esempio07.dto.LoginResponse;
import it.alnao.esempio07.dto.UserDto;
import it.alnao.esempio07.service.AuthService;
import it.alnao.esempio07.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthService authService;
    private final UserService userService;
    
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Richiesta di login per email: {}", loginRequest.getEmail());
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody UserDto userDto) {
        log.info("Richiesta di registrazione per email: {}", userDto.getEmail());
        UserDto registeredUser = authService.register(userDto);
        return ResponseEntity.ok(registeredUser);
    }



}