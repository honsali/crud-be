package app.domain.rh.conge;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Set;

import app.core.reference.Reference;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class CongeValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsACompleteRequest() {
        CongeCreateRequest request = new CongeCreateRequest(
                "CONGE-001",
                new Reference(1L, "Congé payé"),
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 12),
                "Repos");

        Set<ConstraintViolation<CongeCreateRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void rejectsABlankCode() {
        CongeCreateRequest request = new CongeCreateRequest(
                "   ",
                null,
                LocalDate.of(2026, 8, 11),
                LocalDate.of(2026, 8, 10),
                "Repos");

        Set<ConstraintViolation<CongeCreateRequest>> violations = validator.validate(request);

        assertThat(violations)
                .singleElement()
                .satisfies(violation -> assertThat(violation.getPropertyPath().toString()).isEqualTo("code"));
    }

    @Test
    void rejectsATypeReferenceWithoutAnId() {
        CongeCreateRequest request = new CongeCreateRequest(
                "CONGE-002",
                new Reference(null, "Congé payé"),
                null,
                null,
                null);

        Set<ConstraintViolation<CongeCreateRequest>> violations = validator.validate(request);

        assertThat(violations)
                .singleElement()
                .satisfies(violation -> assertThat(violation.getPropertyPath().toString()).isEqualTo("typeConge.id"));
    }
}
