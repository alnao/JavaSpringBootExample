package it.alnao.springbootexample.api.mapper;

import it.alnao.springbootexample.api.dto.TransizioneStatoResponse;
import it.alnao.springbootexample.core.domain.TransizioneStato;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper per convertire oggetti TransizioneStato in DTO per le API
 */
public class TransizioneStatoMapper {

    /**
     * Converte un oggetto domain TransizioneStato in DTO
     */
    public static TransizioneStatoResponse toResponse(TransizioneStato transizione) {
        if (transizione == null) {
            return null;
        }

        return new TransizioneStatoResponse(
            transizione.getStatoPartenza().name(),
            transizione.getStatoArrivo().name(),
            transizione.getRuoloRichiesto().name(),
            transizione.getDescrizione()
        );
    }

    /**
     * Converte una lista di oggetti domain TransizioneStato in lista di DTO
     */
    public static List<TransizioneStatoResponse> toResponseList(List<TransizioneStato> transizioni) {
        if (transizioni == null) {
            return null;
        }

        return transizioni.stream()
                .map(TransizioneStatoMapper::toResponse)
                .collect(Collectors.toList());
    }
}
