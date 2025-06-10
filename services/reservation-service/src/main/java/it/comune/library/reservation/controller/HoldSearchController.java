package it.comune.library.reservation.controller;

import it.comune.library.reservation.domain.Hold;
import it.comune.library.reservation.domain.HoldStatus;
import it.comune.library.reservation.repository.HoldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/holds", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class HoldSearchController {

    private final HoldRepository repo;

    /**
     * GET /holds?title=&author=&pickupBranch=&status=&genre=…
     * Compatibilità con i vecchi test.
     */
    @GetMapping(params = {
            "title",
            "author",
            "pickupBranch",
            "status",
            "genre",
            "publicationYear",
            "position"
    })
    public List<Hold> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String pickupBranch,
            @RequestParam(required = false) HoldStatus status,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Integer publicationYear,
            @RequestParam(required = false) Integer position
    ) {
        return repo.searchByOptionalFilters(
                title, author, pickupBranch, status, genre, publicationYear, position
        );
    }
}
