package app.core;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

public abstract class BaseSpecification {

    protected BaseSpecification() {
    }

    protected static void addLike(List<Predicate> predicates, CriteriaBuilder criteriaBuilder, Expression<String> field, String value) {
        if (value != null && !value.isBlank()) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(field), "%" + escapeLike(value) + "%", '\\'));
        }
    }

    private static String escapeLike(String value) {
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    protected static void addDateRange(List<Predicate> predicates, CriteriaBuilder criteriaBuilder, Expression<LocalDate> field, LocalDate start, LocalDate end) {
        if (start != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(field, start));
        }
        if (end != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(field, end));
        }
    }

    protected static <T> void addEqual(List<Predicate> predicates, CriteriaBuilder criteriaBuilder, Expression<T> field, T value) {
        if (value != null) {
            predicates.add(criteriaBuilder.equal(field, value));
        }
    }
}
