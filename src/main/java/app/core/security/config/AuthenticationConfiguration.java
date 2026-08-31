package app.core.security.config;

import app.core.security.account.SecurityAccountProvider;
import app.core.security.authentication.LocalAuthenticationProvider;
import app.core.security.credential.CredentialService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthenticationConfiguration {

    @Bean
    LocalAuthenticationProvider localAuthenticationProvider(
            SecurityAccountProvider accountProvider,
            CredentialService credentialService,
            PasswordEncoder passwordEncoder) {
        return new LocalAuthenticationProvider(accountProvider, credentialService, passwordEncoder);
    }

    @Bean
    AuthenticationManager authenticationManager(LocalAuthenticationProvider provider) {
        return new ProviderManager(provider);
    }
}
