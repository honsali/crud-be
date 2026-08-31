package app;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import app.core.exception.ApiExceptionHandler;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = ApiErrorHttpTest.FailureProbeController.class)
@Import({ApiExceptionHandler.class, ApiErrorHttpTest.FailureProbeController.class})
class ApiErrorHttpTest {

    @Autowired MockMvc mockMvc;

    @Test
    void unknownApiRouteUsesTheStableNotFoundContract() throws Exception {
        mockMvc.perform(get("/api/error-contract/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("La ressource demandée n'existe pas"))
                .andExpect(jsonPath("$.path").value("/api/error-contract/unknown"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty())
                .andExpect(content().string(not(containsString("NoResourceFoundException"))));
    }

    @Test
    void incorrectMethodUsesApiErrorAndPreservesAllow() throws Exception {
        mockMvc.perform(post("/api/error-contract/known"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", containsString("GET")))
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"))
                .andExpect(jsonPath("$.path").value("/api/error-contract/known"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void incorrectContentTypeUsesTheStableUnsupportedMediaTypeContract() throws Exception {
        mockMvc.perform(post("/api/error-contract/content")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("technical-payload-marker"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"))
                .andExpect(jsonPath("$.path").value("/api/error-contract/content"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty())
                .andExpect(content().string(not(containsString("technical-payload-marker"))));
    }

    @Test
    void unexpectedExceptionUsesGeneric500WithoutTechnicalDetails() throws Exception {
        mockMvc.perform(get("/api/error-contract/failure"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Une erreur interne est survenue"))
                .andExpect(jsonPath("$.path").value("/api/error-contract/failure"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty())
                .andExpect(content().string(not(containsString("IllegalStateException"))))
                .andExpect(content().string(not(containsString("server-technical-detail"))))
                .andExpect(content().string(not(containsString("stackTrace"))));
    }

    @RestController
    @RequestMapping("/api/error-contract")
    public static class FailureProbeController {

        @GetMapping("/known")
        public Map<String, String> known() {
            return Map.of("status", "ok");
        }

        @PostMapping(value = "/content", consumes = MediaType.APPLICATION_JSON_VALUE)
        public void content(@RequestBody Map<String, Object> body) {
        }

        @GetMapping("/failure")
        public void failure() {
            throw new IllegalStateException("server-technical-detail");
        }
    }
}
