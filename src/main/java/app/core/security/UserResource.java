package app.core.security;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class UserResource {

    @GetMapping("/api/user")
    UserInfo currentUser(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new UserInfo(authentication.getName(), authentication.getName(), primaryRole(roles), roles, roles);
    }

    private static String primaryRole(List<String> roles) {
        return roles.stream()
                .filter("ROLE_ADMIN"::equals)
                .findFirst()
                .orElse(roles.isEmpty() ? null : roles.get(0));
    }

    record UserInfo(String username, String login, String role, List<String> roles, List<String> authorities) {
    }
}
