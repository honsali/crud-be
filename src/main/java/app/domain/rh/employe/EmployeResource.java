package app.domain.rh.employe;

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
import app.core.PageResponse;
import jakarta.validation.Valid;

@RestController
public class EmployeResource {

    private final EmployeService employeService;

    public EmployeResource(EmployeService employeService) {
        this.employeService = employeService;
    }

    @PostMapping("/api/employe")
    public ResponseEntity<EmployeDto> creer(@Valid @RequestBody EmployeDto employeDto) {
        EmployeDto result = employeService.creer(employeDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/api/employe/filtrer")
    public PageResponse<EmployeDto> filtrer(@Valid @RequestBody(required = false) EmployeFiltre filtre, Pageable pageable) {
        return PageResponse.from(employeService.filtrer(filtre, pageable));
    }

    @PutMapping("/api/employe/{id}")
    public EmployeDto maj(@PathVariable Long id, @Valid @RequestBody EmployeDto employeDto) {
        if (employeDto.id() != null && !employeDto.id().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path ID and body ID mismatch");
        }

        return employeService.maj(id, employeDto);
    }

    @GetMapping("/api/employe/{id}")
    public EmployeDto recupererParId(@PathVariable Long id) {
        return employeService.recupererParId(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employe not found"));
    }

    @DeleteMapping("/api/employe/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        employeService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
