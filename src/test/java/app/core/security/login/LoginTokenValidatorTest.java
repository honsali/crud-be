package app.core.security.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import app.domain.admin.account.Account;
import app.domain.admin.account.AccountRepository;
import app.domain.admin.account.AppRole;

@ExtendWith(MockitoExtension.class)
class LoginTokenValidatorTest {

    private static Jwt token(Object role, Object tokenVersion) {
        return token(7L, tokenVersion, role);
    }

    private static Jwt token(Object accountId, Object tokenVersion, Object role) {
        return Jwt.withTokenValue("token")
                .header("alg", "HS512")
                .subject("manager")
                .claim(JwtToken.ACCOUNT_ID_CLAIM, accountId)
                .claim(JwtToken.TOKEN_VERSION_CLAIM, tokenVersion)
                .claim(JwtToken.ROLE_CLAIM, role)
                .build();
    }

    private static Account account() {
        Account account = new Account();
        account.setId(7L);
        account.setUsername("manager");
        account.setPasswordHash("hash");
        account.setRole(AppRole.ROLE_GESTIONNAIRE_RH);
        account.setActivated(true);
        return account;
    }

    @Mock
    private AccountRepository accountRepository;

    private LoginTokenValidator validator;

    @BeforeEach
    void setUp() {
        validator = new LoginTokenValidator(accountRepository);
    }

    @Test
    void acceptsTheCurrentActivatedAccountRoleAndTokenVersion() {
        Account account = account();
        when(accountRepository.findById(7L)).thenReturn(Optional.of(account));

        OAuth2TokenValidatorResult result = validator.validate(token(
                AppRole.ROLE_GESTIONNAIRE_RH.name(), 0L));

        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void rejectsARoleArray() {
        OAuth2TokenValidatorResult result = validator.validate(token(
                List.of(AppRole.ROLE_ADMIN.name(), AppRole.ROLE_GESTIONNAIRE_RH.name()), 0L));

        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    void rejectsAnUnknownRole() {
        OAuth2TokenValidatorResult result = validator.validate(token("ROLE_UNKNOWN", 0L));

        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    void rejectsFractionalNumericClaims() {
        OAuth2TokenValidatorResult fractionalAccountId = validator.validate(token(
                7.5, 0L, AppRole.ROLE_GESTIONNAIRE_RH.name()));
        OAuth2TokenValidatorResult fractionalTokenVersion = validator.validate(token(
                7L, 0.5, AppRole.ROLE_GESTIONNAIRE_RH.name()));

        assertThat(fractionalAccountId.getErrors()).isNotEmpty();
        assertThat(fractionalTokenVersion.getErrors()).isNotEmpty();
    }

    @Test
    void rejectsOutOfRangeNumericClaims() {
        OAuth2TokenValidatorResult zeroAccountId = validator.validate(token(
                0L, 0L, AppRole.ROLE_GESTIONNAIRE_RH.name()));
        OAuth2TokenValidatorResult negativeTokenVersion = validator.validate(token(
                7L, -1L, AppRole.ROLE_GESTIONNAIRE_RH.name()));
        OAuth2TokenValidatorResult overflowingAccountId = validator.validate(token(
                BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE),
                0L,
                AppRole.ROLE_GESTIONNAIRE_RH.name()));

        assertThat(zeroAccountId.getErrors()).isNotEmpty();
        assertThat(negativeTokenVersion.getErrors()).isNotEmpty();
        assertThat(overflowingAccountId.getErrors()).isNotEmpty();
    }

    @Test
    void rejectsADeactivatedAccount() {
        Account account = account();
        account.setActivated(false);
        when(accountRepository.findById(7L)).thenReturn(Optional.of(account));

        OAuth2TokenValidatorResult result = validator.validate(token(
                AppRole.ROLE_GESTIONNAIRE_RH.name(), 0L));

        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    void rejectsAChangedRoleEvenAtTheSameTokenVersion() {
        Account account = account();
        account.setRole(AppRole.ROLE_ADMIN);
        when(accountRepository.findById(7L)).thenReturn(Optional.of(account));

        OAuth2TokenValidatorResult result = validator.validate(token(
                AppRole.ROLE_GESTIONNAIRE_RH.name(), 0L));

        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    void rejectsATokenInvalidatedByAnAccountChange() {
        Account account = account();
        account.incrementTokenVersion();
        when(accountRepository.findById(7L)).thenReturn(Optional.of(account));

        OAuth2TokenValidatorResult result = validator.validate(token(
                AppRole.ROLE_GESTIONNAIRE_RH.name(), 0L));

        assertThat(result.getErrors()).isNotEmpty();
    }
}
