package app.domain.admin.role;

import java.util.NoSuchElementException;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {

    private final RoleRepository roleRepository;

    public RoleMapper(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public RoleDto toDtoAsRef(Role entity) {
        return entity == null ? null
                : new RoleDto(
                        entity.getId(),
                        entity.getId(),
                        entity.getLibelle());
    }

    public Role toEntityAsRef(RoleDto dto) {
        if (dto == null || dto.id() == null) {
            return null;
        }
        return roleRepository.findById(dto.id()).orElseThrow(() -> new NoSuchElementException("Role not found"));
    }
}
