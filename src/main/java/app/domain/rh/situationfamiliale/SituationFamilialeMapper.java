package app.domain.rh.situationfamiliale;

import app.core.reference.Reference;

public final class SituationFamilialeMapper {

    public static Reference toReference(SituationFamiliale situationFamiliale) {
        return new Reference(situationFamiliale.getId(), situationFamiliale.getLibelle());
    }

    private SituationFamilialeMapper() {}
}
