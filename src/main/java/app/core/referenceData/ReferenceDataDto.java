package app.core.referenceData;

import app.core.configuration.JsonId;

public record ReferenceDataDto(@JsonId Long id, String libelle) {
}
