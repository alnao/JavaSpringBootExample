package it.alnao.springbootexample.onprem.service;

import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.repository.AnnotazioneRepository;
import it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository;
import it.alnao.springbootexample.core.service.AnnotazioneService;
import it.alnao.springbootexample.core.utils.AnnotazioniUtils;
import it.alnao.springbootexample.onprem.entity.AnnotazioneStoricoEntity;
import it.alnao.springbootexample.onprem.repository.AnnotazioneStoricoMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile("onprem")
@Transactional
public class AnnotazioneServiceImpl implements AnnotazioneService {

    @Autowired
    private AnnotazioneRepository annotazioneRepository;

    @Autowired
    private AnnotazioneMetadataRepository metadataRepository;

    @Autowired
    private AnnotazioneStoricoMongoRepository storicoMongoRepository;

    @Override
    public AnnotazioneCompleta creaAnnotazione(String valoreNota, String descrizione, String utente) {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // Crea annotazione NoSQL
        Annotazione annotazione = new Annotazione();
        annotazione.setId(id);
        annotazione.setVersioneNota("v1.0");
        annotazione.setValoreNota(valoreNota);
        Annotazione savedAnnotazione = annotazioneRepository.save(annotazione);

        // Crea metadata SQL
        AnnotazioneMetadata metadata = new AnnotazioneMetadata();
        metadata.setId(id);
        metadata.setVersioneNota("v1.0");
        metadata.setUtenteCreazione(utente);
        metadata.setDataInserimento(now);
        metadata.setDataUltimaModifica(now);
        metadata.setUtenteUltimaModifica(utente);
        metadata.setDescrizione(descrizione);
        metadata.setCategoria("Default");
        metadata.setTags("");
        metadata.setPubblica(false);
        metadata.setPriorita(1);
        AnnotazioneMetadata savedMetadata = metadataRepository.save(metadata);

        return new AnnotazioneCompleta(savedAnnotazione, savedMetadata);
    }

    @Override
    public AnnotazioneCompleta aggiornaAnnotazione(UUID id, String nuovoValore, String nuovaDescrizione, String utente) {
        Optional<Annotazione> existingAnnotazione = annotazioneRepository.findById(id);
        Optional<AnnotazioneMetadata> existingMetadata = metadataRepository.findById(id);

        if (existingAnnotazione.isPresent() && existingMetadata.isPresent()) {
            Annotazione annotazione = existingAnnotazione.get();
            AnnotazioneMetadata metadata = existingMetadata.get();

            // 1. Salva la versione precedente nello storico
            AnnotazioneStoricoEntity storico = new AnnotazioneStoricoEntity();
            storico.setIdOriginale(annotazione.getId().toString());
            storico.setVersioneNota(annotazione.getVersioneNota());
            storico.setValoreNota(annotazione.getValoreNota());
            storico.setDescrizione(metadata.getDescrizione());
            storico.setUtente(metadata.getUtenteUltimaModifica());
            storico.setCategoria(metadata.getCategoria());
            storico.setTags(metadata.getTags());
            storico.setPubblica(metadata.getPubblica());
            storico.setPriorita(metadata.getPriorita());
            storico.setDataModifica(metadata.getDataUltimaModifica());
            storicoMongoRepository.save(storico);

            // 2. Aggiorna annotazione
            annotazione.setValoreNota(nuovoValore);
            annotazione.setVersioneNota(AnnotazioniUtils.incrementaVersione(annotazione.getVersioneNota()));
            Annotazione updatedAnnotazione = annotazioneRepository.save(annotazione);

            // 3. Aggiorna metadata
            metadata.setDataUltimaModifica(LocalDateTime.now());
            metadata.setUtenteUltimaModifica(utente);
            metadata.setDescrizione(nuovaDescrizione);
            metadata.setVersioneNota(annotazione.getVersioneNota());
            AnnotazioneMetadata updatedMetadata = metadataRepository.save(metadata);

            return new AnnotazioneCompleta(updatedAnnotazione, updatedMetadata);
        }

        throw new RuntimeException("Annotazione non trovata con ID: " + id);
    }

    @Override
    public Optional<AnnotazioneCompleta> trovaPerID(UUID id) {
        Optional<Annotazione> annotazione = annotazioneRepository.findById(id);
        Optional<AnnotazioneMetadata> metadata = metadataRepository.findById(id);

        if (annotazione.isPresent() && metadata.isPresent()) {
            return Optional.of(new AnnotazioneCompleta(annotazione.get(), metadata.get()));
        }
        return Optional.empty();
    }

