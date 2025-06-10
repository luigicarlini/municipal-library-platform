package it.comune.library.reservation.service;

import org.springframework.stereotype.Component;

import java.util.Random;

/** Mock che simula un gateway: true=pagamento riuscito. */
@Component
public class PaymentService {
    private final Random rnd = new Random();

    public boolean charge(String fakeToken, long cents) {
        // 80% di successo
        return rnd.nextInt(10) < 8;
    }
}
