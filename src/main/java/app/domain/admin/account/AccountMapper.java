package app.domain.admin.account;

import app.domain.admin.role.Role;
import app.domain.admin.role.RoleMapper;

public final class AccountMapper {

    public static AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getUsername(),
                account.getRole() == null
                        ? null
                        : RoleMapper.toReference(account.getRole()),
                account.getActivated(),
                account.getVersion());
    }

    public static Account toEntity(String username, Role role, String passwordHash) {
        return new Account(
                username,
                role,
                passwordHash);
    }

    public static void toEntity(Account account, AccountUpdateRequest request, Role role) {
        account.update(
                role,
                request.activated());
    }

    private AccountMapper() {}
}
