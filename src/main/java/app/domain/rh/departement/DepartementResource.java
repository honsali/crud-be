package app.domain.rh.departement;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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
@RequestMapping("/api/departement")
@Transactional
public class DepartementResource {

    private final DepartementRepository departementRepository;

    public DepartementResource(DepartementRepository departementRepository) {
        this.departementRepository = departementRepository;
    }

    @PostMapping
    public ResponseEntity<DepartementDto> creer(@Valid @RequestBody Departement departement) throws URISyntaxException {
        Departement result = departementRepository.save(departement);
        return ResponseEntity.ok().body(DepartementDto.asEntity(result));
    }

    @PutMapping
    public ResponseEntity<Void> enregistrer(@Valid @RequestBody Departement departement) throws URISyntaxException {
        departementRepository.save(departement);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/lister")
    public List<DepartementDto> lister() {
        return departementRepository.findAllByOrderByNom().stream().map(DepartementDto::asEntity).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartementDto> recupererParId(@PathVariable Long id) {
        Optional<Departement> departement = departementRepository.findById(id);
        return departement.map(response -> ResponseEntity.ok().body(DepartementDto.asEntity(response))).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}