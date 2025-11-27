package it.alnao.springbootexample.javafx.service;

import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.javafx.model.AnnotazioneViewModel;
import org.springframework.stereotype.Service;

/**
 * Service di utilità per conversione tra domain model e view model
 */
@Service
public class ViewModelConverterService {

    public AnnotazioneViewModel toViewModel(AnnotazioneCompleta completa) {
        if (completa == null) {
            return null;
        }
        
        return new AnnotazioneViewModel(
            completa.getAnnotazione().getId(),
            completa.getAnnotazione().getValoreNota(),
            completa.getMetadata().getDescrizione(),
            completa.getMetadata().getUtenteCreazione(),
            completa.getMetadata().getDataInserimento(),
            completa.getMetadata().getStato(),
            completa.getMetadata().getCategoria(),
            completa.getMetadata().getPriorita(),
            completa.getMetadata().getPubblica(),
            completa.getMetadata().getTags()
        );
    }
}
