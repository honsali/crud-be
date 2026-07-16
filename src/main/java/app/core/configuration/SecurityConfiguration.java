package app.core.configuration;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
import app.core.exception.ApiSecurityExceptionHandler;
import app.core.security.account.AppRole;
import app.core.security.login.JwtProperties;
import app.core.security.login.JwtToken;
import app.core.security.login.LoginTokenValidator;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
class SecurityConfiguration {

    private static Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        return JwtToken.role(jwt).<Collection<GrantedAuthority>>map(role -> List.of(new SimpleGrantedAuthority(role.name()))).orElseGet(List::of);
    }

    private static List<String> splitCsv(String value) {
        return Arrays.stream(value.split(",")).map(String::trim).filter(item -> !item.isBlank()).toList();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ApiSecurityExceptionHandler securityExceptionHandler) throws Exception {
        http//
                .csrf(AbstractHttpConfigurer::disable)//
                .cors(Customizer.withDefaults())//
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))//
                .authorizeHttpRequests(//
                        auth -> auth//
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()//
                                .requestMatchers(HttpMethod.POST, "/api/login").permitAll()//
                                .requestMatchers("/api/admin/accounts", "/api/admin/accounts/**").hasAuthority(AppRole.ROLE_ADMIN.name())//
                                .requestMatchers("/api/admin/**").denyAll()//
                                .requestMatchers("/api/rh/**").hasAuthority(AppRole.ROLE_GESTIONNAIRE_RH.name())//
                                .requestMatchers("/api/**").denyAll()//
                                .anyRequest().denyAll())
                .exceptionHandling(//
                        exceptions -> exceptions//
                                .authenticationEntryPoint(securityExceptionHandler)//
                                .accessDeniedHandler(securityExceptionHandler))
                .oauth2ResourceServer(//
                        oauth2 -> oauth2//
                                .authenticationEntryPoint(securityExceptionHandler)//
                                .accessDeniedHandler(securityExceptionHandler)//
                                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    @Bean
    UrlBasedCorsConfigurationSource corsConfigurationSource(@Value("${application.cors.allowed-origins}") String allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(splitCsv(allowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
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
    SecretKey jwtSecretKey(@Value("${application.security.jwt-base64-secret}") String encodedSecret) {
        if (encodedSecret == null || encodedSecret.isBlank()) {
            throw new IllegalStateException("APP_SECURITY_JWT_BASE64_SECRET must be configured.");
        }

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

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSecretKey, JwtProperties properties, LoginTokenValidator loginTokenValidator) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey).macAlgorithm(JwtToken.ALGORITHM).build();
        OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<List<String>>(JwtClaimNames.AUD, audience -> audience != null && audience.contains(properties.audience()));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(properties.issuer()), audienceValidator, loginTokenValidator));
        return decoder;
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(SecurityConfiguration::extractAuthorities);
        return converter;
    }
}
