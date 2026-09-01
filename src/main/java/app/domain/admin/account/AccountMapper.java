package app.domain.admin.account;

import app.domain.admin.role.RoleMapper;

public final class AccountMapper {

    private AccountMapper() {
    }

    public static AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getUsername(),
                RoleMapper.toReference(account.getRole()),
                account.isActivated());
    }
}
