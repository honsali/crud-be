package app.domain.admin.account;

import java.util.Optional;

import app.core.security.account.SecurityAccountProvider;
import app.core.security.account.SecurityAccountSnapshot;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AccountSecurityAdapter implements SecurityAccountProvider {

    private final AccountRepository repository;

    public AccountSecurityAdapter(AccountRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SecurityAccountSnapshot> findByUsername(String username) {
        String canonicalUsername = Account.normalizeUsername(username);
        if (canonicalUsername == null || canonicalUsername.isBlank()) {
            return Optional.empty();
        }
        return repository.findByUsername(canonicalUsername).map(AccountSecurityAdapter::toSnapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SecurityAccountSnapshot> findById(Long accountId) {
        if (accountId == null) {
            return Optional.empty();
        }
        return repository.findById(accountId).map(AccountSecurityAdapter::toSnapshot);
    }

    private static SecurityAccountSnapshot toSnapshot(Account account) {
        return new SecurityAccountSnapshot(
                account.getId(),
                account.getUsername(),
                account.isActive(),
                account.getRole().getCode());
    }
}