    @Override
    public List<AnnotazioneCompleta> trovaTutte() {
        List<Annotazione> annotazioni = annotazioneRepository.findAll();
        List<AnnotazioneMetadata> metadatas = metadataRepository.findAll();

        Map<UUID, AnnotazioneMetadata> metadataMap = metadatas.stream()
                .collect(Collectors.toMap(AnnotazioneMetadata::getId, m -> m));

        return annotazioni.stream()
                .filter(a -> metadataMap.containsKey(a.getId()))
                .map(a -> new AnnotazioneCompleta(a, metadataMap.get(a.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneCompleta> trovaPerUtente(String utente) {
        List<AnnotazioneMetadata> metadatas = metadataRepository.findByUtenteCreazione(utente);
        return buildAnnotazioniComplete(metadatas);
    }

    @Override
    public List<AnnotazioneCompleta> trovaPerCategoria(String categoria) {
        List<AnnotazioneMetadata> metadatas = metadataRepository.findByCategoria(categoria);
        return buildAnnotazioniComplete(metadatas);
    }

    @Override
    public List<AnnotazioneCompleta> trovaPerPeriodo(LocalDateTime inizio, LocalDateTime fine) {
        List<AnnotazioneMetadata> metadatas = metadataRepository.findByDataInserimentoBetween(inizio, fine);
        return buildAnnotazioniComplete(metadatas);
    }

    @Override
    public List<AnnotazioneCompleta> cercaPerTesto(String testo) {
        List<Annotazione> annotazioni = annotazioneRepository.findByValoreNotaContaining(testo);
        List<AnnotazioneMetadata> metadatas = metadataRepository.findByDescrizioneContaining(testo);

        // Combina i risultati
        List<UUID> idsAnnotazioni = annotazioni.stream().map(Annotazione::getId).collect(Collectors.toList());
        List<UUID> idsMetadatas = metadatas.stream().map(AnnotazioneMetadata::getId).collect(Collectors.toList());
        
        // Unisce gli ID unici
        idsAnnotazioni.addAll(idsMetadatas);
        List<UUID> idsUnici = idsAnnotazioni.stream().distinct().collect(Collectors.toList());

        return idsUnici.stream()
                .map(id -> trovaPerID(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneCompleta> trovaPubbliche() {
        List<AnnotazioneMetadata> metadatas = metadataRepository.findByPubblica(true);
        return buildAnnotazioniComplete(metadatas);
    }

    @Override
    public void eliminaAnnotazione(UUID id) {
        if (annotazioneRepository.existsById(id) && metadataRepository.existsById(id)) {
            annotazioneRepository.deleteById(id);
            metadataRepository.deleteById(id);
        }
    }

    @Override
    public boolean esisteAnnotazione(UUID id) {
        return annotazioneRepository.existsById(id) && metadataRepository.existsById(id);
    }

    @Override
    public long contaAnnotazioni() {
        return annotazioneRepository.count();
    }

    @Override
    public long contaAnnotazioniPerUtente(String utente) {
        return metadataRepository.countByUtenteCreazione(utente);
    }

    @Override
    public void impostaVisibilitaPubblica(UUID id, boolean pubblica, String utente) {
        Optional<AnnotazioneMetadata> metadataOpt = metadataRepository.findById(id);
        if (metadataOpt.isPresent()) {
            AnnotazioneMetadata metadata = metadataOpt.get();
            metadata.setPubblica(pubblica);
            metadata.setUtenteUltimaModifica(utente);
            metadata.setDataUltimaModifica(LocalDateTime.now());
            metadataRepository.save(metadata);
        }
    }

    @Override
    public void impostaCategoria(UUID id, String categoria, String utente) {
        Optional<AnnotazioneMetadata> metadataOpt = metadataRepository.findById(id);
        if (metadataOpt.isPresent()) {
            AnnotazioneMetadata metadata = metadataOpt.get();
            metadata.setCategoria(categoria);
            metadata.setUtenteUltimaModifica(utente);
            metadata.setDataUltimaModifica(LocalDateTime.now());
            metadataRepository.save(metadata);
        }
    }

    @Override
    public void impostaTags(UUID id, String tags, String utente) {
        Optional<AnnotazioneMetadata> metadataOpt = metadataRepository.findById(id);
        if (metadataOpt.isPresent()) {
            AnnotazioneMetadata metadata = metadataOpt.get();
            metadata.setTags(tags);
            metadata.setUtenteUltimaModifica(utente);
            metadata.setDataUltimaModifica(LocalDateTime.now());
            metadataRepository.save(metadata);
        }
    }

    @Override
    public void impostaPriorita(UUID id, Integer priorita, String utente) {
        Optional<AnnotazioneMetadata> metadataOpt = metadataRepository.findById(id);
        if (metadataOpt.isPresent()) {
            AnnotazioneMetadata metadata = metadataOpt.get();
            metadata.setPriorita(priorita);
            metadata.setUtenteUltimaModifica(utente);
            metadata.setDataUltimaModifica(LocalDateTime.now());
            metadataRepository.save(metadata);
        }
    }

    private List<AnnotazioneCompleta> buildAnnotazioniComplete(List<AnnotazioneMetadata> metadatas) {
        List<UUID> ids = metadatas.stream()
                .map(AnnotazioneMetadata::getId)
                .collect(Collectors.toList());

        List<Annotazione> annotazioni = annotazioneRepository.findAll().stream()
                .filter(a -> ids.contains(a.getId()))
                .collect(Collectors.toList());

        Map<UUID, AnnotazioneMetadata> metadataMap = metadatas.stream()
                .collect(Collectors.toMap(AnnotazioneMetadata::getId, m -> m));

        return annotazioni.stream()
                .map(a -> new AnnotazioneCompleta(a, metadataMap.get(a.getId())))
                .collect(Collectors.toList());
    }

}
