package it.alnao.esempio07.dto;

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
public class ContentDto {
    
    private Long id;
    
    @NotBlank(message = "Il titolo è obbligatorio")
    @Size(min = 3, max = 200, message = "Il titolo deve essere tra 3 e 200 caratteri")
    private String titolo;
    
    @NotBlank(message = "La descrizione è obbligatoria")
    @Size(min = 10, max = 1000, message = "La descrizione deve essere tra 10 e 1000 caratteri")
    private String descrizione;
    
    private String contenuto;
    private Boolean pubblicato;
    private LocalDateTime dataCreazione;
    private LocalDateTime dataModifica;
    
    // Informazioni autore
    private Long autoreId;
    private String autoreNome;
    private String autoreCognome;
    private String autoreEmail;
    
    public String getAutoreNomeCompleto() {
        return autoreNome + " " + autoreCognome;
    }
}