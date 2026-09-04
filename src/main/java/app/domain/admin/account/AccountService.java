package app.domain.admin.account;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.core.exception.ConflictException;
import app.core.exception.ResourceNotFoundException;
import app.core.exception.StaleVersionException;
import app.core.reference.Reference;
import app.domain.admin.role.Role;
import app.domain.admin.role.RoleRepository;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(AccountRepository accountRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AccountResponse creer(AccountCreateRequest request) {
        if (accountRepository.existsByUsername(request.username())) {
            throw new ConflictException("Un compte porte déjà ce nom d'utilisateur");
        }

        Role role = recupererRole(request.role());
        Account account = AccountMapper.toEntity(request, role, passwordEncoder.encode(request.password()));
        Account saved = accountRepository.saveAndFlush(account);
        return AccountMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> lister() {
        return accountRepository.findAllByOrderByUsername().stream().map(AccountMapper::toResponse).toList();
    }

    @Transactional
    public AccountResponse maj(Long id, AccountUpdateRequest request) {
        Account account = recupererAccount(id);
        if (account.getVersion() != request.version()) {
            throw new StaleVersionException("Compte", id);
        }

        Role role = recupererRole(request.role());
        AccountMapper.toEntity(account, request, role);
        accountRepository.flush();
        return AccountMapper.toResponse(account);
    }

    @Transactional(readOnly = true)
    public AccountResponse recupererParId(Long id) {
        Account account = recupererAccount(id);
        return AccountMapper.toResponse(account);
    }

    @Transactional
    public void reinitialiserMotDePasse(Long id, PasswordResetRequest request) {
        Account account = recupererAccount(id);
        account.updatePassword(passwordEncoder.encode(request.password()));
        accountRepository.flush();
    }

    private Account recupererAccount(Long id) {
        return accountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Account", id));
    }

    private Role recupererRole(Reference reference) {
        if (reference == null) {
            return null;
        }
        Long id = reference.id();
        return roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role", id));
    }
}
