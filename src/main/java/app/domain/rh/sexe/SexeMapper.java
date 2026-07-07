package app.domain.rh.sexe;

import java.util.NoSuchElementException;
import org.springframework.stereotype.Component;

@Component
public class SexeMapper {

    private final SexeRepository sexeRepository;

    public SexeMapper(SexeRepository sexeRepository) {
        this.sexeRepository = sexeRepository;
    }

    public SexeDto toDtoAsRef(Sexe entity) {
        return entity == null ? null
                : new SexeDto(
                        entity.getId(),
                        entity.getId(),
                        entity.getLibelle(),
                        null);
    }

    public Sexe toEntityAsRef(SexeDto dto) {
        if (dto == null || dto.id() == null) {
            return null;
        }
        return sexeRepository.findById(dto.id()).orElseThrow(() -> new NoSuchElementException("Sexe not found"));
    }
}
