package app.domain.admin.account;

import java.net.URI;

import app.core.pagination.PageResponse;
import app.core.pagination.SortDirection;
import app.core.security.jwt.LiveAccountAuthenticationToken;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/accounts")
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody AccountCreateRequest request) {
        AccountResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/admin/accounts/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public AccountResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public PageResponse<AccountResponse> search(
            @RequestParam(required = false) @Size(max = 250) String username,
            @RequestParam(required = false) @Size(max = 250) String displayName,
            @RequestParam(required = false) @Size(max = 250) String email,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long roleId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "username") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        AccountSearchCriteria criteria = new AccountSearchCriteria(
                username,
                displayName,
                email,
                active,
                roleId,
                page,
                size,
                AccountSortField.fromApiValue(sort),
                SortDirection.fromApiValue(direction));
        return PageResponse.from(service.search(criteria));
    }

    @PutMapping("/{id}")
    public AccountResponse update(
            @PathVariable Long id,
            @Valid @RequestBody AccountUpdateRequest request,
            LiveAccountAuthenticationToken authentication) {
        return service.update(id, request, authentication.getAccountId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            LiveAccountAuthenticationToken authentication) {
        service.delete(id, authentication.getAccountId());
        return ResponseEntity.noContent().build();
    }
}
