package app.core.pagination;

import app.core.exception.InvalidRequestException;

public enum SortDirection {
    ASC,
    DESC;

    public static SortDirection fromApiValue(String value) {
        if ("asc".equals(value)) {
            return ASC;
        }
        if ("desc".equals(value)) {
            return DESC;
        }
        throw new InvalidRequestException("La direction du tri doit être asc ou desc");
    }
}
