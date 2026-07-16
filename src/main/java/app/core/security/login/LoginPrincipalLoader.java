package app.core.security.login;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.core.security.account.Account;
import app.core.security.account.AccountRepository;

@Service
public class LoginPrincipalLoader implements UserDetailsService {

    private final AccountRepository accountRepository;

    public LoginPrincipalLoader(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findOneByUsernameIgnoreCase(username).orElseThrow(() -> new UsernameNotFoundException("Account not found: " + username));

        return new LoginPrincipal(//
                account.getId(), //
                account.getUsername(), //
                account.getPasswordHash(), //
                account.getRole(), //
                account.getTokenVersion(), //
                account.isActivated());
    }
}
