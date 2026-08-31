package app.domain.rh.departement;

import app.core.reference.Reference;

public final class DepartementMapper {

    public static DepartementResponse toResponse(Departement departement) {
        return new DepartementResponse(
                departement.getId(),
                departement.getNom(),
                departement.getDescription(),
                departement.getVersion());
    }

    public static Departement toEntity(DepartementCreateRequest request) {
        return new Departement(
                request.nom(),
                request.description());
    }

    public static void toEntity(Departement departement, DepartementUpdateRequest request) {
        departement.update(
                request.nom(),
                request.description());
    }

    public static Reference toReference(Departement departement) {
        return new Reference(departement.getId(), departement.getNom());
    }

    private DepartementMapper() {}
}
