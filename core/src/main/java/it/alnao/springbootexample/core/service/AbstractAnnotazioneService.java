package it.alnao.springbootexample.core.service;

import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository;
import it.alnao.springbootexample.core.repository.AnnotazioneRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Base class che implementa i metodi comuni di AnnotazioneService
 * condivisi tra tutti gli adapter (MongoDB, AWS, Azure).
 * Gli adapter estendono questa classe e implementano solo i metodi
 * specifici del loro backend (creaAnnotazione, aggiornaAnnotazione, cambiaStato).
 */
public abstract class AbstractAnnotazioneService implements AnnotazioneService {

    protected abstract AnnotazioneRepository getAnnotazioneRepository();

    protected abstract AnnotazioneMetadataRepository getMetadataRepository();

    // ---- Read operations ----

    @Override
    public AnnotazioneCompleta cambiaStato(UUID id, String nuovoStato, String utenteModifica) {
        AnnotazioneMetadata metadata = requireMetadata(id);
        Optional<Annotazione> annotazioneOpt = getAnnotazioneRepository().findById(id);
        if (annotazioneOpt.isEmpty()) {
            throw new RuntimeException("Annotazione non trovata con ID: " + id);
        }
        boolean statoValido = false;
        for (StatoAnnotazione statoEnum : StatoAnnotazione.values()) {
            if (statoEnum.getValue().equals(nuovoStato)) {
                statoValido = true;
                break;
            }
        }
        if (!statoValido) {
            throw new IllegalArgumentException("Stato annotazione non valido: " + nuovoStato);
        }
        metadata.setStato(nuovoStato);
        metadata.setUtenteUltimaModifica(utenteModifica);
        metadata.setDataUltimaModifica(LocalDateTime.now());
        AnnotazioneMetadata updatedMetadata = getMetadataRepository().save(metadata);
        return new AnnotazioneCompleta(annotazioneOpt.get(), updatedMetadata);
    }

    @Override
    public Optional<AnnotazioneCompleta> trovaPerID(UUID id) {
        Optional<Annotazione> annotazione = getAnnotazioneRepository().findById(id);
        Optional<AnnotazioneMetadata> metadata = getMetadataRepository().findById(id);
        if (annotazione.isPresent() && metadata.isPresent()) {
            return Optional.of(new AnnotazioneCompleta(annotazione.get(), metadata.get()));
        }
        return Optional.empty();
    }

