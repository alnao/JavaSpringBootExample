package it.alnao.springbootexample.core.portService;

import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnnotazioniPortService {

    public AnnotazioneCompleta creaAnnotazione(AnnotazioneCompleta annotazione, String utente);

    public AnnotazioneCompleta aggiornaAnnotazione(AnnotazioneCompleta annotazione, String utente);

    public List<AnnotazioneCompleta> trovaTutte();

    public Optional<AnnotazioneCompleta> trovaPerID(UUID id);

    public void eliminaAnnotazione(UUID id);

    public List<AnnotazioneCompleta> cercaPerTesto(String testo);

    public List<AnnotazioneCompleta> trovaPerUtente(String utente);

    public List<AnnotazioneCompleta> trovaPerCategoria(String categoria);

    public List<AnnotazioneCompleta> trovaPubbliche();

    public long contaAnnotazioni();
    
}
