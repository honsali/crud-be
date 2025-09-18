package app.domain.rh.departement;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DepartementService {

    private final DepartementRepository departementRepository;

    public DepartementService(DepartementRepository departementRepository) {
        this.departementRepository = departementRepository;
    }

    public DepartementDto creer(DepartementDto dto) {
        Departement departement = DepartementDto.toEntity(dto);
        Departement saved = departementRepository.save(departement);
        return DepartementDto.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<DepartementDto> lister() {
        return departementRepository.findAllByOrderByNom().stream().map(DepartementDto::toDto).collect(Collectors.toList());
    }

    public DepartementDto maj(Long id, DepartementDto dto) {
        if (!departementRepository.existsById(id)) {
            throw new IllegalArgumentException("Departement not found");
        }
        Departement departement = DepartementDto.toEntity(dto, id);
        Departement saved = departementRepository.save(departement);
        return DepartementDto.toDto(saved);
    }

    @Transactional(readOnly = true)
    public Optional<DepartementDto> recupererParId(Long id) {
        return departementRepository.findById(id).map(DepartementDto::toDto);
    }

    public void supprimer(Long id) {
        if (!departementRepository.existsById(id)) {
            throw new IllegalArgumentException("Departement not found");
        }
        departementRepository.deleteById(id);
    }
}