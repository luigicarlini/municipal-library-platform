package it.comune.library.reservation.repository;

import it.comune.library.reservation.domain.Hold;
import it.comune.library.reservation.domain.HoldStatus;
import org.springframework.data.jpa.domain.Specification;

/**
 * Utility DSL per filtrare la query delle Holds.
 */
public final class HoldSpecs {

    private HoldSpecs() {}

    /** Filtra per stato */
    public static Specification<Hold> byStatus(HoldStatus status) {
        return (root, q, cb) -> cb.equal(root.get("status"), status);
    }

    /** Filtra per branch (case-insensitive) */
    public static Specification<Hold> byPickupBranch(String branch) {
        return (root, q, cb) ->
                cb.equal(cb.lower(root.get("pickupBranch")),
                         branch.toLowerCase());
    }

    /** Include solo libri non soft-deleted */
    public static Specification<Hold> bookNotDeleted() {
        return (root, q, cb) ->
                cb.isFalse(root.join("book").get("deleted"));
    }
}
