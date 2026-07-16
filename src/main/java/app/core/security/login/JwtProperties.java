package app.core.security.login;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties("application.security")
public record JwtProperties(@NotBlank String issuer, @NotBlank String audience, @Min(1) @Max(604800) long tokenValiditySeconds) {
}
