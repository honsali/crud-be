package app.domain.rh.employe;

import jakarta.validation.Valid;
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
import app.core.pagination.PageResponse;

@RestController
@RequestMapping("/api/rh/employe")
public class EmployeController {

    private final EmployeService employeService;

    public EmployeController(EmployeService employeService) {
        this.employeService = employeService;
    }

    @PostMapping
    public ResponseEntity<EmployeResponse> creer(@Valid @RequestBody EmployeCreateRequest request) {
        EmployeResponse response = employeService.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/filtrer")
    public PageResponse<EmployeResponse> filtrer(@Valid @RequestBody(required = false) EmployeFiltre filtre, Pageable pageable) {
        return PageResponse.from(employeService.filtrer(filtre, pageable));
    }

    @PutMapping("/{id}")
    public EmployeResponse maj(@PathVariable Long id, @Valid @RequestBody EmployeUpdateRequest request) {
        return employeService.maj(id, request);
    }

    @GetMapping("/{id}")
    public EmployeResponse recupererParId(@PathVariable Long id) {
        return employeService.recupererParId(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        employeService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
