package app.core.security.account;

public interface SecurityAdminBootstrapProvider {

    long countAccounts();

    SecurityAccountSnapshot createInitialAdmin(String username, String displayName);
}
