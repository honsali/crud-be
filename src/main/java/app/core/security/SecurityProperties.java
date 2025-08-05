package app.core.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties("application.security")
public class SecurityProperties {

    private String contentSecurityPolicy;
    private String permissionPolicy;

    private String jwtBase64Secret;
    private Duration tokenValidity;
    private Duration rememberMeTokenValidity;

    @NotBlank
    public String getContentSecurityPolicy() {
        return contentSecurityPolicy;
    }

    public void setContentSecurityPolicy(String contentSecurityPolicy) {
        this.contentSecurityPolicy = contentSecurityPolicy;
    }

    @NotBlank
    public String getJwtBase64Secret() {
        return jwtBase64Secret;
    }

    public void setJwtBase64Secret(String jwtBase64Secret) {
        this.jwtBase64Secret = jwtBase64Secret;
    }

    @NotNull
    public Duration getTokenValidity() {
        return tokenValidity;
    }

    public void setTokenValidity(Duration tokenValidity) {
        this.tokenValidity = tokenValidity;
    }

    @NotNull
    public Duration getRememberMeTokenValidity() {
        return rememberMeTokenValidity;
    }

    public void setRememberMeTokenValidity(Duration rememberMeTokenValidity) {
        this.rememberMeTokenValidity = rememberMeTokenValidity;
    }

    public String getPermissionPolicy() {
        return this.permissionPolicy;
    }

    public void setPermissionPolicy(String permissionPolicy) {
        this.permissionPolicy = permissionPolicy;
    }
}
