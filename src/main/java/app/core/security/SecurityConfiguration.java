package app.core.security;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityProperties.class)
class SecurityConfiguration {

    static final String AUTHORITIES_CLAIM = "auth";
    static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfiguration.class);

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/authenticate").permitAll()
                        .requestMatchers("/api/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    @Bean
    UrlBasedCorsConfigurationSource corsConfigurationSource(@Value("${application.cors.allowed-origins}") String allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(splitCsv(allowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization", "Link", "X-Total-Count"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(1800L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecretKey jwtSecretKey(SecurityProperties properties, Environment environment) {
        String encodedSecret = properties.jwtBase64Secret();
        if (encodedSecret != null && !encodedSecret.isBlank()) {
            byte[] secret;
            try {
                secret = Base64.getDecoder().decode(encodedSecret);
            } catch (IllegalArgumentException exception) {
                throw new IllegalStateException("JWT secret must be valid Base64.", exception);
            }
            if (secret.length < 64) {
                throw new IllegalStateException("JWT secret must decode to at least 64 bytes for HS512.");
            }
            return new SecretKeySpec(secret, "HmacSHA512");
        }
        if (properties.allowUnsafeDevSecret() && hasAnyActiveProfile(environment, "dev", "local")) {
            return EphemeralJwtKeyHolder.KEY;
        }
        throw new IllegalStateException("Configure APP_SECURITY_JWT_BASE64_SECRET, or explicitly set APP_SECURITY_ALLOW_UNSAFE_DEV_SECRET=true with the dev or local Spring profile to use an ephemeral local-only key.");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSecretKey, SecurityProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey).macAlgorithm(JWT_ALGORITHM).build();
        OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<List<String>>(
                JwtClaimNames.AUD, audience -> audience != null && audience.contains(properties.audience()));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(properties.issuer()), audienceValidator));
        return decoder;
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(SecurityConfiguration::extractAuthorities);
        return converter;
    }

    private static Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<String> authorities = jwt.getClaimAsStringList(AUTHORITIES_CLAIM);
        if (authorities == null) {
            return List.of();
        }
        return authorities.stream()
                .filter(SecurityConfiguration::isApplicationRole)
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }

    static List<String> applicationRoles(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .filter(authority -> authority != null)
                .map(GrantedAuthority::getAuthority)
                .filter(SecurityConfiguration::isApplicationRole)
                .distinct()
                .toList();
    }

    private static boolean isApplicationRole(String authority) {
        return authority != null && authority.startsWith("ROLE_");
    }

    private static SecretKey createEphemeralJwtKey() {
        byte[] secret = new byte[64];
        new SecureRandom().nextBytes(secret);
        LOGGER.warn("Using an ephemeral local-only JWT signing key; tokens become invalid after restart.");
        return new SecretKeySpec(secret, "HmacSHA512");
    }

    private static boolean hasAnyActiveProfile(Environment environment, String... profiles) {
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        return Arrays.stream(profiles).anyMatch(activeProfiles::contains);
    }

    private static List<String> splitCsv(String value) {
        return Arrays.stream(value.split(",")).map(String::trim).filter(item -> !item.isBlank()).toList();
    }

    private static final class EphemeralJwtKeyHolder {

        private static final SecretKey KEY = createEphemeralJwtKey();
    }
}
