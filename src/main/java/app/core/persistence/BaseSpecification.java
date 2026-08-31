package app.core.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import app.core.reference.Reference;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public abstract class BaseSpecification {

    protected static void addLike(
            List<Predicate> predicates,
            CriteriaBuilder builder,
            Expression<String> field,
            String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        String pattern = "%" + escapeLike(value.strip().toLowerCase(Locale.ROOT)) + "%";
        predicates.add(builder.like(builder.lower(field), pattern, '\\'));
    }

    protected static void addDateRange(
            List<Predicate> predicates,
            CriteriaBuilder builder,
            Expression<LocalDate> field,
            LocalDate start,
            LocalDate end) {
        if (start != null) {
            predicates.add(builder.greaterThanOrEqualTo(field, start));
        }
        if (end != null) {
            predicates.add(builder.lessThanOrEqualTo(field, end));
        }
    }

    protected static <T> void addReference(
            List<Predicate> predicates,
            CriteriaBuilder builder,
            Root<T> root,
            String association,
            Reference reference) {
        if (reference == null || reference.id() == null) {
            return;
        }
        predicates.add(builder.equal(root.get(association).get("id"), reference.id()));
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    protected BaseSpecification() {
    }
}
