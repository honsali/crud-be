package app.domain.admin.account;

import java.util.Locale;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import app.core.persistence.BaseEntity;
import app.domain.admin.role.Role;

@Entity
@Table(name = "account")
public class Account extends BaseEntity {

    private String username;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    private Boolean activated;
    @Column(name = "password_hash")
    private String passwordHash;

    protected Account() {}

    Account(String username, Role role, String passwordHash) {
        this.username = normalizeUsername(username);
        this.role = role;
        this.activated = true;
        this.passwordHash = passwordHash;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public Boolean getActivated() {
        return activated;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    void update(Role role, Boolean activated) {
        this.role = role;
        this.activated = activated;
    }

    void updatePassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    static String normalizeUsername(String value) {
        return value == null ? null : value.strip().toLowerCase(Locale.ROOT);
    }
}
