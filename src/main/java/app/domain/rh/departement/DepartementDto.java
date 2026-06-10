package app.domain.rh.departement;

import jakarta.validation.constraints.NotNull;

public record DepartementDto(
        Long id,
        Long idDepartement,
        @NotNull String nom,
        String description) {

    public static DepartementDto toDto(Departement entity) {
        return entity == null ? null : new DepartementDto(entity.getId(), entity.getId(), entity.getNom(), entity.getDescription());
    }

    public static DepartementDto toDtoAsRef(Departement entity) {
        return entity == null ? null : new DepartementDto(entity.getId(), entity.getId(), entity.getNom(), null);
    }

    public static Departement toEntity(DepartementDto dto) {
        if (dto == null) {
            return null;
        }

        Departement entity = new Departement();
        copyToEntity(dto, entity);
        return entity;
    }

    public static Departement toEntityAsRef(DepartementDto dto) {
        if (dto == null) {
            return null;
        }

        Long id = dto.id() != null ? dto.id() : dto.idDepartement();
        if (id == null) {
            return null;
        }

        Departement entity = new Departement();
        entity.setId(id);
        return entity;
    }

    public static void copyToEntity(DepartementDto dto, Departement entity) {
        entity.setNom(dto.nom());
        entity.setDescription(dto.description());
    }
}
