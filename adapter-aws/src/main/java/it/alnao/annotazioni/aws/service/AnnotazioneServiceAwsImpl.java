package it.alnao.annotazioni.aws.service;

import it.alnao.annotazioni.aws.entity.AnnotazioneStoricoDynamoEntity;
import it.alnao.annotazioni.aws.repository.AnnotazioneStoricoDynamoRepository;
import it.alnao.annotazioni.port.domain.Annotazione;
import it.alnao.annotazioni.port.domain.AnnotazioneCompleta;
import it.alnao.annotazioni.port.domain.AnnotazioneMetadata;
import it.alnao.annotazioni.port.repository.AnnotazioneRepository;
import it.alnao.annotazioni.port.repository.AnnotazioneMetadataRepository;
import it.alnao.annotazioni.port.service.AnnotazioneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile("aws")
public class AnnotazioneServiceAwsImpl implements AnnotazioneService {

    @Autowired
    private AnnotazioneRepository annotazioneRepository;

    @Autowired
    private AnnotazioneMetadataRepository metadataRepository;

    @Autowired
    private AnnotazioneStoricoDynamoRepository storicoDynamoRepository;

    @Override
    public AnnotazioneCompleta creaAnnotazione(String valoreNota, String descrizione, String utente) {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // Crea annotazione DynamoDB
        Annotazione annotazione = new Annotazione();
        annotazione.setId(id);
        annotazione.setVersioneNota("1.0");
        annotazione.setValoreNota(valoreNota);
        Annotazione savedAnnotazione = annotazioneRepository.save(annotazione);

        // Crea metadata MySQL
        AnnotazioneMetadata metadata = new AnnotazioneMetadata();
        metadata.setId(id);
        metadata.setVersioneNota("1.0");
        metadata.setUtenteCreazione(utente);
        metadata.setDataInserimento(now);
        metadata.setDataUltimaModifica(now);
        metadata.setUtenteUltimaModifica(utente);
        metadata.setDescrizione(descrizione);
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

            // 1. Salva la versione precedente nello storico Dynamo
            AnnotazioneStoricoDynamoEntity storico = new AnnotazioneStoricoDynamoEntity();
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
            storicoDynamoRepository.save(storico);

            // 2. Aggiorna annotazione
            if (nuovoValore != null) {
                annotazione.setValoreNota(nuovoValore);
                annotazione.setVersioneNota(incrementaVersione(annotazione.getVersioneNota()));
            }
            Annotazione updatedAnnotazione = annotazioneRepository.save(annotazione);

            // 3. Aggiorna metadata
            metadata.setDataUltimaModifica(LocalDateTime.now());
            metadata.setUtenteUltimaModifica(utente);
            if (nuovaDescrizione != null) {
                metadata.setDescrizione(nuovaDescrizione);
            }
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

        return annotazioni.stream()
                .map(a -> {
                    Optional<AnnotazioneMetadata> metadata = metadatas.stream()
                            .filter(m -> m.getId().equals(a.getId()))
                            .findFirst();
                    return metadata.map(m -> new AnnotazioneCompleta(a, m)).orElse(null);
                })
                .filter(ac -> ac != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneCompleta> trovaPerUtente(String utente) {
        List<AnnotazioneMetadata> metadatas = metadataRepository.findByUtenteCreazione(utente);
        
        return metadatas.stream()
                .map(metadata -> {
                    Optional<Annotazione> annotazione = annotazioneRepository.findById(metadata.getId());
                    return annotazione.map(a -> new AnnotazioneCompleta(a, metadata)).orElse(null);
                })
                .filter(ac -> ac != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneCompleta> trovaPerCategoria(String categoria) {
        List<AnnotazioneMetadata> metadatas = metadataRepository.findByCategoria(categoria);
        
        return metadatas.stream()
                .map(metadata -> {
                    Optional<Annotazione> annotazione = annotazioneRepository.findById(metadata.getId());
                    return annotazione.map(a -> new AnnotazioneCompleta(a, metadata)).orElse(null);
                })
                .filter(ac -> ac != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneCompleta> trovaPerPeriodo(LocalDateTime inizio, LocalDateTime fine) {
        List<AnnotazioneMetadata> metadatas = metadataRepository.findByDataInserimentoBetween(inizio, fine);
        
        return metadatas.stream()
                .map(metadata -> {
                    Optional<Annotazione> annotazione = annotazioneRepository.findById(metadata.getId());
                    return annotazione.map(a -> new AnnotazioneCompleta(a, metadata)).orElse(null);
                })
                .filter(ac -> ac != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneCompleta> cercaPerTesto(String testo) {
        List<Annotazione> annotazioni = annotazioneRepository.findByValoreNotaContaining(testo);
        List<AnnotazioneMetadata> metadatas = metadataRepository.findByDescrizioneContaining(testo);

        List<UUID> annotazioniIds = annotazioni.stream().map(Annotazione::getId).collect(Collectors.toList());
        List<UUID> metadataIds = metadatas.stream().map(AnnotazioneMetadata::getId).collect(Collectors.toList());

        // Unisci gli ID 
        annotazioniIds.addAll(metadataIds);
        List<UUID> allIds = annotazioniIds.stream().distinct().collect(Collectors.toList());

        return allIds.stream()
                .map(id -> trovaPerID(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneCompleta> trovaPubbliche() {
        List<AnnotazioneMetadata> metadatas = metadataRepository.findByPubblica(true);
        
        return metadatas.stream()
                .map(metadata -> {
                    Optional<Annotazione> annotazione = annotazioneRepository.findById(metadata.getId());
                    return annotazione.map(a -> new AnnotazioneCompleta(a, metadata)).orElse(null);
                })
                .filter(ac -> ac != null)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminaAnnotazione(UUID id) {
        if (annotazioneRepository.existsById(id) && metadataRepository.existsById(id)) {
            annotazioneRepository.deleteById(id);
            metadataRepository.deleteById(id);
        } else {
            throw new RuntimeException("Annotazione non trovata con ID: " + id);
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
        Optional<AnnotazioneMetadata> metadata = metadataRepository.findById(id);
        if (metadata.isPresent()) {
            AnnotazioneMetadata meta = metadata.get();
            meta.setPubblica(pubblica);
            meta.setDataUltimaModifica(LocalDateTime.now());
            meta.setUtenteUltimaModifica(utente);
            metadataRepository.save(meta);
        } else {
            throw new RuntimeException("Metadata non trovati per ID: " + id);
        }
    }

    @Override
    public void impostaCategoria(UUID id, String categoria, String utente) {
        Optional<AnnotazioneMetadata> metadata = metadataRepository.findById(id);
        if (metadata.isPresent()) {
            AnnotazioneMetadata meta = metadata.get();
            meta.setCategoria(categoria);
            meta.setDataUltimaModifica(LocalDateTime.now());
            meta.setUtenteUltimaModifica(utente);
            metadataRepository.save(meta);
        } else {
            throw new RuntimeException("Metadata non trovati per ID: " + id);
        }
    }

    @Override
    public void impostaTags(UUID id, String tags, String utente) {
        Optional<AnnotazioneMetadata> metadata = metadataRepository.findById(id);
        if (metadata.isPresent()) {
            AnnotazioneMetadata meta = metadata.get();
            meta.setTags(tags);
            meta.setDataUltimaModifica(LocalDateTime.now());
            meta.setUtenteUltimaModifica(utente);
            metadataRepository.save(meta);
        } else {
            throw new RuntimeException("Metadata non trovati per ID: " + id);
        }
    }

    @Override
    public void impostaPriorita(UUID id, Integer priorita, String utente) {
        Optional<AnnotazioneMetadata> metadata = metadataRepository.findById(id);
        if (metadata.isPresent()) {
            AnnotazioneMetadata meta = metadata.get();
            meta.setPriorita(priorita);
            meta.setDataUltimaModifica(LocalDateTime.now());
            meta.setUtenteUltimaModifica(utente);
            metadataRepository.save(meta);
        } else {
            throw new RuntimeException("Metadata non trovati per ID: " + id);
        }
    }

    /**
     * Incrementa la versione da stringa (es. "1.0" -> "1.1", "v2.3" -> "v2.4")
     */
    private String incrementaVersione(String versioneCorrente) {
        if (versioneCorrente == null || versioneCorrente.isEmpty()) {
            return "1.0";
        }
        
        // Cerca pattern come "1.0", "2.3", "v1.5"
        if (versioneCorrente.matches("^v?\\d+\\.\\d+$")) {
            String prefix = versioneCorrente.startsWith("v") ? "v" : "";
            String numberPart = versioneCorrente.replace("v", "");
            String[] parts = numberPart.split("\\.");
            
            try {
                int major = Integer.parseInt(parts[0]);
                int minor = Integer.parseInt(parts[1]);
                minor++;
                return prefix + major + "." + minor;
            } catch (NumberFormatException e) {
                // Fallback: aggiungi .1
                return versioneCorrente + ".1";
            }
        } else {
            // Per versioni non standard, aggiungi .1
            return versioneCorrente + ".1";
        }
    }
}
