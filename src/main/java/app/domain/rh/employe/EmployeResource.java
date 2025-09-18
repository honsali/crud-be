package app.domain.rh.employe;

import java.net.URISyntaxException;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class EmployeResource {

    private final EmployeService employeService;

    public EmployeResource(EmployeService employeService) {
        this.employeService = employeService;
    }

    @PostMapping("/api/employe")
    public ResponseEntity<EmployeDto> creer(@Valid @RequestBody EmployeDto employeDto) throws URISyntaxException {
        try {
            EmployeDto result = employeService.creer(employeDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/api/employe/filtrer")
    public Page<EmployeDto> filtrer(@RequestBody EmployeFiltre filtre, @ParameterObject Pageable pageable) {
        return employeService.filtrer(filtre, pageable);
    }

    @PutMapping("/api/employe/{id}")
    public ResponseEntity<EmployeDto> maj(@PathVariable Long id, @Valid @RequestBody EmployeDto employeDto) throws URISyntaxException {
        try {
            if (employeDto.id() != null && !employeDto.id().equals(id)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path ID and body ID mismatch");
            }
            EmployeDto result = employeService.maj(id, employeDto);
            return ResponseEntity.ok().body(result);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/api/employe/{id}")
    public ResponseEntity<EmployeDto> recupererParId(@PathVariable Long id) {
        return employeService.recupererParId(id).map(dto -> ResponseEntity.ok().body(dto)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employe not found"));
    }

    @DeleteMapping("/api/employe/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        try {
            employeService.supprimer(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}