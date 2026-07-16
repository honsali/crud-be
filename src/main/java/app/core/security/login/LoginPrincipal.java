package app.core.security.login;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import app.core.security.account.AppRole;

record LoginPrincipal(//
        Long id, //
        String username, //
        String passwordHash, //
        AppRole role, //
        long tokenVersion, //
        boolean activated) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return activated;
    }

    @Override
    public String toString() {
        return "LoginPrincipal[id=" + id + ", username=" + username + ", role=" + role + ", tokenVersion=" + tokenVersion + ", activated=" + activated + "]";
    }
}
