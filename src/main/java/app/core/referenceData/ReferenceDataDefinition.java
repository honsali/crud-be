package app.core.referenceData;

import java.util.Set;

public record ReferenceDataDefinition(String entityName, String labelField, Set<String> filterFields) {

    public ReferenceDataDefinition {
        filterFields = Set.copyOf(filterFields);
    }
}
