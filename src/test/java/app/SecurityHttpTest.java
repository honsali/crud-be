package app;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import app.core.exception.ApiExceptionHandler;
import app.core.security.config.SecurityConfiguration;
import app.core.security.jwt.JwtTokenService;
import app.core.security.web.AuthController;
import app.core.security.web.AuthService;
import app.core.security.web.JsonAccessDeniedHandler;
import app.core.security.web.JsonAuthenticationEntryPoint;
import app.domain.admin.account.Account;
import app.domain.admin.account.AccountController;
import app.domain.admin.account.AccountRepository;
import app.domain.admin.account.AccountService;
import app.domain.admin.role.Role;
import app.domain.rh.departement.DepartementController;
import app.domain.rh.departement.DepartementService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = {AuthController.class, AccountController.class, DepartementController.class})
@Import({
        SecurityConfiguration.class,
        JwtTokenService.class,
        AuthService.class,
        JsonAuthenticationEntryPoint.class,
        JsonAccessDeniedHandler.class,
        ApiExceptionHandler.class
})
@ImportAutoConfiguration({
        SecurityAutoConfiguration.class,
        ServletWebSecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
})
@TestPropertySource(properties = "app.security.cors.allowed-origins=https://allowed.example")
class SecurityHttpTest {

    @Autowired MockMvc mockMvc;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtDecoder jwtDecoder;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean AccountRepository accountRepository;
    @MockitoBean AccountService accountService;
    @MockitoBean DepartementService departementService;

    @Test
    void loginReturnsTheFrontendJwtContract() throws Exception {
        Account account = account("alice.admin", "ADMIN", true, "password-123");
        when(accountRepository.findByUsername("alice.admin"))
                .thenReturn(Optional.of(account));

        String token = login(" Alice.Admin ", "password-123");
        var jwt = jwtDecoder.decode(token);

        org.assertj.core.api.Assertions.assertThat(jwt.getSubject()).isEqualTo("alice.admin");
        org.assertj.core.api.Assertions.assertThat(jwt.getClaimAsString("role")).isEqualTo("ROLE_ADMIN");
        org.assertj.core.api.Assertions.assertThat(jwt.getExpiresAt()).isNotNull();
    }

    @Test
    void protectsApiRoutesAndRestrictsAdministrationToAdmins() throws Exception {
        mockMvc.perform(get("/api/rh/departements"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));

        Account user = account("rh.user", "GESTIONNAIRE_RH", true, "password-123");
        when(accountRepository.findByUsername("rh.user"))
                .thenReturn(Optional.of(user));
        String userToken = login("rh.user", "password-123");
        mockMvc.perform(get("/api/admin/accounts").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        Account admin = account("admin", "ADMIN", true, "password-123");
        when(accountRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));
        when(accountService.lister()).thenReturn(List.of());
        String adminToken = login("admin", "password-123");
        mockMvc.perform(get("/api/admin/accounts").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsBadCredentialsAndInactiveAccounts() throws Exception {
        Account inactive = account("inactive", "ADMIN", false, "password-123");
        when(accountRepository.findByUsername("inactive"))
                .thenReturn(Optional.of(inactive));

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"inactive\",\"password\":\"password-123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"unknown\",\"password\":\"password-123\"}"))
                .andExpect(status().isUnauthorized());
    }

    private String login(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginBody(username, password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn().getResponse().getContentAsString();
        JsonNode body = objectMapper.readTree(response);
        return body.get("accessToken").asText();
    }

    private Account account(String username, String roleCode, boolean activated, String password) {
        Account account = mock(Account.class);
        Role role = mock(Role.class);
        when(account.getUsername()).thenReturn(username);
        when(account.getPasswordHash()).thenReturn(passwordEncoder.encode(password));
        when(account.getActivated()).thenReturn(activated);
        when(account.getRole()).thenReturn(role);
        when(role.getAuthority()).thenReturn("ROLE_" + roleCode);
        return account;
    }

    private record LoginBody(String username, String password) {
    }
}
