package app.core.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityProperties.class)
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
class SecurityConfiguration {

    private final SecurityProperties securityProperties;
    private final CorsFilter corsFilter;

    public SecurityConfiguration(SecurityProperties securityProperties, CorsFilter corsFilter) {
        this.securityProperties = securityProperties;
        this.corsFilter = corsFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // @formatter:off
    /* 
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web ->
            web
                .ignoring()
                .requestMatchers(HttpMethod.OPTIONS, "/**")
                .requestMatchers("/app/**")
                .requestMatchers("/i18n/**")
                .requestMatchers("/content/**");
    }
    */
    // @formatter:on

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    // @formatter:off
    http
      .csrf(csrf -> csrf.disable())
      .addFilterBefore(new JwtFilter(securityProperties), UsernamePasswordAuthenticationFilter.class)
      .addFilterBefore(corsFilter, JwtFilter.class)
      .headers(headers -> headers
        .frameOptions(FrameOptionsConfig::deny)
        .contentSecurityPolicy(csp -> csp.policyDirectives(securityProperties.getContentSecurityPolicy()))
        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
        .permissionsPolicy(permissions ->  permissions.policy(securityProperties.getPermissionPolicy()))
      )
      .formLogin(AbstractHttpConfigurer::disable)
      .httpBasic(AbstractHttpConfigurer::disable)
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(authz -> authz
        .requestMatchers("/api/authenticate").permitAll()
        .requestMatchers("/api/register").permitAll()
        .requestMatchers("/api/activate").permitAll()
        .requestMatchers("/api/user/reset-password/init").permitAll()
        .requestMatchers("/api/user/reset-password/finish").permitAll()
        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
        .requestMatchers("/api/**").authenticated() 
        .anyRequest().authenticated()
      );
      return http.build();
    // @formatter:on
    }

}
