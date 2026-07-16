package app.core.security.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import app.core.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, passwordEncoder);
    }

    @Test
    void createsOneActivatedAccountWithAnEncodedPassword() {
        when(passwordEncoder.encode("secure-password")).thenReturn("encoded");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setId(101L);
            return account;
        });

        AccountDto result = accountService.create("  Manager  ", "secure-password", AppRole.ROLE_GESTIONNAIRE_RH);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        Account saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("manager");
        assertThat(saved.getPasswordHash()).isEqualTo("encoded");
        assertThat(saved.getRole()).isEqualTo(AppRole.ROLE_GESTIONNAIRE_RH);
        assertThat(saved.isActivated()).isTrue();
        assertThat(result).isEqualTo(new AccountDto(101L, "manager", AppRole.ROLE_GESTIONNAIRE_RH, true));
    }

    @Test
    void rejectsPasswordsThatBcryptCannotRepresentWithoutTruncation() {
        assertThatThrownBy(() -> accountService.create(
                "manager", "é".repeat(37), AppRole.ROLE_GESTIONNAIRE_RH))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must not exceed 72 UTF-8 bytes for BCrypt.");
    }

    @Test
    void preventsAnAdministratorFromChangingTheirOwnAccess() {
        Account administrator = account(1L, "admin", AppRole.ROLE_ADMIN, true);
        when(accountRepository.findAllByRoleForUpdate(AppRole.ROLE_ADMIN)).thenReturn(List.of(administrator));

        assertThatThrownBy(() -> accountService.update(
                1L, AppRole.ROLE_GESTIONNAIRE_RH, true, "admin"))
                .isInstanceOf(ConflictException.class)
                .hasMessage("An administrator cannot change or deactivate their own role.");
    }

    @Test
    void preservesTheLastActiveAdministrator() {
        Account administrator = account(1L, "admin", AppRole.ROLE_ADMIN, true);
        when(accountRepository.findAllByRoleForUpdate(AppRole.ROLE_ADMIN)).thenReturn(List.of(administrator));

        assertThatThrownBy(() -> accountService.update(
                1L, AppRole.ROLE_ADMIN, false, "other-admin"))
                .isInstanceOf(ConflictException.class)
                .hasMessage("The last active administrator cannot be changed or deactivated.");
    }

    @Test
    void changesAnotherAdministratorWhenOneActiveAdministratorRemains() {
        Account target = account(1L, "first-admin", AppRole.ROLE_ADMIN, true);
        Account remaining = account(2L, "second-admin", AppRole.ROLE_ADMIN, true);
        when(accountRepository.findAllByRoleForUpdate(AppRole.ROLE_ADMIN)).thenReturn(List.of(target, remaining));

        AccountDto result = accountService.update(
                1L, AppRole.ROLE_GESTIONNAIRE_RH, false, "second-admin");

        assertThat(result.role()).isEqualTo(AppRole.ROLE_GESTIONNAIRE_RH);
        assertThat(result.activated()).isFalse();
        assertThat(target.getTokenVersion()).isEqualTo(1);
    }

    @Test
    void resetsThePasswordAndInvalidatesExistingTokens() {
        Account target = account(3L, "manager", AppRole.ROLE_GESTIONNAIRE_RH, true);
        when(accountRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(target));
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        accountService.resetPassword(3L, "new-password");

        assertThat(target.getPasswordHash()).isEqualTo("new-hash");
        assertThat(target.getTokenVersion()).isEqualTo(1);
    }

    private static Account account(Long id, String username, AppRole role, boolean activated) {
        Account account = new Account();
        account.setId(id);
        account.setUsername(username);
        account.setPasswordHash("hash");
        account.setRole(role);
        account.setActivated(activated);
        return account;
    }
}
