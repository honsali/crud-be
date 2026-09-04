package app.domain.admin.account;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountResponse> creer(@Valid @RequestBody AccountCreateRequest request) {
        AccountResponse response = accountService.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/accounts")
    public List<AccountResponse> lister() {
        return accountService.lister();
    }

    @PutMapping("/accounts/{id}")
    public AccountResponse maj(@PathVariable Long id, @Valid @RequestBody AccountUpdateRequest request) {
        return accountService.maj(id, request);
    }

    @GetMapping("/accounts/{id}")
    public AccountResponse recupererParId(@PathVariable Long id) {
        return accountService.recupererParId(id);
    }

    @PutMapping("/accounts/{id}/password")
    public ResponseEntity<Void> reinitialiserMotDePasse(@PathVariable Long id, @Valid @RequestBody PasswordResetRequest request) {
        accountService.reinitialiserMotDePasse(id, request);
        return ResponseEntity.noContent().build();
    }
}
