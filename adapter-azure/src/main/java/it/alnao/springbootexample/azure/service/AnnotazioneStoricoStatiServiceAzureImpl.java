package it.alnao.springbootexample.azure.service;

import it.alnao.springbootexample.core.domain.AnnotazioneStoricoStati;
import it.alnao.springbootexample.core.service.AnnotazioneStoricoStatiService;
import it.alnao.springbootexample.azure.entity.AnnotazioneStoricoStatiCosmosEntity;
import it.alnao.springbootexample.azure.repository.AnnotazioneStoricoStatiCosmosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile("azure")
public class AnnotazioneStoricoStatiServiceAzureImpl implements AnnotazioneStoricoStatiService {

    @Autowired
    private AnnotazioneStoricoStatiCosmosRepository storicoRepository;

    @Override
    public AnnotazioneStoricoStati inserisciCambioStato(String idAnnotazione, String versione, String statoNew, String statoOld, String utente, String notaOperazione) {
        AnnotazioneStoricoStatiCosmosEntity entity = new AnnotazioneStoricoStatiCosmosEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setIdAnnotazione(idAnnotazione);
        entity.setVersione(versione);
        entity.setStatoNew(statoNew);
        entity.setStatoOld(statoOld);
        entity.setUtente(utente);
        entity.setNotaOperazione(notaOperazione);
        entity.setDataCambio(java.time.LocalDateTime.now());
        storicoRepository.save(entity);
        AnnotazioneStoricoStati storico = new AnnotazioneStoricoStati();
        storico.setIdAnnotazione(idAnnotazione);
        storico.setVersione(versione);
        storico.setStatoNew(statoNew);
        storico.setStatoOld(statoOld);
        storico.setUtente(utente);
        storico.setNotaOperazione(notaOperazione);
        storico.setDataModifica(entity.getDataCambio());
        return storico;
    }

    @Override
    public List<AnnotazioneStoricoStati> trovaStoricoPerAnnotazione(String idAnnotazione) {
        return storicoRepository.findByIdAnnotazioneOrderByDataCambioDesc(idAnnotazione)
                .stream()
                .map(entity -> {
                    AnnotazioneStoricoStati storico = new AnnotazioneStoricoStati();
                    storico.setIdAnnotazione(entity.getIdAnnotazione());
                    storico.setVersione(entity.getVersione());
                    storico.setStatoNew(entity.getStatoNew());
                    storico.setStatoOld(entity.getStatoOld());
                    storico.setUtente(entity.getUtente());
                    storico.setNotaOperazione(entity.getNotaOperazione());
                    storico.setDataModifica(entity.getDataCambio());
                    return storico;
                })
                .collect(Collectors.toList());
    }
}
