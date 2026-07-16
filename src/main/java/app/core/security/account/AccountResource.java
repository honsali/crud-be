package app.core.security.account;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AccountResource {

    public record CreateAccountRequest(@NotBlank @Size(max = 50) String username, @NotBlank @Size(min = 8, max = 256) String password, @NotNull AppRole role) {
    }

    public record UpdateAccountRequest(@NotNull AppRole role, @NotNull Boolean activated) {
    }

    public record ResetPasswordRequest(@NotBlank @Size(min = 8, max = 256) String password) {
    }

    private final AccountService accountService;

    public AccountResource(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/api/admin/accounts")
    public List<AccountDto> list() {
        return accountService.list();
    }

    @PostMapping("/api/admin/accounts")
    public ResponseEntity<AccountDto> create(@Valid @RequestBody CreateAccountRequest request) {
        AccountDto result = accountService.create(request.username(), request.password(), request.role());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/api/admin/accounts/{id}")
    public AccountDto update(@PathVariable Long id, @Valid @RequestBody UpdateAccountRequest request, Authentication authentication) {
        return accountService.update(id, request.role(), request.activated(), authentication.getName());
    }

    @PutMapping("/api/admin/accounts/{id}/password")
    public ResponseEntity<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        accountService.resetPassword(id, request.password());
        return ResponseEntity.noContent().build();
    }
}
