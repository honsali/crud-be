package app.domain.rh.conge;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/rh")
@PreAuthorize("hasAuthority('ROLE_GESTIONNAIRE_RH')")
public class CongeResource {

    private final CongeService congeService;

    public CongeResource(CongeService congeService) {
        this.congeService = congeService;
    }

    @PostMapping("/employe/{idEmploye}/conge")
    public ResponseEntity<CongeDto> creer(@PathVariable Long idEmploye, @Valid @RequestBody CongeDto congeDto) {
        CongeDto result = congeService.creer(idEmploye, congeDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/conge/employe/{idEmploye}")
    public List<CongeDto> listerParIdEmploye(@PathVariable Long idEmploye) {
        return congeService.listerParIdEmploye(idEmploye);
    }

    @PutMapping("/conge/{id}")
    public CongeDto maj(@PathVariable Long id, @Valid @RequestBody CongeDto congeDto) {
        if (congeDto.id() != null && !congeDto.id().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path ID and body ID mismatch");
        }

        return congeService.maj(id, congeDto);
    }

    @GetMapping("/conge/{id}")
    public CongeDto recupererParId(@PathVariable Long id) {
        return congeService.recupererParId(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conge not found"));
    }

    @DeleteMapping("/conge/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        congeService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
