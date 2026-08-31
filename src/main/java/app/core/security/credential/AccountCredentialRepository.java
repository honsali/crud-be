package app.core.security.credential;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface AccountCredentialRepository extends JpaRepository<AccountCredential, Long> {

    Optional<AccountCredential> findByAccountId(Long accountId);

    boolean existsByAccountId(Long accountId);

    @Query("""
            select new app.core.security.credential.LoginCredentialSnapshot(
                credential.accountId, credential.passwordHash, credential.tokenVersion)
            from AccountCredential credential
            where credential.accountId = :accountId
            """)
    Optional<LoginCredentialSnapshot> findLoginCredentialByAccountId(@Param("accountId") Long accountId);

    @Query("""
            select credential.tokenVersion
            from AccountCredential credential
            where credential.accountId = :accountId
            """)
    Optional<Long> findTokenVersionByAccountId(@Param("accountId") Long accountId);
}
