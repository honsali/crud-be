package app.core.security.jwt;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class LiveAccountAuthenticationToken extends JwtAuthenticationToken {

    private final Long accountId;
    private final String username;
    private final String roleCode;

    public LiveAccountAuthenticationToken(
            Jwt jwt,
            Collection<? extends GrantedAuthority> authorities,
            Long accountId,
            String username,
            String roleCode) {
        super(jwt, authorities, username);
        this.accountId = accountId;
        this.username = username;
        this.roleCode = roleCode;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getUsername() {
        return username;
    }

    public String getRoleCode() {
        return roleCode;
    }
}
