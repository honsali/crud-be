package app.domain.admin.role;

import java.util.List;

import app.core.exception.ConflictException;
import app.core.exception.ResourceNotFoundException;
import app.core.exception.StaleVersionException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {

    private final RoleRepository repository;

    public RoleService(RoleRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public RoleResponse create(RoleCreateRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException("Un rôle porte déjà ce code");
        }
        Role role = repository.saveAndFlush(new Role(request.code(), request.libelle(), request.description()));
        return RoleMapper.toResponse(role);
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> list() {
        return repository.findAllByOrderByCodeAscIdAsc().stream()
                .map(RoleMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoleResponse get(Long id) {
        return RoleMapper.toResponse(require(id));
    }

    @Transactional
    public RoleResponse update(Long id, RoleUpdateRequest request) {
        Role role = require(id);
        if (role.getVersion() != request.version()) {
            throw new StaleVersionException("Le rôle", id);
        }
        role.update(request.libelle(), request.description());
        repository.flush();
        return RoleMapper.toResponse(role);
    }

    @Transactional
    public void delete(Long id) {
        Role role = require(id);
        repository.delete(role);
        repository.flush();
    }

    Role require(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle", id));
    }
}
