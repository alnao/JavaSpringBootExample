package it.alnao.esempio07.service;

import it.alnao.esempio07.dto.UserDto;
import it.alnao.esempio07.entity.User;
import it.alnao.esempio07.repository.UserRepository;
import it.alnao.esempio07.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public UserDto createUser(UserDto userDto) {
        log.info("Creazione nuovo utente: {}", userDto.getEmail());
        
        // Verifica se l'email è già in uso
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new CustomException("Email già in uso: " + userDto.getEmail());
        }
        
        User user = User.builder()
                .nome(userDto.getNome())
                .cognome(userDto.getCognome())
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .flagAttivo(true)
                .dataCreazione(LocalDateTime.now())
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("Utente creato con successo: {}", savedUser.getId());
        
        return convertToDto(savedUser);
    }
    
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        log.info("Recupero tutti gli utenti");
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<UserDto> getActiveUsers() {
        log.info("Recupero utenti attivi");
        return userRepository.findByFlagAttivoTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        log.info("Recupero utente con ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException("Utente non trovato con ID: " + id));
        return convertToDto(user);
    }
    
    @Transactional(readOnly = true)
    public UserDto getUserByEmail(String email) {
        log.info("Recupero utente con email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Utente non trovato con email: " + email));
        return convertToDto(user);
    }
    
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        log.info("Aggiornamento utente con ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException("Utente non trovato con ID: " + id));
        
        // Verifica se l'email è già in uso da un altro utente
        if (!user.getEmail().equals(userDto.getEmail()) && 
            userRepository.existsByEmail(userDto.getEmail())) {
            throw new CustomException("Email già in uso: " + userDto.getEmail());
        }
        
        user.setNome(userDto.getNome());
        user.setCognome(userDto.getCognome());
        user.setEmail(userDto.getEmail());
        user.setFlagAttivo(userDto.getFlagAttivo());
        
        // Aggiorna password solo se fornita
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        
        User updatedUser = userRepository.save(user);
        log.info("Utente aggiornato con successo: {}", updatedUser.getId());
        
        return convertToDto(updatedUser);
    }
    
    @Transactional
    public void deleteUser(Long id) {
        log.info("Eliminazione utente con ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException("Utente non trovato con ID: " + id));
        
        // Soft delete: disattiva l'utente invece di eliminarlo
        user.setFlagAttivo(false);
        userRepository.save(user);
        
        log.info("Utente disattivato con successo: {}", id);
    }
    
    @Transactional
    public void updateLastAccess(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Utente non trovato con email: " + email));
        
        user.setDataUltimoAccesso(LocalDateTime.now());
        userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    public List<UserDto> searchUsers(String searchTerm) {
        log.info("Ricerca utenti con termine: {}", searchTerm);
        return userRepository.searchActiveUsers(searchTerm).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private UserDto convertToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .nome(user.getNome())
                .cognome(user.getCognome())
                .email(user.getEmail())
                .flagAttivo(user.getFlagAttivo())
                .dataCreazione(user.getDataCreazione())
                .dataUltimoAccesso(user.getDataUltimoAccesso())
                .nomeCompleto(user.getNomeCompleto())
                .build();
    }
}