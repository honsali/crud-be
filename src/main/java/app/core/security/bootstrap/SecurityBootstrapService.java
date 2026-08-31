package app.core.security.bootstrap;

import app.core.exception.InvalidRequestException;
import app.core.security.account.SecurityAccountSnapshot;
import app.core.security.account.SecurityAdminBootstrapProvider;
import app.core.security.config.SecurityProperties;
import app.core.security.credential.CredentialService;
import app.core.security.credential.PasswordPolicy;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecurityBootstrapService {

    private final SecurityAdminBootstrapProvider bootstrapProvider;
    private final CredentialService credentialService;
    private final SecurityProperties properties;

    public SecurityBootstrapService(
            SecurityAdminBootstrapProvider bootstrapProvider,
            CredentialService credentialService,
            SecurityProperties properties) {
        this.bootstrapProvider = bootstrapProvider;
        this.credentialService = credentialService;
        this.properties = properties;
    }

    @Transactional
    public void bootstrapIfEnabled() {
        SecurityProperties.Bootstrap bootstrap = properties.getBootstrap();
        if (!bootstrap.isEnabled()) {
            return;
        }
        requireText(bootstrap.getUsername(), "APP_SECURITY_BOOTSTRAP_USERNAME");
        requireText(bootstrap.getDisplayName(), "APP_SECURITY_BOOTSTRAP_DISPLAY_NAME");
        requireText(bootstrap.getPassword(), "APP_SECURITY_BOOTSTRAP_PASSWORD");
        PasswordPolicy.requireValid(bootstrap.getPassword());
        if (bootstrapProvider.countAccounts() != 0) {
            throw new IllegalStateException(
                    "Le bootstrap est encore actif alors qu'un compte existe; retirez les variables APP_SECURITY_BOOTSTRAP_*");
        }
        SecurityAccountSnapshot account = bootstrapProvider.createInitialAdmin(
                bootstrap.getUsername(), bootstrap.getDisplayName());
        credentialService.createInitialCredential(account.accountId(), bootstrap.getPassword());
    }

    private void requireText(String value, String variableName) {
        if (value == null || value.isBlank()) {
            throw new InvalidRequestException(variableName + " est obligatoire lorsque le bootstrap est actif");
        }
    }
}
