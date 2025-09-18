package app.core.validation;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ValidationErrorHandler {

    @ExceptionHandler(AssertionException.class)
    public ResponseEntity<Map<String, Object>> handleAssertionException(AssertionException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("type", ex.type().toString());
        error.put("field", ex.field());
        error.put("message", ex.getMessage());
        error.put("parameters", ex.parameters());

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        Map<String, Object> response = new HashMap<>();
        response.put("type", "VALIDATION_ERROR");
        response.put("errors", errors);

        return ResponseEntity.badRequest().body(response);
    }
}