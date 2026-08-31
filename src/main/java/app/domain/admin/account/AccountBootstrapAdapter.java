package app.domain.admin.account;

import app.core.exception.InvalidRequestException;

import app.core.security.account.SecurityAccountSnapshot;
import app.core.security.account.SecurityAdminBootstrapProvider;
import app.domain.admin.role.RoleCreateRequest;
import app.domain.admin.role.RoleRepository;
import app.domain.admin.role.RoleResponse;
import app.domain.admin.role.RoleService;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AccountBootstrapAdapter implements SecurityAdminBootstrapProvider {

    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final RoleRepository roleRepository;
    private final RoleService roleService;

    public AccountBootstrapAdapter(
            AccountRepository accountRepository,
            AccountService accountService,
            RoleRepository roleRepository,
            RoleService roleService) {
        this.accountRepository = accountRepository;
        this.accountService = accountService;
        this.roleRepository = roleRepository;
        this.roleService = roleService;
    }

    @Override
    @Transactional(readOnly = true)
    public long countAccounts() {
        return accountRepository.count();
    }

    @Override
    @Transactional
    public SecurityAccountSnapshot createInitialAdmin(String username, String displayName) {
        String canonicalUsername = Account.normalizeUsername(username);
        String canonicalDisplayName = Account.normalizeDisplayName(displayName);
        if (canonicalUsername == null || canonicalUsername.length() < 3 || canonicalUsername.length() > 100
                || !canonicalUsername.matches("[a-z0-9._-]+")) {
            throw new InvalidRequestException("Le username de bootstrap n'est pas valide");
        }
        if (canonicalDisplayName == null || canonicalDisplayName.isBlank()
                || canonicalDisplayName.length() > 150) {
            throw new InvalidRequestException("Le display name de bootstrap n'est pas valide");
        }
        RoleResponse role = roleRepository.findByCode("ADMIN")
                .map(value -> new RoleResponse(
                        value.getId(), value.getCode(), value.getLibelle(), value.getDescription(), value.getVersion()))
                .orElseGet(() -> roleService.create(new RoleCreateRequest(
                        "ADMIN", "Administrateur", "Administration de l'application")));
        AccountResponse account = accountService.create(new AccountCreateRequest(
                canonicalUsername, canonicalDisplayName, null, true, role.id()));
        return new SecurityAccountSnapshot(account.id(), account.username(), account.active(), role.code());
    }
}
