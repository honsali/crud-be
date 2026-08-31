package app.domain.admin.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import app.core.exception.ConflictException;
import app.domain.admin.role.Role;
import app.domain.admin.role.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountServiceSelfLockoutTest {

    private static final Long ACCOUNT_ID = 7L;
    private static final Long ADMIN_ROLE_ID = 10L;

    @Mock AccountRepository accountRepository;
    @Mock RoleRepository roleRepository;
    @Mock Account account;
    @Mock Role adminRole;
    private AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService(accountRepository, roleRepository);
        lenient().when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        lenient().when(account.getId()).thenReturn(ACCOUNT_ID);
        lenient().when(account.getVersion()).thenReturn(4L);
        lenient().when(account.getRole()).thenReturn(adminRole);
        lenient().when(adminRole.getId()).thenReturn(ADMIN_ROLE_ID);
    }

    @Test
    void refusesSelfDeletionButAllowsAnotherAdministratorToDelete() {
        assertThatThrownBy(() -> service.delete(ACCOUNT_ID, ACCOUNT_ID))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Un administrateur ne peut pas supprimer son propre compte");
        verify(accountRepository, never()).delete(account);

        service.delete(ACCOUNT_ID, 99L);
        verify(accountRepository).delete(account);
        verify(accountRepository).flush();
    }

    @Test
    void refusesSelfDeactivation() {
        AccountUpdateRequest request = request(false, ADMIN_ROLE_ID);

        assertThatThrownBy(() -> service.update(ACCOUNT_ID, request, ACCOUNT_ID))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Un administrateur ne peut pas désactiver son propre compte");
        verify(account, never()).update(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyBoolean(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void refusesChangingItsOwnRole() {
        AccountUpdateRequest request = request(true, 11L);

        assertThatThrownBy(() -> service.update(ACCOUNT_ID, request, ACCOUNT_ID))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Un administrateur ne peut pas modifier son propre rôle");
        verify(roleRepository, never()).findById(11L);
    }

    @Test
    void allowsEditingItsOwnProfileWhileRemainingActiveWithTheSameRole() {
        AccountUpdateRequest request = new AccountUpdateRequest(
                "renamed.admin", "Nom renommé", "Mixed.Case@Example.test", true, ADMIN_ROLE_ID, 4L);
        when(roleRepository.findById(ADMIN_ROLE_ID)).thenReturn(Optional.of(adminRole));
        when(account.getUsername()).thenReturn("renamed.admin");
        when(account.getDisplayName()).thenReturn("Nom renommé");
        when(account.getEmail()).thenReturn("mixed.case@example.test");
        when(account.isActive()).thenReturn(true);

        AccountResponse response = service.update(ACCOUNT_ID, request, ACCOUNT_ID);

        verify(account).update(
                "renamed.admin", "Nom renommé", "mixed.case@example.test", true, adminRole);
        verify(accountRepository).flush();
        assertThat(response.username()).isEqualTo("renamed.admin");
        assertThat(response.active()).isTrue();
    }

    private AccountUpdateRequest request(boolean active, Long roleId) {
        return new AccountUpdateRequest(
                "admin.user", "Administrateur", null, active, roleId, 4L);
    }
}
