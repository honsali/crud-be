package app.domain.admin.account;

import app.core.pagination.SortDirection;

public record AccountSearchCriteria(
        String username,
        String displayName,
        String email,
        Boolean active,
        Long roleId,
        int page,
        int size,
        AccountSortField sort,
        SortDirection direction) {
}
