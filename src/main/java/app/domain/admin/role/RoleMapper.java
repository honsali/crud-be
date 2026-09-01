package app.domain.admin.role;

public final class RoleMapper {

    private RoleMapper() {
    }

    public static RoleReference toReference(Role role) {
        return new RoleReference(role.getId(), role.getAuthority(), role.getLibelle());
    }
}
