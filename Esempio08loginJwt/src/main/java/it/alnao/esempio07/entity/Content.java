package it.alnao.esempio07.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "contenuti")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Content {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Il titolo è obbligatorio")
    @Size(min = 3, max = 200, message = "Il titolo deve essere tra 3 e 200 caratteri")
    @Column(nullable = false, length = 200)
    private String titolo;
    
    @NotBlank(message = "La descrizione è obbligatoria")
    @Size(min = 10, max = 1000, message = "La descrizione deve essere tra 10 e 1000 caratteri")
    @Column(nullable = false, length = 1000)
    private String descrizione;
    
    @Column(columnDefinition = "TEXT")
    private String contenuto;
    
    @Column(name = "data_creazione", nullable = false)
    @Builder.Default
    private LocalDateTime dataCreazione = LocalDateTime.now();
    
    @Column(name = "data_modifica")
    private LocalDateTime dataModifica;
    
    @Column(name = "pubblicato", nullable = false)
    @Builder.Default
    private Boolean pubblicato = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User autore;
    
    @PreUpdate
    public void preUpdate() {
        dataModifica = LocalDateTime.now();
    }
}