package it.alnao.esempio07.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Il nome è obbligatorio")
    @Size(min = 2, max = 50, message = "Il nome deve essere tra 2 e 50 caratteri")
    @Column(nullable = false, length = 50)
    private String nome;
    
    @NotBlank(message = "Il cognome è obbligatorio")
    @Size(min = 2, max = 50, message = "Il cognome deve essere tra 2 e 50 caratteri")
    @Column(nullable = false, length = 50)
    private String cognome;
    
    @Email(message = "Email non valida")
    @NotBlank(message = "L'email è obbligatoria")
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @NotBlank(message = "La password è obbligatoria")
    @Size(min = 6, message = "La password deve essere di almeno 6 caratteri")
    @Column(nullable = false)
    private String password;
    
    @Column(name = "flag_attivo", nullable = false)
    @Builder.Default
    private Boolean flagAttivo = true;
    
    @Column(name = "data_creazione", nullable = false)
    @Builder.Default
    private LocalDateTime dataCreazione = LocalDateTime.now();
    
    @Column(name = "data_ultimo_accesso")
    private LocalDateTime dataUltimoAccesso;
    
    // Metodi UserDetails per Spring Security
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // Per ora nessun ruolo specifico
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return flagAttivo;
    }
    
    // Metodo di utilità per nome completo
    public String getNomeCompleto() {
        return nome + " " + cognome;
    }
}