package app.core.security.account;

import java.util.Optional;

public interface SecurityAccountProvider {

    Optional<SecurityAccountSnapshot> findByUsername(String username);

    Optional<SecurityAccountSnapshot> findById(Long accountId);
}
