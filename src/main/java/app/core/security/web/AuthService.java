package app.core.security.web;

import java.util.Locale;

import app.core.security.jwt.JwtAccessTokenResponse;
import app.core.security.jwt.JwtTokenService;
import app.domain.admin.account.Account;
import app.domain.admin.account.AccountRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokenService;

    public AuthService(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService tokenService) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public JwtAccessTokenResponse login(LoginRequest request) {
        String username = request.username().strip().toLowerCase(Locale.ROOT);
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);
        if (!account.isActivated() || !passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return tokenService.issue(account);
    }
}
