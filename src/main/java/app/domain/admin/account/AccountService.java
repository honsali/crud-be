package app.domain.admin.account;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.core.exception.ConflictException;

@Service
public class AccountService {

    private record LockedAccount(Account account, List<Account> administrators) {
    }

    private static long countActiveAdministrators(List<Account> administrators) {
        return administrators.stream().filter(Account::isActivated).count();
    }

    private static String normalizeUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        return username.trim();
    }

    private static void validatePassword(String password) {
        if (password == null || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password must not exceed 72 UTF-8 bytes for BCrypt.");
        }
    }

    private final AccountRepository accountRepository;

    private final PasswordEncoder passwordEncoder;

    public AccountService(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AccountDto> list() {
        return accountRepository.findAllByOrderByUsernameAsc().stream().map(AccountDto::from).toList();
    }

    @Transactional(readOnly = true)
    public AccountDto get(Long id) {
        return accountRepository.findById(id)
                .map(AccountDto::from)
                .orElseThrow(() -> new NoSuchElementException("Account not found: " + id));
    }

    @Transactional
    public AccountDto create(String username, String password, AppRole role) {
        String normalizedUsername = normalizeUsername(username);
        validatePassword(password);
        if (accountRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new ConflictException("An account already uses this username.");
        }

        Account account = new Account();
        account.setUsername(normalizedUsername);
        account.setPasswordHash(passwordEncoder.encode(password));
        account.setRole(role);
        account.setActivated(true);
        return AccountDto.from(accountRepository.save(account));
    }

    @Transactional
    public AccountDto update(Long id, AppRole role, boolean activated, String currentUsername) {
        LockedAccount locked = lockAccount(id);
        Account account = locked.account();

        boolean changesOwnAccess = account.getUsername().equalsIgnoreCase(currentUsername) && (account.getRole() != role || !activated);
        if (changesOwnAccess) {
            throw new ConflictException("An administrator cannot change or deactivate their own role.");
        }

        boolean removesActiveAdministrator = account.getRole() == AppRole.ROLE_ADMIN && account.isActivated() && (role != AppRole.ROLE_ADMIN || !activated);
        if (removesActiveAdministrator && countActiveAdministrators(locked.administrators()) <= 1) {
            throw new ConflictException("The last active administrator cannot be changed or deactivated.");
        }

        boolean securityChanged = account.getRole() != role || account.isActivated() != activated;
        account.setRole(role);
        account.setActivated(activated);
        if (securityChanged) {
            account.incrementTokenVersion();
        }
        return AccountDto.from(account);
    }

    @Transactional
    public void resetPassword(Long id, String password) {
        validatePassword(password);
        String passwordHash = passwordEncoder.encode(password);
        Account account = accountRepository.findByIdForUpdate(id).orElseThrow(() -> new NoSuchElementException("Account not found: " + id));
        account.setPasswordHash(passwordHash);
        account.incrementTokenVersion();
    }

    private LockedAccount lockAccount(Long id) {
        List<Account> administrators = accountRepository.findAllByRoleForUpdate(AppRole.ROLE_ADMIN);
        Account account = administrators.stream().filter(candidate -> candidate.getId().equals(id)).findFirst().orElseGet(() -> accountRepository.findByIdForUpdate(id).orElseThrow(() -> new NoSuchElementException("Account not found: " + id)));
        return new LockedAccount(account, administrators);
    }
}
