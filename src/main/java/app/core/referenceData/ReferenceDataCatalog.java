package app.core.referenceData;

import java.util.Optional;

public interface ReferenceDataCatalog {

    Optional<ReferenceDataDefinition> find(String referenceName);
}
