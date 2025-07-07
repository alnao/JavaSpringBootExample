package it.alnao.esempio07.service;

import it.alnao.esempio07.dto.LoginRequest;
import it.alnao.esempio07.dto.LoginResponse;
import it.alnao.esempio07.dto.UserDto;
import it.alnao.esempio07.entity.User;
import it.alnao.esempio07.repository.UserRepository;
import it.alnao.esempio07.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserRepository userRepository;
    
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Tentativo di login per utente: {}", loginRequest.getEmail());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );
            
            User user = (User) authentication.getPrincipal();
            
            if (!user.getFlagAttivo()) {
                throw new CustomException("Account disattivato");
            }
            
            String token = jwtService.generateToken(user);
            
            // Aggiorna l'ultimo accesso
            userService.updateLastAccess(user.getEmail());
            
            UserDto userDto = UserDto.builder()
                    .id(user.getId())
                    .nome(user.getNome())
                    .cognome(user.getCognome())
                    .email(user.getEmail())
                    .flagAttivo(user.getFlagAttivo())
                    .dataCreazione(user.getDataCreazione())
                    .dataUltimoAccesso(user.getDataUltimoAccesso())
                    .build();
            
            log.info("Login effettuato con successo per utente: {}", loginRequest.getEmail());
            
            return new LoginResponse(token, userDto);
            
        } catch (AuthenticationException e) {
            log.error("Errore durante il login per utente: {}", loginRequest.getEmail(), e);
            throw new CustomException("Credenziali non valide");
        }
    }
    
    @Transactional
    public UserDto register(UserDto userDto) {
        log.info("Registrazione nuovo utente: {}", userDto.getEmail());
        
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new CustomException("Email già registrata: " + userDto.getEmail());
        }
        
        return userService.createUser(userDto);
    }
}