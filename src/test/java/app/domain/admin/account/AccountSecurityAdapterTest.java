package app.domain.admin.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import app.core.security.account.SecurityAccountSnapshot;
import app.domain.admin.role.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.jpa.repository.EntityGraph;

@ExtendWith(MockitoExtension.class)
class AccountSecurityAdapterTest {

    @Mock
    private AccountRepository repository;

    private AccountSecurityAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AccountSecurityAdapter(repository);
    }

    @Test
    void canonicalizesRawUsernameAndReturnsAnActiveSnapshot() {
        Account account = account(7L, "alice.admin", true, "ADMIN");
        when(repository.findByUsername("alice.admin")).thenReturn(Optional.of(account));

        assertThat(adapter.findByUsername(" Alice.Admin "))
                .contains(new SecurityAccountSnapshot(7L, "alice.admin", true, "ADMIN"));
        verify(repository).findByUsername("alice.admin");
    }

    @Test
    void returnsInactiveAccountsWithoutFilteringThem() {
        Account account = account(8L, "inactive.user", false, "USER");
        when(repository.findByUsername("inactive.user")).thenReturn(Optional.of(account));

        assertThat(adapter.findByUsername("INACTIVE.USER"))
                .contains(new SecurityAccountSnapshot(8L, "inactive.user", false, "USER"));
    }

    @Test
    void returnsEmptyForUnknownOrBlankUsernames() {
        when(repository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThat(adapter.findByUsername("unknown")).isEmpty();
        assertThat(adapter.findByUsername("   ")).isEmpty();
        verify(repository).findByUsername("unknown");
    }

    @Test
    void findsTheSnapshotByStableIdentifier() {
        Account account = account(9L, "by-id", true, "SUPPORT");
        when(repository.findById(9L)).thenReturn(Optional.of(account));

        assertThat(adapter.findById(9L))
                .contains(new SecurityAccountSnapshot(9L, "by-id", true, "SUPPORT"));
        assertThat(adapter.findById(null)).isEmpty();
    }

    @Test
    void repositoryUsernameLookupLoadsTheRoleWithAnEntityGraph() throws Exception {
        EntityGraph entityGraph = AccountRepository.class
                .getMethod("findByUsername", String.class)
                .getAnnotation(EntityGraph.class);

        assertThat(entityGraph).isNotNull();
        assertThat(entityGraph.attributePaths()).containsExactly("role");
    }

    private Account account(Long id, String username, boolean active, String roleCode) {
        Role role = mock(Role.class);
        when(role.getCode()).thenReturn(roleCode);
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(id);
        when(account.getUsername()).thenReturn(username);
        when(account.isActive()).thenReturn(active);
        when(account.getRole()).thenReturn(role);
        return account;
    }
}
