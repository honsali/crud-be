package app.core.referenceData;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReferenceDataController {

    private final ReferenceDataService referenceDataService;

    public ReferenceDataController(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    @GetMapping("/api/reference/{entity}")
    public List<ReferenceDataDto> lister(@PathVariable String entity) {
        return referenceDataService.getReferenceData(entity);
    }

    @GetMapping("/api/reference/{entity}/{field}/{value}")
    public List<ReferenceDataDto> filtrer(@PathVariable String entity, @PathVariable String field, @PathVariable Long value) {
        return referenceDataService.getReferenceData(entity, field, value);
    }

    @GetMapping("/api/reference/{entity}/{id}")
    public ReferenceDataDto recupererParId(@PathVariable String entity, @PathVariable Long id) {
        return referenceDataService.getReferenceData(entity, id);
    }
}
