package no.entur.uttu.ext.entur.config;

import org.entur.pubsub.base.config.GooglePubSubConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("entur-pubsub-messaging-service")
@Import(GooglePubSubConfig.class)
public class EnturConfiguration {}
