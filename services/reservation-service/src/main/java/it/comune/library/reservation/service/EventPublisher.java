package it.comune.library.reservation.service;

/**
 * Pubblicatore di eventi di dominio dell’applicazione.
 *
 * <p>Nel codice di produzione può essere implementato delegando, ad esempio,
 * a {@code org.springframework.context.ApplicationEventPublisher},
 * a un message broker (Kafka, RabbitMQ…), oppure a un bus interno.
 *
 * <p>Per i test d’integrazione è sufficiente definire
 * l’interfaccia — il bean reale verrà sostituito con un mock tramite
 * {@code @MockBean}.
 */
public interface EventPublisher {

    /**
     * Pubblica un evento di dominio.
     *
     * @param event istanza dell’evento da pubblicare (può essere qualsiasi
     *              oggetto, normalmente un record o una classe che rappresenta
     *              il fatto di dominio)
     */
    void publish(Object event);
}
