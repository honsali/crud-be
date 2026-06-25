package app.domain.rh.situationFamiliale;

import jakarta.validation.constraints.NotBlank;

public record SituationFamilialeDto(
        Long id,
        Long idSituationFamiliale,
        @NotBlank String libelle,
        @NotBlank String code) {

    public static SituationFamilialeDto toDtoAsRef(SituationFamiliale entity) {
        return entity == null ? null
                : new SituationFamilialeDto(
                        entity.getId(),
                        entity.getId(),
                        entity.getLibelle(),
                        null);
    }

    public static SituationFamiliale toEntityAsRef(SituationFamilialeDto dto) {
        if (dto == null || dto.id() == null) {
            return null;
        }

        SituationFamiliale entity = new SituationFamiliale();
        entity.setId(dto.id());
        return entity;
    }
}
