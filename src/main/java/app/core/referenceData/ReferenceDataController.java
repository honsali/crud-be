package app.core.referenceData;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reference")
public class ReferenceDataController {

    private final ReferenceDataService referenceDataService;

    public ReferenceDataController(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    @GetMapping("/{entity}")
    public ResponseEntity<List<ReferenceDataDto>> getReferenceData(@PathVariable String entity) {
        try {
            List<ReferenceDataDto> referenceData = referenceDataService.getReferenceData(entity);
            return ResponseEntity.ok(referenceData);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{entity}/{field}/{value}")
    public ResponseEntity<List<ReferenceDataDto>> getReferenceData(@PathVariable String entity, @PathVariable String field, @PathVariable Long value) {
        try {
            List<ReferenceDataDto> referenceData = referenceDataService.getReferenceData(entity, field, value);
            return ResponseEntity.ok(referenceData);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{entity}/{id}")
    public ResponseEntity<ReferenceDataDto> getReferenceData(@PathVariable String entity, @PathVariable Long id) {
        try {
            ReferenceDataDto referenceData = referenceDataService.getReferenceData(entity, id);
            return ResponseEntity.ok(referenceData);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
