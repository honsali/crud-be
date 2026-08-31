package app.domain.rh.departement;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rh/departement")
public class DepartementController {

    private final DepartementService departementService;

    public DepartementController(DepartementService departementService) {
        this.departementService = departementService;
    }

    @PostMapping
    public ResponseEntity<DepartementResponse> creer(@Valid @RequestBody DepartementCreateRequest request) {
        DepartementResponse response = departementService.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<DepartementResponse> lister() {
        return departementService.lister();
    }

    @PutMapping("/{id}")
    public DepartementResponse maj(@PathVariable Long id, @Valid @RequestBody DepartementUpdateRequest request) {
        return departementService.maj(id, request);
    }

    @GetMapping("/{id}")
    public DepartementResponse recupererParId(@PathVariable Long id) {
        return departementService.recupererParId(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        departementService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
