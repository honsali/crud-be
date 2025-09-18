package app.domain.rh.departement;

import jakarta.validation.constraints.NotNull;

public record DepartementDto(//
        Long id, //
        Long idDepartement, //
        String libelle, //
        @NotNull
        String nom, //
        String description //
) {

    public static DepartementDto toDto(Departement entity) {
        return entity == null ? null
                : new DepartementDto(//
                        entity.getId(), //
                        entity.getId(), //
                        entity.getDisplayString(), //
                        entity.getNom(), //
                        entity.getDescription() //
                );
    }

    public static DepartementDto toDtoAsRef(Departement entity) {
        return entity == null ? null
                : new DepartementDto(//
                        entity.getId(), //
                        entity.getId(), //
                        entity.getDisplayString(), //
                        entity.getNom(), //
                        null //
                );
    }

    public static Departement toEntity(DepartementDto dto) {
        if (dto == null) {
            return null;
        }

        Departement entity = new Departement();
        entity.setNom(dto.nom());
        entity.setDescription(dto.description());
        return entity;
    }

    public static Departement toEntity(DepartementDto dto, Long id) {
        Departement entity = toEntity(dto);
        if (entity != null) {
            entity.setId(id);
        }
        return entity;
    }

    public static Departement toEntityAsRef(DepartementDto dto) {
        if (dto == null || dto.id() == null) {
            return null;
        }

        Departement entity = new Departement();
        entity.setId(dto.id());
        return entity;
    }
}