package app.domain.admin.account;

import app.core.configuration.JsonId;

public record AccountDto(
        @JsonId Long id,
        String username,
        AppRole role,
        boolean activated) {

    static AccountDto from(Account account) {
        return new AccountDto(account.getId(), account.getUsername(), account.getRole(), account.isActivated());
    }
}
