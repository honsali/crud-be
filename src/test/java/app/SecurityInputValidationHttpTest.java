package app;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.core.exception.ApiExceptionHandler;
import app.core.security.account.SecurityAccountProvider;
import app.core.security.credential.CredentialService;
import app.core.security.jwt.JwtTokenService;
import app.core.security.ratelimit.LoginAttemptLimiter;
import app.core.security.web.AdminCredentialController;
import app.core.security.web.AuthController;
import app.core.security.web.AuthService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = {AuthController.class, AdminCredentialController.class})
@Import({AuthService.class, ApiExceptionHandler.class})
class SecurityInputValidationHttpTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean AuthenticationManager authenticationManager;
    @MockitoBean JwtTokenService jwtTokenService;
    @MockitoBean LoginAttemptLimiter loginAttemptLimiter;
    @MockitoBean CredentialService credentialService;
    @MockitoBean SecurityAccountProvider accountProvider;

    @Test
    void oversizedUsernameIsRejectedBeforeRateLimitingOrAuthentication() throws Exception {
        String oversizedUsername = "oversized-username-marker-" + "x".repeat(101);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginBody(oversizedUsername, "mot de passe valide"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("username"))
                .andExpect(content().string(not(containsString(oversizedUsername))));

        verifyNoInteractions(loginAttemptLimiter, authenticationManager);
    }

    @Test
    void oversizedLoginPasswordIsRejectedBeforeRateLimitingOrAuthentication() throws Exception {
        String oversizedPassword = "login-password-secret-marker-" + "x".repeat(257);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginBody("alice.admin", oversizedPassword))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("password"))
                .andExpect(content().string(not(containsString(oversizedPassword))));

        verifyNoInteractions(loginAttemptLimiter, authenticationManager);
    }

    @Test
    void oversizedCurrentAndNewPasswordsNeverReachCredentialOrAccountServices() throws Exception {
        String oversizedCurrentPassword = "current-password-secret-marker-" + "x".repeat(257);
        String oversizedNewPassword = "new-password-secret-marker-" + "x".repeat(257);

        mockMvc.perform(put("/api/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PasswordChangeBody(oversizedCurrentPassword, "nouveau mot de passe sûr"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(content().string(not(containsString(oversizedCurrentPassword))));

        mockMvc.perform(put("/api/admin/accounts/7/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PasswordResetBody(oversizedNewPassword))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(content().string(not(containsString(oversizedNewPassword))));

        verifyNoInteractions(credentialService, accountProvider);
    }

    private record LoginBody(String username, String password) {
    }

    private record PasswordChangeBody(String currentPassword, String newPassword) {
    }

    private record PasswordResetBody(String newPassword) {
    }
}
