package app.domain.rh.typeConge;

import jakarta.validation.constraints.NotNull;

public record TypeCongeDto(
        Long id,
        Long idTypeConge,
        @NotNull String libelle,
        String code) {

    public static TypeCongeDto toDtoAsRef(TypeConge entity) {
        return entity == null ? null : new TypeCongeDto(entity.getId(), entity.getId(), entity.getLibelle(), null);
    }

    public static TypeConge toEntityAsRef(TypeCongeDto dto) {
        if (dto == null) {
            return null;
        }

        Long id = dto.id() != null ? dto.id() : dto.idTypeConge();
        if (id == null) {
            return null;
        }

        TypeConge entity = new TypeConge();
        entity.setId(id);
        return entity;
    }
}
