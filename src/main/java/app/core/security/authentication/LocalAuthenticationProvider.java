package app.core.security.authentication;

import java.util.List;
import java.util.Optional;

import app.core.security.account.SecurityAccountProvider;
import app.core.security.account.SecurityAccountSnapshot;
import app.core.security.credential.CredentialService;
import app.core.security.credential.LoginCredentialSnapshot;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

public class LocalAuthenticationProvider implements AuthenticationProvider {

    private static final String DUMMY_PASSWORD = "dummy-password-never-used";

    private final SecurityAccountProvider accountProvider;
    private final CredentialService credentialService;
    private final PasswordEncoder passwordEncoder;
    private final String dummyPasswordHash;

    public LocalAuthenticationProvider(
            SecurityAccountProvider accountProvider,
            CredentialService credentialService,
            PasswordEncoder passwordEncoder) {
        this.accountProvider = accountProvider;
        this.credentialService = credentialService;
        this.passwordEncoder = passwordEncoder;
        this.dummyPasswordHash = passwordEncoder.encode(DUMMY_PASSWORD);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials() instanceof String value ? value : "";
        Optional<SecurityAccountSnapshot> account = accountProvider.findByUsername(username);
        Optional<LoginCredentialSnapshot> credential = account
                .flatMap(snapshot -> credentialService.findForLogin(snapshot.accountId()));

        String hashToCheck = credential.map(LoginCredentialSnapshot::passwordHash).orElse(dummyPasswordHash);
        boolean passwordMatches = passwordEncoder.matches(password, hashToCheck);
        if (account.isEmpty() || credential.isEmpty() || !account.get().active() || !passwordMatches) {
            throw new BadCredentialsException("Identifiants invalides");
        }

        SecurityAccountSnapshot snapshot = account.get();
        LoginCredentialSnapshot credentialSnapshot = credential.get();
        AuthenticatedAccountPrincipal principal = new AuthenticatedAccountPrincipal(
                snapshot.accountId(),
                snapshot.username(),
                snapshot.roleCode(),
                credentialSnapshot.tokenVersion());
        return UsernamePasswordAuthenticationToken.authenticated(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + snapshot.roleCode())));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
