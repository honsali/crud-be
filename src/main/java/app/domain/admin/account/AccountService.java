package app.domain.admin.account;

import app.core.exception.ConflictException;
import app.core.exception.InvalidRequestException;
import app.core.exception.ResourceNotFoundException;
import app.core.exception.StaleVersionException;
import app.core.pagination.SortDirection;
import app.domain.admin.role.Role;
import app.domain.admin.role.RoleRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository repository;
    private final RoleRepository roleRepository;

    public AccountService(AccountRepository repository, RoleRepository roleRepository) {
        this.repository = repository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public AccountResponse create(AccountCreateRequest request) {
        ensureUnique(request.username(), request.email(), null);
        Role role = requireRole(request.roleId());
        Account account = new Account(
                request.username(), request.displayName(), request.email(), request.active(), role);
        return AccountMapper.toResponse(repository.saveAndFlush(account));
    }

    @Transactional(readOnly = true)
    public AccountResponse get(Long id) {
        return AccountMapper.toResponse(require(id));
    }

    @Transactional(readOnly = true)
    public Page<AccountResponse> search(AccountSearchCriteria criteria) {
        validatePagination(criteria);
        Sort.Direction direction = criteria.direction() == SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Sort sort = Sort.by(
                new Sort.Order(direction, criteria.sort().property()),
                new Sort.Order(Sort.Direction.ASC, "id"));
        PageRequest pageRequest = PageRequest.of(criteria.page(), criteria.size(), sort);
        return repository.findAll(AccountSpecification.from(criteria), pageRequest)
                .map(AccountMapper::toResponse);
    }

    @Transactional
    public AccountResponse update(Long id, AccountUpdateRequest request, Long actorAccountId) {
        Account account = require(id);
        if (account.getVersion() != request.version()) {
            throw new StaleVersionException("Le compte", id);
        }
        preventSelfLockout(account, request, actorAccountId);
        ensureUnique(request.username(), request.email(), id);
        Role role = requireRole(request.roleId());
        account.update(request.username(), request.displayName(), request.email(), request.active(), role);
        repository.flush();
        return AccountMapper.toResponse(account);
    }

    @Transactional
    public void delete(Long id, Long actorAccountId) {
        Account account = require(id);
        if (id.equals(actorAccountId)) {
            throw new ConflictException("Un administrateur ne peut pas supprimer son propre compte");
        }
        repository.delete(account);
        repository.flush();
    }

    private void preventSelfLockout(Account account, AccountUpdateRequest request, Long actorAccountId) {
        if (!account.getId().equals(actorAccountId)) {
            return;
        }
        if (!request.active()) {
            throw new ConflictException("Un administrateur ne peut pas désactiver son propre compte");
        }
        if (!account.getRole().getId().equals(request.roleId())) {
            throw new ConflictException("Un administrateur ne peut pas modifier son propre rôle");
        }
    }

    private void validatePagination(AccountSearchCriteria criteria) {
        if (criteria.page() < 0 || criteria.size() < 1 || criteria.size() > 100) {
            throw new InvalidRequestException("La page doit être positive et sa taille comprise entre 1 et 100");
        }
        if (criteria.sort() == null || criteria.direction() == null) {
            throw new InvalidRequestException("Le tri est obligatoire");
        }
    }

    private void ensureUnique(String username, String email, Long excludedId) {
        boolean usernameExists = excludedId == null
                ? repository.existsByUsername(username)
                : repository.existsByUsernameAndIdNot(username, excludedId);
        if (usernameExists) {
            throw new ConflictException("Un compte porte déjà ce nom d'utilisateur");
        }
        if (email != null) {
            boolean emailExists = excludedId == null
                    ? repository.existsByEmail(email)
                    : repository.existsByEmailAndIdNot(email, excludedId);
            if (emailExists) {
                throw new ConflictException("Un compte porte déjà cet e-mail");
            }
        }
    }

    private Account require(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compte", id));
    }

    private Role requireRole(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle", id));
    }
}
