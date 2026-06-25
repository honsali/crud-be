package app.domain.rh.typeConge;

import jakarta.validation.constraints.NotBlank;

public record TypeCongeDto(
        Long id,
        Long idTypeConge,
        @NotBlank String libelle,
        @NotBlank String code) {

    public static TypeCongeDto toDtoAsRef(TypeConge entity) {
        return entity == null ? null
                : new TypeCongeDto(
                        entity.getId(),
                        entity.getId(),
                        entity.getLibelle(),
                        null);
    }

    public static TypeConge toEntityAsRef(TypeCongeDto dto) {
        if (dto == null || dto.id() == null) {
            return null;
        }

        TypeConge entity = new TypeConge();
        entity.setId(dto.id());
        return entity;
    }
}
