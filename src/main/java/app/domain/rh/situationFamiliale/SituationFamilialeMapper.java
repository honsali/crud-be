package app.domain.rh.situationFamiliale;

import java.util.NoSuchElementException;
import org.springframework.stereotype.Component;

@Component
public class SituationFamilialeMapper {

    private final SituationFamilialeRepository situationFamilialeRepository;

    public SituationFamilialeMapper(SituationFamilialeRepository situationFamilialeRepository) {
        this.situationFamilialeRepository = situationFamilialeRepository;
    }

    public SituationFamilialeDto toDtoAsRef(SituationFamiliale entity) {
        return entity == null ? null
                : new SituationFamilialeDto(
                        entity.getId(),
                        entity.getId(),
                        entity.getLibelle());
    }

    public SituationFamiliale toEntityAsRef(SituationFamilialeDto dto) {
        if (dto == null || dto.id() == null) {
            return null;
        }
        return situationFamilialeRepository.findById(dto.id()).orElseThrow(() -> new NoSuchElementException("SituationFamiliale not found"));
    }
}
