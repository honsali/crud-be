package app.domain.rh.referenceData;

import java.util.List;
import app.core.referenceData.ReferenceDataDto;
import app.core.referenceData.ReferenceDataService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rh/reference")
@PreAuthorize("hasAuthority('ROLE_GESTIONNAIRE_RH')")
public class RhReferenceDataResource {

    private final ReferenceDataService referenceDataService;

    public RhReferenceDataResource(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    @GetMapping("/{entity}")
    public List<ReferenceDataDto> lister(@PathVariable String entity) {
        return referenceDataService.getReferenceData(entity);
    }

    @GetMapping("/{entity}/{field}/{value}")
    public List<ReferenceDataDto> filtrer(@PathVariable String entity, @PathVariable String field, @PathVariable Long value) {
        return referenceDataService.getReferenceData(entity, field, value);
    }

    @GetMapping("/{entity}/{id}")
    public ReferenceDataDto recupererParId(@PathVariable String entity, @PathVariable Long id) {
        return referenceDataService.getReferenceData(entity, id);
    }
}
