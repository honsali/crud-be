package app.domain.rh.typeConge;

import app.core.reference.Reference;

public final class TypeCongeMapper {

    public static Reference toReference(TypeConge typeConge) {
        return new Reference(typeConge.getId(), typeConge.getLibelle());
    }

    private TypeCongeMapper() {}
}
