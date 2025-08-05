package app.domain.rh.departement;


public record DepartementDto(//
        Long id, //
        Long idDepartement, //
        String libelle, //
        String nom, //
        String description //
) {

    public static DepartementDto asEntity(Departement entity) {
        return entity == null ? null
                : new DepartementDto(//
                        entity.getId(), //
                        entity.getId(), //
                        entity.getDisplayString(), //
                        entity.getNom(), //
                        entity.getDescription() //
                );
    }

    public static DepartementDto asRef(Departement entity) {
        return entity == null ? null
                : new DepartementDto(//
                        entity.getId(), //
                        entity.getId(), //
                        entity.getDisplayString(), //
                        entity.getNom(), //
                        null //
                );
    }
}