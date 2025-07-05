package it.comune.library.reservation.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

/**
 * Configurazione caricata SOLO nei test.
 * Fornisce uno stub di ApplicationEventPublisher per evitare la
 * NoSuchBeanDefinitionException sollevata dal listener di dominio.
 */
@TestConfiguration
public class TestConfig {

    /** Bean fittizio: pubblica gli eventi ma non fa nulla */
    @Bean
    ApplicationEventPublisher applicationEventPublisher() {
        // ApplicationEventPublisher ha UN solo metodo astratto,
        // quindi possiamo usare una lambda.
        return event -> { /* NO-OP in test */ };
    }
}

