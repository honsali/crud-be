package app.core.pagination;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageResponse<T>(//
        List<T> content, //
        long totalElements, //
        int totalPages, //
        int size, //
        int number, //
        int numberOfElements, //
        boolean first, //
        boolean last) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(List.copyOf(page.getContent()), //
                page.getTotalElements(), //
                page.getTotalPages(), //
                page.getSize(), //
                page.getNumber(), //
                page.getNumberOfElements(), //
                page.isFirst(), //
                page.isLast());
    }
}
