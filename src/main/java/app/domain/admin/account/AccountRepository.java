package app.domain.admin.account;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findOneByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    List<Account> findAllByOrderByUsernameAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select account from Account account where account.role = :role order by account.id")
    List<Account> findAllByRoleForUpdate(@Param("role") AppRole role);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select account from Account account where account.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") Long id);
}
