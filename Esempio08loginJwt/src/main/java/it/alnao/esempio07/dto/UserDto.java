package it.alnao.esempio07.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    
    private Long id;
    
    @NotBlank(message = "Il nome è obbligatorio")
    @Size(min = 2, max = 50, message = "Il nome deve essere tra 2 e 50 caratteri")
    private String nome;
    
    @NotBlank(message = "Il cognome è obbligatorio")
    @Size(min = 2, max = 50, message = "Il cognome deve essere tra 2 e 50 caratteri")
    private String cognome;
    
    @Email(message = "Email non valida")
    @NotBlank(message = "L'email è obbligatoria")
    private String email;
    
    private Boolean flagAttivo;
    private LocalDateTime dataCreazione;
    private LocalDateTime dataUltimoAccesso;
    
    // Per la registrazione
    @Size(min = 6, message = "La password deve essere di almeno 6 caratteri")
    private String password;
    
    // Campo calcolato
    private String nomeCompleto;    
    public String getNomeCompleto() {
        return nome + " " + cognome;
    }
}