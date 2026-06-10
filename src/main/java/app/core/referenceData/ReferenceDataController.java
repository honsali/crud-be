package app.core.referenceData;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ReferenceDataController {

    private final ReferenceDataService referenceDataService;

    public ReferenceDataController(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    @GetMapping("/api/reference/{entity}")
    public List<ReferenceDataDto> lister(@PathVariable String entity) {
        try {
            return referenceDataService.getReferenceData(entity);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/api/reference/{entity}/{field}/{value}")
    public List<ReferenceDataDto> filtrer(@PathVariable String entity, @PathVariable String field, @PathVariable Long value) {
        try {
            return referenceDataService.getReferenceData(entity, field, value);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/api/reference/{entity}/{id}")
    public ReferenceDataDto recupererParId(@PathVariable String entity, @PathVariable Long id) {
        try {
            return referenceDataService.getReferenceData(entity, id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
