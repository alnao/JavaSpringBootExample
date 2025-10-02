package it.alnao.springbootexample.azure.service;

import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventData;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository;
import it.alnao.springbootexample.core.service.AnnotazioneInvioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Profile("azure")
public class AnnotazioneInvioServiceAzureImpl implements AnnotazioneInvioService {

    private final AnnotazioneMetadataRepository metadataRepository;
    private final EventHubProducerClient producerClient;
    private final boolean enabled;

    public AnnotazioneInvioServiceAzureImpl(
            AnnotazioneMetadataRepository metadataRepository,
            @Value("${azure.eventhubs.connection-string}") String connectionString,
            @Value("${azure.eventhubs.name}") String eventHubName,
            @Value("${annotazione.invio.enabled:true}") boolean enabled) {
        this.metadataRepository = metadataRepository;
        this.enabled = enabled;
        this.producerClient = new EventHubClientBuilder()
                .connectionString(connectionString, eventHubName)
                .buildProducerClient();
    }

    @Override
    public List<AnnotazioneCompleta> inviaAnnotazioni() {
        if (!enabled) return List.of();
        // Recupera annotazioni in stato DAINVIARE
        List<AnnotazioneCompleta> daInviare = metadataRepository.findByStato(StatoAnnotazione.DAINVIARE)
                .stream()
                .map(meta -> new AnnotazioneCompleta(null, meta))
                .collect(Collectors.toList());
        for (AnnotazioneCompleta ann : daInviare) {
            producerClient.send(java.util.Collections.singletonList(new EventData(ann.toString())));
            // Aggiorna stato a INVIATA
            var meta = ann.getMetadata();
            meta.setStato(StatoAnnotazione.INVIATA.name());
            metadataRepository.save(meta);

        }
        return daInviare;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
