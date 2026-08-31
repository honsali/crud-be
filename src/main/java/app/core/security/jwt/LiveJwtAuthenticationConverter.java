package app.core.security.jwt;

import java.util.List;

import app.core.security.account.SecurityAccountProvider;
import app.core.security.account.SecurityAccountSnapshot;
import app.core.security.credential.CredentialService;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;

public class LiveJwtAuthenticationConverter
        implements Converter<Jwt, AbstractOAuth2TokenAuthenticationToken<Jwt>> {

    private final SecurityAccountProvider accountProvider;
    private final CredentialService credentialService;

    public LiveJwtAuthenticationConverter(
            SecurityAccountProvider accountProvider,
            CredentialService credentialService) {
        this.accountProvider = accountProvider;
        this.credentialService = credentialService;
    }

    @Override
    public AbstractOAuth2TokenAuthenticationToken<Jwt> convert(Jwt jwt) {
        Long accountId = parseAccountId(jwt.getSubject());
        long claimedCredentialVersion = parseCredentialVersion(
                jwt.getClaim(JwtTokenService.CREDENTIAL_VERSION_CLAIM));
        SecurityAccountSnapshot account = accountProvider.findById(accountId)
                .filter(SecurityAccountSnapshot::active)
                .orElseThrow(LiveJwtAuthenticationConverter::invalidToken);
        long currentCredentialVersion = credentialService.findTokenVersion(accountId)
                .orElseThrow(LiveJwtAuthenticationConverter::invalidToken);
        if (currentCredentialVersion != claimedCredentialVersion) {
            throw invalidToken();
        }
        return new LiveAccountAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority("ROLE_" + account.roleCode())),
                account.accountId(),
                account.username(),
                account.roleCode());
    }

    private Long parseAccountId(String subject) {
        try {
            long accountId = Long.parseLong(subject);
            if (accountId <= 0) {
                throw invalidToken();
            }
            return accountId;
        } catch (NumberFormatException exception) {
            throw invalidToken();
        }
    }

    private long parseCredentialVersion(Object claim) {
        if (!(claim instanceof Number number)) {
            throw invalidToken();
        }
        long value = number.longValue();
        if (value < 0 || number.doubleValue() != (double) value) {
            throw invalidToken();
        }
        return value;
    }

    private static OAuth2AuthenticationException invalidToken() {
        return new OAuth2AuthenticationException(new OAuth2Error("invalid_token"));
    }
}
