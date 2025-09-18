package app.domain.rh.departement;

import java.net.URISyntaxException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;

@RestController
public class DepartementResource {

    private final DepartementService departementService;

    public DepartementResource(DepartementService departementService) {
        this.departementService = departementService;
    }

    @PostMapping("/api/departement")
    public ResponseEntity<DepartementDto> creer(@Valid @RequestBody DepartementDto departementDto) throws URISyntaxException {
        try {
            DepartementDto result = departementService.creer(departementDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/api/departement")
    public List<DepartementDto> lister() {
        return departementService.lister();
    }

    @PutMapping("/api/departement/{id}")
    public ResponseEntity<DepartementDto> maj(@PathVariable Long id, @Valid @RequestBody DepartementDto departementDto) throws URISyntaxException {
        try {
            if (departementDto.id() != null && !departementDto.id().equals(id)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path ID and body ID mismatch");
            }
            DepartementDto result = departementService.maj(id, departementDto);
            return ResponseEntity.ok().body(result);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/api/departement/{id}")
    public ResponseEntity<DepartementDto> recupererParId(@PathVariable Long id) {
        return departementService.recupererParId(id).map(dto -> ResponseEntity.ok().body(dto)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Departement not found"));
    }

    @DeleteMapping("/api/departement/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        try {
            departementService.supprimer(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}