package app.domain.admin.account;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, Long id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    @EntityGraph(attributePaths = "role")
    Optional<Account> findByUsername(String username);

    @Override
    @EntityGraph(attributePaths = "role")
    Optional<Account> findById(Long id);

    @Override
    @EntityGraph(attributePaths = "role")
    Page<Account> findAll(Specification<Account> specification, Pageable pageable);
}
