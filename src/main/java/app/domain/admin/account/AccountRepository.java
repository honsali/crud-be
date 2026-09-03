package app.domain.admin.account;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByUsername(String username);

    @EntityGraph(attributePaths = { "role" })
    List<Account> findAllByOrderByUsername();

    @Override
    @EntityGraph(attributePaths = {"role"})
    Optional<Account> findById(Long id);

    @EntityGraph(attributePaths = "role")
    Optional<Account> findByUsername(String username);
}
