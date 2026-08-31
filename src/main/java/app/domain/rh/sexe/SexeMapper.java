package app.domain.rh.sexe;

import app.core.reference.Reference;

public final class SexeMapper {

    public static Reference toReference(Sexe sexe) {
        return new Reference(sexe.getId(), sexe.getLibelle());
    }

    private SexeMapper() {}
}
