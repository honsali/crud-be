package app.domain.admin.account;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByUsername(String username);

    @EntityGraph(attributePaths = "role")
    Optional<Account> findByUsername(String username);

    @Override
    @EntityGraph(attributePaths = "role")
    Optional<Account> findById(Long id);

    @EntityGraph(attributePaths = "role")
    List<Account> findAllByOrderByUsernameAsc();
}
