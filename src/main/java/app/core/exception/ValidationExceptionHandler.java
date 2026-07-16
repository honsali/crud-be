package app.core.exception;

import java.util.List;
import java.util.stream.Stream;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ValidationExceptionHandler {

    private record FieldViolation(String field, String message) {
    }

    private static ProblemDetail validationProblem() {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "The request contains invalid fields.");
        problem.setTitle("Validation failed");
        return problem;
    }

    private static FieldViolation toFieldViolation(FieldError fieldError) {
        return new FieldViolation(fieldError.getField(), fieldError.getDefaultMessage());
    }

    private static Stream<FieldViolation> toFieldViolations(ParameterValidationResult result) {
        if (result instanceof ParameterErrors errors) {
            return errors.getFieldErrors().stream().map(ValidationExceptionHandler::toFieldViolation);
        }
        String parameterName = result.getMethodParameter().getParameterName();
        String field = parameterName == null ? "arg" + result.getMethodParameter().getParameterIndex() : parameterName;
        return result.getResolvableErrors().stream().map(error -> new FieldViolation(field, error.getDefaultMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        ProblemDetail problem = validationProblem();
        List<FieldViolation> fields = exception.getBindingResult().getFieldErrors().stream().map(ValidationExceptionHandler::toFieldViolation).toList();
        problem.setProperty("fields", fields);
        return problem;
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ProblemDetail handleMethodValidation(HandlerMethodValidationException exception) {
        ProblemDetail problem = validationProblem();
        List<FieldViolation> fields = exception.getParameterValidationResults().stream().flatMap(ValidationExceptionHandler::toFieldViolations).toList();
        problem.setProperty("fields", fields);
        return problem;
    }
}
