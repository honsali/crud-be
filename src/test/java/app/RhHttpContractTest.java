package app;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import app.core.exception.ResourceNotFoundException;
import app.core.exception.StaleVersionException;
import app.core.reference.Reference;
import app.domain.rh.conge.CongeController;
import app.domain.rh.conge.CongeCreateRequest;
import app.domain.rh.conge.CongeResponse;
import app.domain.rh.conge.CongeService;
import app.domain.rh.departement.DepartementController;
import app.domain.rh.departement.DepartementCreateRequest;
import app.domain.rh.departement.DepartementResponse;
import app.domain.rh.departement.DepartementService;
import app.domain.rh.departement.DepartementUpdateRequest;
import app.domain.rh.employe.EmployeController;
import app.domain.rh.employe.EmployeCreateRequest;
import app.domain.rh.employe.EmployeFiltre;
import app.domain.rh.employe.EmployeResponse;
import app.domain.rh.employe.EmployeService;
import app.domain.rh.employe.EmployeUpdateRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {
        DepartementController.class,
        EmployeController.class,
        CongeController.class
})
@WithMockUser(roles = "USER")
class RhHttpContractTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean DepartementService departementService;
    @MockitoBean EmployeService employeService;
    @MockitoBean CongeService congeService;

    @Test
    void exposesTheSimpleDepartementCrud() throws Exception {
        long largeId = 9_007_199_254_740_993L;
        DepartementResponse created = new DepartementResponse(largeId, "Support", "Services internes", 0);
        DepartementResponse current = new DepartementResponse(3L, "Support", "Services internes", 1);
        DepartementResponse updated = new DepartementResponse(3L, "Assistance", "Services internes", 2);
        when(departementService.creer(any())).thenReturn(created);
        when(departementService.lister()).thenReturn(List.of(current));
        when(departementService.recupererParId(3L)).thenReturn(current);
        when(departementService.maj(eq(3L), any())).thenReturn(updated);

        mockMvc.perform(post("/api/rh/departements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":"Support","description":"Services internes"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id").value("9007199254740993"));
        mockMvc.perform(get("/api/rh/departements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Support"));
        mockMvc.perform(get("/api/rh/departements/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1));
        mockMvc.perform(put("/api/rh/departements/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":"Assistance","description":"Services internes","version":1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Assistance"))
                .andExpect(jsonPath("$.version").value(2));
        mockMvc.perform(delete("/api/rh/departements/3"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(departementService).supprimer(3L);
    }

    @Test
    void validatesTheThreeRequestShapesBeforeCallingServices() throws Exception {
        mockMvc.perform(post("/api/rh/departements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("nom"));
        mockMvc.perform(post("/api/rh/employes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"matricule":"M-013","nom":"Martin","prenom":"Alice"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("dateNaissance"));
        mockMvc.perform(post("/api/rh/employes/4/conges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("code"));

        verify(departementService, never()).creer(any());
        verify(employeService, never()).creer(any());
        verify(congeService, never()).creer(any(), any());
    }

    @Test
    void mapsTheEmployeFilterAndReturnsAnApplicationOwnedPage() throws Exception {
        EmployeResponse employe = new EmployeResponse(
                11L, "M-011", "Martin", "Alice", LocalDate.of(1990, 5, 12),
                new Reference(3L, "Féminin"), null, null, null, null, null, null, null, null,
                new Reference(2L, "Support"), 0);
        when(employeService.filtrer(any(), any()))
                .thenReturn(new PageImpl<>(List.of(employe), PageRequest.of(1, 1), 3));

        mockMvc.perform(post("/api/rh/employes/filtrer")
                        .param("page", "1")
                        .param("size", "1")
                        .param("sort", "prenom,desc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "prenom":"ali",
                                  "debutDateNaissance":"1980-01-01",
                                  "finDateNaissance":"2000-12-31",
                                  "sexe":{"id":"3"},
                                  "departement":{"id":"2"}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value("11"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(3));

        ArgumentCaptor<EmployeFiltre> filter = ArgumentCaptor.forClass(EmployeFiltre.class);
        ArgumentCaptor<Pageable> page = ArgumentCaptor.forClass(Pageable.class);
        verify(employeService).filtrer(filter.capture(), page.capture());
        org.assertj.core.api.Assertions.assertThat(filter.getValue().prenom()).isEqualTo("ali");
        org.assertj.core.api.Assertions.assertThat(filter.getValue().sexe().id()).isEqualTo(3L);
        org.assertj.core.api.Assertions.assertThat(filter.getValue().departement().id()).isEqualTo(2L);
        org.assertj.core.api.Assertions.assertThat(page.getValue().getPageNumber()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(page.getValue().getSort().getOrderFor("prenom").isDescending())
                .isTrue();
    }

    @Test
    void acceptsStringReferencesWhenCreatingAnEmploye() throws Exception {
        long departementId = 9_007_199_254_740_993L;
        EmployeResponse response = new EmployeResponse(
                12L, "M-012", "Martin", "Alice", LocalDate.of(1992, 4, 3),
                null, null, null, null, null, null, null, null, null,
                new Reference(departementId, "Support"), 0);
        when(employeService.creer(any())).thenReturn(response);

        mockMvc.perform(post("/api/rh/employes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matricule":"M-012",
                                  "nom":"Martin",
                                  "prenom":"Alice",
                                  "dateNaissance":"1992-04-03",
                                  "departement":{"id":"9007199254740993"}
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("12"))
                .andExpect(jsonPath("$.departement.id").value("9007199254740993"));

        ArgumentCaptor<EmployeCreateRequest> request = ArgumentCaptor.forClass(EmployeCreateRequest.class);
        verify(employeService).creer(request.capture());
        org.assertj.core.api.Assertions.assertThat(request.getValue().dateNaissance())
                .isEqualTo(LocalDate.of(1992, 4, 3));
        org.assertj.core.api.Assertions.assertThat(request.getValue().departement().id())
                .isEqualTo(departementId);
    }

    @Test
    void exposesCongeAsAnEmployeChild() throws Exception {
        CongeResponse conge = new CongeResponse(
                21L, "CONGE-001", new Reference(8L, "Congé payé"),
                LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 12), "Repos",
                new Reference(4L, "M-004"), 0);
        when(congeService.creer(eq(4L), any())).thenReturn(conge);
        when(congeService.listerParIdEmploye(4L)).thenReturn(List.of(conge));

        mockMvc.perform(post("/api/rh/employes/4/conges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code":"CONGE-001",
                                  "typeConge":{"id":"8"},
                                  "dateDebutConge":"2026-08-10",
                                  "dateFinConge":"2026-08-12",
                                  "commentaire":"Repos"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("21"))
                .andExpect(jsonPath("$.employe.id").value("4"));
        mockMvc.perform(get("/api/rh/employes/4/conges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("CONGE-001"));
    }

    @Test
    void mapsDomainErrorsToTheStableApiContract() throws Exception {
        when(employeService.recupererParId(99L)).thenThrow(new ResourceNotFoundException("Employé", 99L));
        when(employeService.maj(eq(7L), any(EmployeUpdateRequest.class)))
                .thenThrow(new StaleVersionException("Employe", 7L));

        mockMvc.perform(get("/api/rh/employes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        mockMvc.perform(put("/api/rh/employes/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matricule":"M-007",
                                  "nom":"Martin",
                                  "prenom":"Alice",
                                  "dateNaissance":"1990-01-01",
                                  "version":1
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Employe 7 a été modifié depuis sa lecture"));
    }
}
