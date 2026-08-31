package app.core.security.credential;

import java.time.Instant;

import app.core.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "account_credential")
public class AccountCredential extends BaseEntity {

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "token_version")
    private long tokenVersion;

    @Column(name = "password_changed_at")
    private Instant passwordChangedAt;

    protected AccountCredential() {
    }

    AccountCredential(Long accountId, String passwordHash, Instant passwordChangedAt) {
        this.accountId = accountId;
        this.passwordHash = passwordHash;
        this.passwordChangedAt = passwordChangedAt;
    }

    void replacePassword(String passwordHash, Instant changedAt) {
        this.passwordHash = passwordHash;
        this.passwordChangedAt = changedAt;
        this.tokenVersion++;
    }

    public void revokeAllTokens() {
        this.tokenVersion++;
    }

    public Long getAccountId() {
        return accountId;
    }

    String getPasswordHash() {
        return passwordHash;
    }

    public long getTokenVersion() {
        return tokenVersion;
    }

    public Instant getPasswordChangedAt() {
        return passwordChangedAt;
    }
}
