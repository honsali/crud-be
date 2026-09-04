package app.domain.admin.account;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import app.core.exception.StaleVersionException;
import app.core.reference.Reference;
import app.domain.admin.role.Role;
import app.domain.admin.role.RoleRepository;

class AccountServiceTest {

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
                .hasMessage("Compte 1 a été modifié depuis sa lecture");
        verifyNoInteractions(roleRepository);
    }
}
