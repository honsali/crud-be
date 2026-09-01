package app.domain.admin.account;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/accounts")
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> creer(@Valid @RequestBody AccountCreateRequest request) {
        AccountResponse response = service.creer(request);
        return ResponseEntity.created(URI.create("/api/admin/accounts/" + response.id())).body(response);
    }

    @GetMapping
    public List<AccountResponse> lister() {
        return service.lister();
    }

    @GetMapping("/{id}")
    public AccountResponse recupererParId(@PathVariable Long id) {
        return service.recupererParId(id);
    }

    @PutMapping("/{id}")
    public AccountResponse maj(@PathVariable Long id, @Valid @RequestBody AccountUpdateRequest request) {
        return service.maj(id, request);
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> reinitialiserMotDePasse(
            @PathVariable Long id,
            @Valid @RequestBody PasswordResetRequest request) {
        service.reinitialiserMotDePasse(id, request);
        return ResponseEntity.noContent().build();
    }
}
