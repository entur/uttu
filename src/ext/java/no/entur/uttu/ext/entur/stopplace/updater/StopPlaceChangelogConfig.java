package no.entur.uttu.ext.entur.stopplace.updater;

import no.entur.uttu.stopplace.NetexPublicationDeliveryFileStopPlaceRegistry;
import org.rutebanken.helper.stopplace.changelog.kafka.PublicationTimeRecordFilterStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StopPlaceChangelogConfig {

    @Bean("publicationTimeRecordFilterStrategy")
    public PublicationTimeRecordFilterStrategy publicationTimeRecordFilterStrategy(
      NetexPublicationDeliveryFileStopPlaceRegistry registry
    ) {
        return new PublicationTimeRecordFilterStrategy(registry.getPublicationTime());
    }
}
