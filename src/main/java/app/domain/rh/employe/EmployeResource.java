package app.domain.rh.employe;

import java.util.NoSuchElementException;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/employe")
public class EmployeResource {

    private final EmployeService employeService;

    public EmployeResource(EmployeService employeService) {
        this.employeService = employeService;
    }

    @PostMapping
    public ResponseEntity<EmployeDto> creer(@Valid @RequestBody EmployeDto employeDto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(employeService.creer(employeDto));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/filtrer")
    public Page<EmployeDto> filtrer(@RequestBody(required = false) EmployeFiltre filtre, Pageable pageable) {
        return employeService.filtrer(filtre, pageable);
    }

    @PutMapping("/{id}")
    public EmployeDto maj(@PathVariable Long id, @Valid @RequestBody EmployeDto employeDto) {
        if (employeDto.id() != null && !employeDto.id().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path ID and body ID mismatch");
        }

        try {
            return employeService.maj(id, employeDto);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public EmployeDto recupererParId(@PathVariable Long id) {
        return employeService.recupererParId(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employe not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        try {
            employeService.supprimer(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