    @Override
    public List<AnnotazioneCompleta> trovaTutte() {
        List<Annotazione> annotazioni = getAnnotazioneRepository().findAll();
        List<AnnotazioneMetadata> metadatas = getMetadataRepository().findAll();
        java.util.Map<UUID, AnnotazioneMetadata> metadataMap = metadatas.stream()
                .collect(Collectors.toMap(AnnotazioneMetadata::getId, m -> m));
        return annotazioni.stream()
                .filter(a -> metadataMap.containsKey(a.getId()))
                .map(a -> new AnnotazioneCompleta(a, metadataMap.get(a.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneCompleta> trovaPerUtente(String utente) {
        return buildAnnotazioniCompleteFromMetadata(
                getMetadataRepository().findByUtenteCreazione(utente));
    }

    @Override
    public List<AnnotazioneCompleta> trovaPerCategoria(String categoria) {
        return buildAnnotazioniCompleteFromMetadata(
                getMetadataRepository().findByCategoria(categoria));
    }

    @Override
    public List<AnnotazioneCompleta> trovaPerPeriodo(LocalDateTime inizio, LocalDateTime fine) {
        return buildAnnotazioniCompleteFromMetadata(
                getMetadataRepository().findByDataInserimentoBetween(inizio, fine));
    }

    @Override
    public List<AnnotazioneCompleta> trovaPubbliche() {
        return buildAnnotazioniCompleteFromMetadata(
                getMetadataRepository().findByPubblica(true));
    }

    @Override
    public List<AnnotazioneCompleta> trovaPerStato(StatoAnnotazione stato) {
        return buildAnnotazioniCompleteFromMetadata(
                getMetadataRepository().findByStato(stato));
    }

    @Override
    public List<AnnotazioneCompleta> cercaPerTesto(String testo) {
        List<Annotazione> annotazioni = getAnnotazioneRepository().findByValoreNotaContaining(testo);
        List<AnnotazioneMetadata> metadatas = getMetadataRepository().findByDescrizioneContaining(testo);

        List<UUID> ids = annotazioni.stream().map(Annotazione::getId).collect(Collectors.toList());
        metadatas.stream().map(AnnotazioneMetadata::getId).forEach(ids::add);

        return ids.stream()
                .distinct()
                .map(id -> trovaPerID(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    // ---- Count / exists ----

    @Override
    public boolean esisteAnnotazione(UUID id) {
        return getAnnotazioneRepository().existsById(id) && getMetadataRepository().existsById(id);
    }

    @Override
    public long contaAnnotazioni() {
        return getAnnotazioneRepository().count();
    }

    @Override
    public long contaAnnotazioniPerUtente(String utente) {
        return getMetadataRepository().countByUtenteCreazione(utente);
    }

    // ---- Delete ----

    @Override
    public void eliminaAnnotazione(UUID id) {
        if (getAnnotazioneRepository().existsById(id) && getMetadataRepository().existsById(id)) {
            getAnnotazioneRepository().deleteById(id);
            getMetadataRepository().deleteById(id);
        }
    }

    // ---- Metadata setters ----

    @Override
    public void impostaVisibilitaPubblica(UUID id, boolean pubblica, String utente) {
        AnnotazioneMetadata metadata = requireMetadata(id);
        metadata.setPubblica(pubblica);
        metadata.setDataUltimaModifica(LocalDateTime.now());
        metadata.setUtenteUltimaModifica(utente);
        getMetadataRepository().save(metadata);
    }

    @Override
    public void impostaCategoria(UUID id, String categoria, String utente) {
        AnnotazioneMetadata metadata = requireMetadata(id);
        metadata.setCategoria(categoria);
        metadata.setDataUltimaModifica(LocalDateTime.now());
        metadata.setUtenteUltimaModifica(utente);
        getMetadataRepository().save(metadata);
    }

    @Override
    public void impostaTags(UUID id, String tags, String utente) {
        AnnotazioneMetadata metadata = requireMetadata(id);
        metadata.setTags(tags);
        metadata.setDataUltimaModifica(LocalDateTime.now());
        metadata.setUtenteUltimaModifica(utente);
        getMetadataRepository().save(metadata);
    }

    @Override
    public void impostaPriorita(UUID id, Integer priorita, String utente) {
        AnnotazioneMetadata metadata = requireMetadata(id);
        metadata.setPriorita(priorita);
        metadata.setDataUltimaModifica(LocalDateTime.now());
        metadata.setUtenteUltimaModifica(utente);
        getMetadataRepository().save(metadata);
    }

    // ---- Helpers ----

    /**
     * Finds metadata by id or throws RuntimeException if not found.
     */
    protected AnnotazioneMetadata requireMetadata(UUID id) {
        return getMetadataRepository().findById(id)
                .orElseThrow(() -> new RuntimeException("Metadata non trovati per ID: " + id));
    }

    /**
     * Assembles AnnotazioneCompleta list from a metadata list,
     * loading each annotazione by ID.
     */
    protected List<AnnotazioneCompleta> buildAnnotazioniCompleteFromMetadata(List<AnnotazioneMetadata> metadatas) {
        return metadatas.stream()
                .map(metadata -> getAnnotazioneRepository().findById(metadata.getId())
                        .map(a -> new AnnotazioneCompleta(a, metadata))
                        .orElse(null))
                .filter(ac -> ac != null)
                .collect(Collectors.toList());
    }
}
