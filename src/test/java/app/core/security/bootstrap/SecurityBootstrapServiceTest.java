package app.core.security.bootstrap;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.core.exception.InvalidRequestException;
import app.core.security.account.SecurityAccountSnapshot;
import app.core.security.account.SecurityAdminBootstrapProvider;
import app.core.security.config.SecurityProperties;
import app.core.security.credential.CredentialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SecurityBootstrapServiceTest {

    @Mock SecurityAdminBootstrapProvider provider;
    @Mock CredentialService credentialService;
    private SecurityProperties properties;
    private SecurityBootstrapService service;

    @BeforeEach
    void setUp() {
        properties = new SecurityProperties();
        service = new SecurityBootstrapService(provider, credentialService, properties);
    }

    @Test
    void doesAbsolutelyNothingWhileDisabled() {
        service.bootstrapIfEnabled();
        verify(provider, never()).countAccounts();
        verify(credentialService, never()).createInitialCredential(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createsOneActiveAdminAndItsCredential() {
        enableValidBootstrap();
        when(provider.countAccounts()).thenReturn(0L);
        when(provider.createInitialAdmin(" First.Admin ", "Premier admin"))
                .thenReturn(new SecurityAccountSnapshot(3L, "first.admin", true, "ADMIN"));

        service.bootstrapIfEnabled();

        verify(provider).createInitialAdmin(" First.Admin ", "Premier admin");
        verify(credentialService).createInitialCredential(3L, " mot de passe initial sûr ");
    }

    @Test
    void refusesASecondExecutionAndNeverResetsExistingData() {
        enableValidBootstrap();
        when(provider.countAccounts()).thenReturn(1L);

        assertThatThrownBy(service::bootstrapIfEnabled)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("retirez les variables APP_SECURITY_BOOTSTRAP_*");
        verify(provider, never()).createInitialAdmin(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(credentialService, never()).createInitialCredential(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void validatesAllParametersOnlyWhenEnabled() {
        properties.getBootstrap().setEnabled(true);
        assertThatThrownBy(service::bootstrapIfEnabled).isInstanceOf(InvalidRequestException.class);
        verify(provider, never()).countAccounts();
    }

    private void enableValidBootstrap() {
        properties.getBootstrap().setEnabled(true);
        properties.getBootstrap().setUsername(" First.Admin ");
        properties.getBootstrap().setDisplayName("Premier admin");
        properties.getBootstrap().setPassword(" mot de passe initial sûr ");
    }
}
