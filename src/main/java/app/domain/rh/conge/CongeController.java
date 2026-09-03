package app.domain.rh.conge;

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
@RequestMapping("/api/rh")
public class CongeController {

    private final CongeService congeService;

    public CongeController(CongeService congeService) {
        this.congeService = congeService;
    }

    @PostMapping("/employes/{idEmploye}/conges")
    public ResponseEntity<CongeResponse> creer(@PathVariable Long idEmploye, @Valid @RequestBody CongeCreateRequest request) {
        CongeResponse response = congeService.creer(idEmploye, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/employes/{idEmploye}/conges")
    public List<CongeResponse> listerParIdEmploye(@PathVariable Long idEmploye) {
        return congeService.listerParIdEmploye(idEmploye);
    }

    @PutMapping("/conges/{id}")
    public CongeResponse maj(@PathVariable Long id, @Valid @RequestBody CongeUpdateRequest request) {
        return congeService.maj(id, request);
    }

    @GetMapping("/conges/{id}")
    public CongeResponse recupererParId(@PathVariable Long id) {
        return congeService.recupererParId(id);
    }

    @DeleteMapping("/conges/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        congeService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
