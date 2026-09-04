package app;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import app.core.reference.Reference;
import app.domain.admin.account.AccountController;
import app.domain.admin.account.AccountCreateRequest;
import app.domain.admin.account.AccountResponse;
import app.domain.admin.account.AccountService;
import app.domain.admin.account.AccountUpdateRequest;
import app.domain.admin.account.PasswordResetRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AccountController.class)
@WithMockUser(roles = "ADMIN")
class AdminHttpContractTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AccountService accountService;

    @Test
    void exposesTheFiveAccountOperationsUsedByTheFrontend() throws Exception {
        AccountResponse account = account(9_007_199_254_740_993L, "alice", "ROLE_ADMIN", true, 4L);
        when(accountService.creer(any())).thenReturn(account);
        when(accountService.lister()).thenReturn(List.of(account));
        when(accountService.recupererParId(account.id())).thenReturn(account);
        when(accountService.maj(any(), any())).thenReturn(account);

        mockMvc.perform(post("/api/admin/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":" Alice ","password":"password-123","role":{"id":"1"}}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.id").value("9007199254740993"))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.role.id").value("1"))
                .andExpect(jsonPath("$.role.libelle").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.activated").value(true))
                .andExpect(jsonPath("$.version").value(4));

        mockMvc.perform(get("/api/admin/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("alice"));
        mockMvc.perform(get("/api/admin/accounts/{id}", account.id()))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/admin/accounts/{id}", account.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":{\"id\":\"1\"},\"activated\":true,\"version\":4}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/admin/accounts/{id}/password", account.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"new-password-123\"}"))
                .andExpect(status().isNoContent());

        verify(accountService).reinitialiserMotDePasse(any(), any(PasswordResetRequest.class));
    }

    @Test
    void acceptsAUsernameToNormalizeAndReadsTheRole() throws Exception {
        when(accountService.creer(any())).thenReturn(account(1L, "alice", "ROLE_ADMIN", true, 0L));
        ArgumentCaptor<AccountCreateRequest> captor = ArgumentCaptor.forClass(AccountCreateRequest.class);

        mockMvc.perform(post("/api/admin/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":" Alice.Admin ","password":"password-123","role":{"id":"1"}}
                                """))
                .andExpect(status().isCreated());

        verify(accountService).creer(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().username()).isEqualTo(" Alice.Admin ");
        org.assertj.core.api.Assertions.assertThat(captor.getValue().role()).isEqualTo(new Reference(1L, null));
    }

    @Test
    void validatesCreateUpdateAndPasswordBodies() throws Exception {
        mockMvc.perform(post("/api/admin/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"a\",\"password\":\"short\",\"role\":{}}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/admin/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":{\"id\":\"1\"},\"activated\":true}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/admin/accounts/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"short\"}"))
                .andExpect(status().isBadRequest());
    }

    private AccountResponse account(Long id, String username, String role, boolean activated, long version) {
        return new AccountResponse(id, username, new Reference(1L, role), activated, version);
    }
}
