package app.core.security.login;

import java.util.Optional;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import app.core.security.account.Account;
import app.core.security.account.AccountRepository;
import app.core.security.account.AppRole;

@Component
public class LoginTokenValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error INVALID_TOKEN = new OAuth2Error("invalid_token", "The account or role represented by this token is no longer valid.", null);

    private static OAuth2TokenValidatorResult invalid() {
        return OAuth2TokenValidatorResult.failure(INVALID_TOKEN);
    }

    private final AccountRepository accountRepository;

    public LoginTokenValidator(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        Optional<Long> accountId = JwtToken.accountId(jwt);
        Optional<Long> tokenVersion = JwtToken.tokenVersion(jwt);
        AppRole role = JwtToken.role(jwt).orElse(null);
        if (accountId.isEmpty() || tokenVersion.isEmpty() || role == null || jwt.getSubject() == null) {
            return invalid();
        }

        return accountRepository//
                .findById(accountId.get())//
                .filter(Account::isActivated)//
                .filter(account -> account.getUsername().equals(jwt.getSubject()))//
                .filter(account -> account.getRole() == role)//
                .filter(account -> account.getTokenVersion() == tokenVersion.get())//
                .map(account -> OAuth2TokenValidatorResult.success())//
                .orElseGet(LoginTokenValidator::invalid);
    }
}
