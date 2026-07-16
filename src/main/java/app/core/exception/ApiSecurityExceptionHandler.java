package app.core.exception;

import java.io.IOException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Component
public final class ApiSecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final BearerTokenAuthenticationEntryPoint BEARER_AUTHENTICATION_ENTRY_POINT = new BearerTokenAuthenticationEntryPoint();
    private static final BearerTokenAccessDeniedHandler BEARER_ACCESS_DENIED_HANDLER = new BearerTokenAccessDeniedHandler();
    private static final String AUTHENTICATION_REQUIRED_CODE = "AUTHENTICATION_REQUIRED";
    private static final String ACCESS_DENIED_CODE = "ACCESS_DENIED";

    private final ObjectMapper objectMapper;

    public ApiSecurityExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        BEARER_AUTHENTICATION_ENTRY_POINT.commence(request, response, exception);
        writeProblem(request, response, HttpStatus.UNAUTHORIZED, "Authentication required", "Authentication is required to access this resource.", AUTHENTICATION_REQUIRED_CODE);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException, ServletException {
        BEARER_ACCESS_DENIED_HANDLER.handle(request, response, exception);
        writeProblem(request, response, HttpStatus.FORBIDDEN, "Access denied", "You do not have permission to access this resource.", ACCESS_DENIED_CODE);
    }

    private void writeProblem(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String title, String detail, String code) throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", code);

        response.setStatus(status.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
