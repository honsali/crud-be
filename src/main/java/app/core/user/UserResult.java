package app.core.user;

import app.core.validation.Assert;
import java.util.Set;

public class UserResult {

    private final String login;
    private final Set<String> authorities;

    public UserResult(String login, Set<String> authorities) {
        Assert.notBlank("login", login);
        this.login = login;
        this.authorities = authorities;
    }

    public String getLogin() {
        return login;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }
}
