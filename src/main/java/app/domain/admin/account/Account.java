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

    @Column(name = "display_name")
    private String displayName;

    private String email;

    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id")
    private Role role;

    protected Account() {
    }

    Account(String username, String displayName, String email, boolean active, Role role) {
        this.username = normalizeUsername(username);
        this.displayName = normalizeDisplayName(displayName);
        this.email = normalizeEmail(email);
        this.active = active;
        this.role = role;
    }

    void update(String username, String displayName, String email, boolean active, Role role) {
        this.username = normalizeUsername(username);
        this.displayName = normalizeDisplayName(displayName);
        this.email = normalizeEmail(email);
        this.active = active;
        this.role = role;
    }

    static String normalizeUsername(String value) {
        return value == null ? null : value.strip().toLowerCase(Locale.ROOT);
    }

    static String normalizeDisplayName(String value) {
        return value == null ? null : value.strip();
    }

    static String normalizeEmail(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.strip().toLowerCase(Locale.ROOT);
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return active;
    }

    public Role getRole() {
        return role;
    }
}
