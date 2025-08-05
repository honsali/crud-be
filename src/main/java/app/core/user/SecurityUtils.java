package app.core.user;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import app.core.validation.Assert;
import io.micrometer.common.util.StringUtils;

public final class SecurityUtils {

    public static String currentUserName() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Assert.notNull("authentication", authentication);

        String userName = null;
        if (authentication.getPrincipal() instanceof UserDetails details) {
            userName = details.getUsername();
        } else if (authentication.getPrincipal() instanceof String principal) {
            userName = principal;
        } else {
            throw new Exception("unknown authentication");

        }

        if (StringUtils.isBlank(userName)) {
            throw new Exception("not authenticated");
        }

        return userName;

    }

    public static Set<String> currentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Set<String> authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        return authorities;
    }

    private SecurityUtils() {
    }
}
