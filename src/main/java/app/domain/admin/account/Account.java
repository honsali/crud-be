package app.domain.admin.account;

import java.util.Locale;

import app.core.persistence.BaseEntity;
import app.domain.admin.role.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "account")
public class Account extends BaseEntity {

    private String username;

    @Column(name = "password_hash")
    private String passwordHash;

    private boolean activated;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id")
    private Role role;

    protected Account() {
    }

    Account(String username, String passwordHash, Role role) {
        this.username = normalizeUsername(username);
        this.passwordHash = passwordHash;
        this.activated = true;
        this.role = role;
    }

    void update(Role role, boolean activated) {
        this.role = role;
        this.activated = activated;
    }

    void updatePassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    static String normalizeUsername(String value) {
        return value == null ? null : value.strip().toLowerCase(Locale.ROOT);
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isActivated() {
        return activated;
    }

    public Role getRole() {
        return role;
    }
}
