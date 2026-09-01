package app.domain.admin.account;

import java.util.List;

import app.core.exception.ConflictException;
import app.core.exception.ResourceNotFoundException;
import app.domain.admin.role.Role;
import app.domain.admin.role.RoleRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository repository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(
            AccountRepository repository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AccountResponse creer(AccountCreateRequest request) {
        if (repository.existsByUsername(request.username())) {
            throw new ConflictException("Un compte porte déjà ce nom d'utilisateur");
        }
        Account account = new Account(
                request.username(),
                passwordEncoder.encode(request.password()),
                requireRole(request.role()));
        return AccountMapper.toResponse(repository.saveAndFlush(account));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> lister() {
        return repository.findAllByOrderByUsernameAsc().stream()
                .map(AccountMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse recupererParId(Long id) {
        return AccountMapper.toResponse(requireAccount(id));
    }

    @Transactional
    public AccountResponse maj(Long id, AccountUpdateRequest request) {
        Account account = requireAccount(id);
        account.update(requireRole(request.role()), request.activated());
        repository.flush();
        return AccountMapper.toResponse(account);
    }

    @Transactional
    public void reinitialiserMotDePasse(Long id, PasswordResetRequest request) {
        Account account = requireAccount(id);
        account.updatePassword(passwordEncoder.encode(request.password()));
        repository.flush();
    }

    private Account requireAccount(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compte", id));
    }

    private Role requireRole(String code) {
        return roleRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle", code));
    }
}
