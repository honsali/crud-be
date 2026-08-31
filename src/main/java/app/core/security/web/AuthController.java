package app.core.security.web;

import app.core.security.credential.CredentialService;
import app.core.security.jwt.JwtAccessTokenResponse;
import app.core.security.jwt.LiveAccountAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CredentialService credentialService;

    public AuthController(AuthService authService, CredentialService credentialService) {
        this.authService = authService;
        this.credentialService = credentialService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAccessTokenResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header("Pragma", "no-cache")
                .body(authService.login(request, servletRequest.getRemoteAddr()));
    }

    @GetMapping("/me")
    public CurrentAccountResponse me(LiveAccountAuthenticationToken authentication) {
        return new CurrentAccountResponse(
                authentication.getAccountId(), authentication.getName(), authentication.getRoleCode());
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            LiveAccountAuthenticationToken authentication,
            @Valid @RequestBody PasswordChangeRequest request) {
        credentialService.changePassword(
                authentication.getAccountId(), request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(LiveAccountAuthenticationToken authentication) {
        credentialService.revokeAllTokens(authentication.getAccountId());
        return ResponseEntity.noContent().build();
    }
}
