package app.core.security;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findOneByUsernameIgnoreCase(String username);
}
