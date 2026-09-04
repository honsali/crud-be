package app.domain.admin.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import app.core.exception.StaleVersionException;
import app.core.reference.Reference;
import app.domain.admin.role.Role;
import app.domain.admin.role.RoleRepository;

class AccountServiceTest {

    @Test
    void normaliseLeUsernameAvantLeControleEtLaPersistance() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        RoleRepository roleRepository = mock(RoleRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        Role role = mock(Role.class);
        AccountService service = new AccountService(accountRepository, roleRepository, passwordEncoder);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("password-123")).thenReturn("password-hash");
        when(accountRepository.saveAndFlush(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.creer(new AccountCreateRequest(
                " Alice.Admin ",
                "password-123",
                new Reference(1L, null)));

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).existsByUsername("alice.admin");
        verify(accountRepository).saveAndFlush(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getUsername()).isEqualTo("alice.admin");
        assertThat(accountCaptor.getValue().getRole()).isSameAs(role);
        assertThat(accountCaptor.getValue().getActivated()).isTrue();
        assertThat(accountCaptor.getValue().getPasswordHash()).isEqualTo("password-hash");
    }

    @Test
    void refuseUneVersionPerimee() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        RoleRepository roleRepository = mock(RoleRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        Account account = new Account("alice", mock(Role.class), "password-hash");
        AccountService service = new AccountService(accountRepository, roleRepository, passwordEncoder);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        AccountUpdateRequest request = new AccountUpdateRequest(new Reference(1L, null), true, 1L);

        assertThatThrownBy(() -> service.maj(1L, request))
                .isInstanceOf(StaleVersionException.class)
                .hasMessage("Account 1 a été modifié depuis sa lecture");
        verifyNoInteractions(roleRepository);
    }
}
