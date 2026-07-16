package app.core.security.account;

public record AccountDto(Long id, String username, AppRole role, boolean activated) {

    static AccountDto from(Account account) {
        return new AccountDto(account.getId(), account.getUsername(), account.getRole(), account.isActivated());
    }
}
