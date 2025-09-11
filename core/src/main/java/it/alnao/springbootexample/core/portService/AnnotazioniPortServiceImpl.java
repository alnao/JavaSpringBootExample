package it.alnao.springbootexample.core.portService;

import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.service.AnnotazioneService;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AnnotazioniPortServiceImpl implements AnnotazioniPortService {
    @Autowired
    private AnnotazioneService annotazioneService;

        public AnnotazioneCompleta creaAnnotazione(AnnotazioneCompleta annotazione, String utente) {
            AnnotazioneCompleta annotazioneCompleta = annotazioneService.creaAnnotazione(
                    annotazione.getAnnotazione().getValoreNota(),
                    annotazione.getMetadata().getDescrizione(),
                    utente
            );
            if (annotazione.getMetadata().getCategoria() != null) {
                annotazioneService.impostaCategoria(annotazioneCompleta.getId(), annotazione.getMetadata().getCategoria(), utente);
            }
            if (annotazione.getMetadata().getTags() != null) {
                annotazioneService.impostaTags(annotazioneCompleta.getId(), annotazione.getMetadata().getTags(), utente);
            }
            if (annotazione.getMetadata().getPubblica() != null) {
                annotazioneService.impostaVisibilitaPubblica(annotazioneCompleta.getId(), annotazione.getMetadata().getPubblica(), utente);
            }
            if (annotazione.getMetadata().getPriorita() != null) {
                annotazioneService.impostaPriorita(annotazioneCompleta.getId(), annotazione.getMetadata().getPriorita(), utente);
            }
            return annotazioneService.trovaPerID(annotazioneCompleta.getId()).orElse(null);
        }

    public AnnotazioneCompleta aggiornaAnnotazione(AnnotazioneCompleta annotazione, String utente) {
        UUID id = annotazione.getId();
        AnnotazioneCompleta annotazioneAggiornata = annotazioneService.aggiornaAnnotazione(
                id,
                annotazione.getAnnotazione().getValoreNota(),
                annotazione.getMetadata().getDescrizione(),
                utente
        );
        if (annotazione.getMetadata().getCategoria() != null) {
            annotazioneService.impostaCategoria(id, annotazione.getMetadata().getCategoria(), utente);
        }
        if (annotazione.getMetadata().getTags() != null) {
            annotazioneService.impostaTags(id, annotazione.getMetadata().getTags(), utente);
        }
        if (annotazione.getMetadata().getPubblica() != null) {
            annotazioneService.impostaVisibilitaPubblica(id, annotazione.getMetadata().getPubblica(), utente);
        }
        if (annotazione.getMetadata().getPriorita() != null) {
            annotazioneService.impostaPriorita(id, annotazione.getMetadata().getPriorita(), utente);
        }
        return annotazioneService.trovaPerID(id).orElse(null);
    }

    public List<AnnotazioneCompleta> trovaTutte() {
        return annotazioneService.trovaTutte();
    }

    public Optional<AnnotazioneCompleta> trovaPerID(UUID id) {
        return annotazioneService.trovaPerID(id);
    }

    public void eliminaAnnotazione(UUID id) {
        annotazioneService.eliminaAnnotazione(id);
    }

    public List<AnnotazioneCompleta> cercaPerTesto(String testo) {
        return annotazioneService.cercaPerTesto(testo);
    }

    public List<AnnotazioneCompleta> trovaPerUtente(String utente) {
        return annotazioneService.trovaPerUtente(utente);
    }

    public List<AnnotazioneCompleta> trovaPerCategoria(String categoria) {
        return annotazioneService.trovaPerCategoria(categoria);
    }

    public List<AnnotazioneCompleta> trovaPubbliche() {
        return annotazioneService.trovaPubbliche();
    }

    public long contaAnnotazioni() {
        return annotazioneService.contaAnnotazioni();
    }
}
