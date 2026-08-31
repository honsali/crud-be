package app.domain.admin.role;

public final class RoleMapper {

    private RoleMapper() {
    }

    public static RoleResponse toResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getCode(),
                role.getLibelle(),
                role.getDescription(),
                role.getVersion());
    }

    public static RoleReference toReference(Role role) {
        return new RoleReference(role.getId(), role.getCode(), role.getLibelle());
    }
}
