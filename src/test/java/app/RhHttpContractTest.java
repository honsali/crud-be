package app;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
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

@WebMvcTest(controllers = {
                DepartementController.class,
                EmployeController.class,
                CongeController.class
})
@WithMockUser(roles = "USER")
class RhHttpContractTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private DepartementService departementService;

        @MockitoBean
        private EmployeService employeService;

        @MockitoBean
        private CongeService congeService;

        @Test
        void createsADepartementWithoutLocationAndWithAJavascriptSafeId() throws Exception {
                long idBeyondJavascriptSafeInteger = 9_007_199_254_740_993L;
                when(departementService.creer(any(DepartementCreateRequest.class)))
                                .thenReturn(new DepartementResponse(
                                                idBeyondJavascriptSafeInteger, "Support", "Services internes", 0));

                mockMvc.perform(post("/api/rh/departement")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                                {"nom":"Support","description":"Services internes"}
                                """))
                                .andExpect(status().isCreated())
                                .andExpect(header().doesNotExist(HttpHeaders.LOCATION))
                                .andExpect(jsonPath("$.id").value("9007199254740993"))
                                .andExpect(jsonPath("$.nom").value("Support"))
                                .andExpect(jsonPath("$.version").value(0));
        }

        @Test
        void listsDepartementsInTheDeterministicOrderProvidedByTheUseCase() throws Exception {
                when(departementService.lister()).thenReturn(List.of(
                                new DepartementResponse(2L, "Administration", null, 0),
                                new DepartementResponse(1L, "Support", null, 0)));

                mockMvc.perform(get("/api/rh/departement"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value("2"))
                                .andExpect(jsonPath("$[0].nom").value("Administration"))
                                .andExpect(jsonPath("$[1].id").value("1"))
                                .andExpect(jsonPath("$[1].nom").value("Support"));
        }

        @Test
        void reportsBodyValidationErrorsWithTheStableErrorContract() throws Exception {
                mockMvc.perform(post("/api/rh/departement")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"nom":"   "}
                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                                .andExpect(jsonPath("$.path").value("/api/rh/departement"))
                                .andExpect(jsonPath("$.fieldErrors[0].field").value("nom"))
                                .andExpect(jsonPath("$.fieldErrors[0].code").value("NotBlank"));
        }

        @Test
        void retrievesAndUpdatesADepartementWithItsCurrentRepresentation() throws Exception {
                when(departementService.recupererParId(3L))
                                .thenReturn(new DepartementResponse(3L, "Support", "Services internes", 1));
                when(departementService.maj(org.mockito.ArgumentMatchers.eq(3L), any(DepartementUpdateRequest.class)))
                                .thenReturn(new DepartementResponse(3L, "Assistance", "Services internes", 2));

                mockMvc.perform(get("/api/rh/departement/3"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value("3"))
                                .andExpect(jsonPath("$.nom").value("Support"))
                                .andExpect(jsonPath("$.version").value(1));

                mockMvc.perform(put("/api/rh/departement/3")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"nom":"Assistance","description":"Services internes","version":1}
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value("3"))
                                .andExpect(jsonPath("$.nom").value("Assistance"))
                                .andExpect(jsonPath("$.version").value(2));
        }

        @Test
        void deletesADepartementWithoutResponseBody() throws Exception {
                mockMvc.perform(delete("/api/rh/departement/3"))
                                .andExpect(status().isNoContent())
                                .andExpect(content().string(""));

                verify(departementService).supprimer(3L);
        }

        @Test
        void exposesAnApplicationOwnedPageAndPassesExplicitSearchOptions() throws Exception {
                EmployeResponse employe = new EmployeResponse(
                                11L,
                                "M-011",
                                "Martin",
                                "Alice",
                                LocalDate.of(1990, 5, 12),
                                new Reference(3L, "Féminin"),
                                new Reference(4L, "Marié(e)"),
                                LocalDate.of(2020, 9, 1),
                                null,
                                "0600000000",
                                "Rabat",
                                "10 rue des Fleurs",
                                "Analyste",
                                "Profil RH",
                                new Reference(2L, "Support"),
                                0);
                when(employeService.filtrer(any(EmployeFiltre.class), any(Pageable.class)))
                                .thenReturn(new PageImpl<>(List.of(employe), PageRequest.of(1, 1), 3));

                mockMvc.perform(post("/api/rh/employe/filtrer")
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
                                .andExpect(jsonPath("$.items[0].matricule").value("M-011"))
                                .andExpect(jsonPath("$.items[0].dateNaissance").value("1990-05-12"))
                                .andExpect(jsonPath("$.items[0].sexe.id").value("3"))
                                .andExpect(jsonPath("$.items[0].departement.id").value("2"))
                                .andExpect(jsonPath("$.page").value(1))
                                .andExpect(jsonPath("$.size").value(1))
                                .andExpect(jsonPath("$.totalElements").value(3))
                                .andExpect(jsonPath("$.totalPages").value(3))
                                .andExpect(jsonPath("$.first").value(false))
                                .andExpect(jsonPath("$.last").value(false));

                ArgumentCaptor<EmployeFiltre> filtreCaptor = ArgumentCaptor.forClass(EmployeFiltre.class);
                ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
                verify(employeService).filtrer(filtreCaptor.capture(), pageableCaptor.capture());
                assertThatFilterAndPageableAreMapped(filtreCaptor.getValue(), pageableCaptor.getValue());
        }

        @Test
        void rejectsATextSearchParameterLongerThan250Characters() throws Exception {
                mockMvc.perform(post("/api/rh/employe/filtrer")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"nom\":\"" + "x".repeat(251) + "\"}"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                                .andExpect(jsonPath("$.fieldErrors[0].field").value("nom"));

                verify(employeService, never()).filtrer(any(), any());
        }

        @Test
        void acceptsAStringDepartmentIdWhenCreatingAnEmploye() throws Exception {
                long departementId = 9_007_199_254_740_993L;
                EmployeResponse response = new EmployeResponse(
                                12L,
                                "M-012",
                                "Martin",
                                "Alice",
                                LocalDate.of(1992, 4, 3),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                new Reference(departementId, "Support"),
                                0);
                when(employeService.creer(any(EmployeCreateRequest.class))).thenReturn(response);

                mockMvc.perform(post("/api/rh/employe")
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
                                .andExpect(header().doesNotExist(HttpHeaders.LOCATION))
                                .andExpect(jsonPath("$.id").value("12"))
                                .andExpect(jsonPath("$.departement.id").value("9007199254740993"));

                ArgumentCaptor<EmployeCreateRequest> captor = ArgumentCaptor.forClass(EmployeCreateRequest.class);
                verify(employeService).creer(captor.capture());
                org.assertj.core.api.Assertions.assertThat(captor.getValue().dateNaissance())
                                .isEqualTo(LocalDate.of(1992, 4, 3));
                org.assertj.core.api.Assertions.assertThat(captor.getValue().departement().id())
                                .isEqualTo(departementId);
        }

        @Test
        void requiresTheBirthDateWhenCreatingAnEmploye() throws Exception {
                mockMvc.perform(post("/api/rh/employe")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"matricule":"M-013","nom":"Martin","prenom":"Alice"}
                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                                .andExpect(jsonPath("$.fieldErrors[0].field").value("dateNaissance"));

                verify(employeService, never()).creer(any());
        }

        @Test
        void rejectsAReferenceWithoutAnIdBeforeCallingTheEmployeService() throws Exception {
                mockMvc.perform(post("/api/rh/employe")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "matricule":"M-014",
                                                  "nom":"Martin",
                                                  "prenom":"Alice",
                                                  "dateNaissance":"1990-01-01",
                                                  "departement":{"libelle":"Support"}
                                                }
                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

                verify(employeService, never()).creer(any());
        }

        @Test
        void createsAndListsLeavesInTheirEmployeContext() throws Exception {
                Reference employe = new Reference(4L, "M-004");
                Reference typeConge = new Reference(8L, "Congé payé");
                CongeResponse conge = new CongeResponse(
                                21L,
                                "CONGE-001",
                                typeConge,
                                LocalDate.of(2026, 8, 10),
                                LocalDate.of(2026, 8, 12),
                                "Repos",
                                employe,
                                0);
                when(congeService.creer(any(), any(CongeCreateRequest.class))).thenReturn(conge);
                when(congeService.listerParIdEmploye(4L)).thenReturn(List.of(conge));

                mockMvc.perform(post("/api/rh/conge/employe/4")
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
                                .andExpect(header().doesNotExist(HttpHeaders.LOCATION))
                                .andExpect(jsonPath("$.id").value("21"))
                                .andExpect(jsonPath("$.code").value("CONGE-001"))
                                .andExpect(jsonPath("$.typeConge.id").value("8"))
                                .andExpect(jsonPath("$.employe.id").value("4"));

                mockMvc.perform(get("/api/rh/conge/employe/4"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value("21"))
                                .andExpect(jsonPath("$[0].employe.id").value("4"))
                                .andExpect(jsonPath("$[0].dateDebutConge").value("2026-08-10"));

                verify(congeService).listerParIdEmploye(4L);
        }

        @Test
        void rejectsABlankLeaveCodeBeforeCallingTheService() throws Exception {
                mockMvc.perform(post("/api/rh/conge/employe/4")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "code":"   ",
                                                  "dateDebutConge":"2026-08-11",
                                                  "dateFinConge":"2026-08-10",
                                                  "commentaire":"Repos"
                                                }
                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                                .andExpect(jsonPath("$.fieldErrors[0].field").value("code"));

                verify(congeService, never()).creer(any(), any());
        }

        @Test
        void mapsMissingResourcesToTheStableNotFoundContract() throws Exception {
                when(employeService.recupererParId(99L)).thenThrow(new ResourceNotFoundException("Employé", 99L));

                mockMvc.perform(get("/api/rh/employe/99"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                                .andExpect(jsonPath("$.path").value("/api/rh/employe/99"));
        }

        @Test
        void hidesJpaDetailsBehindTheStableOptimisticConflictContract() throws Exception {
                when(employeService.recupererParId(7L)).thenThrow(new ObjectOptimisticLockingFailureException(
                                "Hibernate stale entity details",
                                new IllegalStateException("JPA internal state")));

                mockMvc.perform(get("/api/rh/employe/7"))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.code").value("CONFLICT"))
                                .andExpect(jsonPath("$.message").value(
                                                "La ressource a été modifiée par une autre transaction"))
                                .andExpect(content().string(not(containsString("Hibernate"))))
                                .andExpect(content().string(not(containsString("JPA"))));
        }

        @Test
        void mapsTheApplicationStaleVersionExceptionToTheStableConflictContract() throws Exception {
                when(employeService.maj(
                                org.mockito.ArgumentMatchers.eq(7L), any(EmployeUpdateRequest.class)))
                                                .thenThrow(new StaleVersionException("Employe", 7L));

                mockMvc.perform(put("/api/rh/employe/7")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "matricule":"M-007",
                                                  "nom":"Martin",
                                                  "prenom":"Alice",
                                                  "dateNaissance":"1990-01-01",
                                                  "departement":{"id":"2"},
                                                  "version":1
                                                }
                                                """))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.code").value("CONFLICT"))
                                .andExpect(jsonPath("$.message").value("Employe 7 a été modifié depuis sa lecture"))
                                .andExpect(content().string(not(containsString("StaleVersionException"))))
                                .andExpect(content().string(not(containsString("app.core"))));
        }

        private void assertThatFilterAndPageableAreMapped(EmployeFiltre filtre, Pageable pageable) {
                org.assertj.core.api.Assertions.assertThat(filtre.prenom()).isEqualTo("ali");
                org.assertj.core.api.Assertions.assertThat(filtre.debutDateNaissance())
                                .isEqualTo(LocalDate.of(1980, 1, 1));
                org.assertj.core.api.Assertions.assertThat(filtre.finDateNaissance())
                                .isEqualTo(LocalDate.of(2000, 12, 31));
                org.assertj.core.api.Assertions.assertThat(filtre.sexe().id()).isEqualTo(3L);
                org.assertj.core.api.Assertions.assertThat(filtre.departement().id()).isEqualTo(2L);
                org.assertj.core.api.Assertions.assertThat(pageable.getPageNumber()).isEqualTo(1);
                org.assertj.core.api.Assertions.assertThat(pageable.getPageSize()).isEqualTo(1);
                org.assertj.core.api.Assertions.assertThat(pageable.getSort().getOrderFor("prenom").getDirection().name())
                                .isEqualTo("DESC");
        }
}
