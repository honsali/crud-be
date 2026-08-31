package app.core.security.web;

import app.core.exception.ResourceNotFoundException;
import app.core.security.account.SecurityAccountProvider;
import app.core.security.credential.CredentialService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/accounts")
public class AdminCredentialController {

    private final SecurityAccountProvider accountProvider;
    private final CredentialService credentialService;

    public AdminCredentialController(SecurityAccountProvider accountProvider, CredentialService credentialService) {
        this.accountProvider = accountProvider;
        this.credentialService = credentialService;
    }

    @PutMapping("/{accountId}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> resetPassword(
            @PathVariable Long accountId,
            @Valid @RequestBody AdminPasswordResetRequest request) {
        accountProvider.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Compte", accountId));
        credentialService.resetPassword(accountId, request.newPassword());
        return ResponseEntity.noContent().build();
    }
}
