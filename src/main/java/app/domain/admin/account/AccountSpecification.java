package app.domain.admin.account;

import java.util.Locale;

import jakarta.persistence.criteria.Expression;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class AccountSpecification {

    private AccountSpecification() {
    }

    public static Specification<Account> from(AccountSearchCriteria criteria) {
        return Specification.<Account>unrestricted()
                .and(containsIgnoreCase("username", criteria.username()))
                .and(containsIgnoreCase("displayName", criteria.displayName()))
                .and(containsIgnoreCase("email", criteria.email()))
                .and(hasActive(criteria.active()))
                .and(hasRole(criteria.roleId()));
    }

    private static Specification<Account> containsIgnoreCase(String property, String value) {
        if (!StringUtils.hasText(value)) {
            return Specification.unrestricted();
        }
        String pattern = "%" + escapeLike(value.strip().toLowerCase(Locale.ROOT)) + "%";
        return (root, query, builder) -> {
            Expression<String> expression = builder.lower(root.get(property));
            return builder.like(expression, pattern, '\\');
        };
    }

    private static Specification<Account> hasActive(Boolean active) {
        if (active == null) {
            return Specification.unrestricted();
        }
        return (root, query, builder) -> builder.equal(root.get("active"), active);
    }

    private static Specification<Account> hasRole(Long roleId) {
        if (roleId == null) {
            return Specification.unrestricted();
        }
        return (root, query, builder) -> builder.equal(root.get("role").get("id"), roleId);
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
