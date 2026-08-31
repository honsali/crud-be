package app.core.pagination;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import app.core.exception.InvalidRequestException;

public final class PageableUtils {

    private static final int TAILLE_PAGE_MAX = 100;

    public static Pageable paginationValide(Pageable pageable) {
        if (pageable.getPageNumber() < 0
                || pageable.getPageSize() < 1
                || pageable.getPageSize() > TAILLE_PAGE_MAX) {
            throw new InvalidRequestException("La page doit être positive et sa taille comprise entre 1 et 100");
        }

        Sort tri = pageable.getSort();
        if (tri.isUnsorted()) {
            tri = Sort.by(Sort.Direction.ASC, "id");
        } else if (tri.getOrderFor("id") == null) {
            tri = tri.and(Sort.by(Sort.Direction.ASC, "id"));
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), tri);
    }

    private PageableUtils() {}
}
