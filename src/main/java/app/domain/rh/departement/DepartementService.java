package app.domain.rh.departement;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
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
        if (departementRepository.existsByNom(dto.nom())) {
            throw new IllegalArgumentException("Nom already exists");
        }
        Departement departement = DepartementDto.toEntity(dto);
        Departement saved = departementRepository.save(departement);
        return DepartementDto.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<DepartementDto> lister() {
        return departementRepository.findAllByOrderByNom().stream().map(DepartementDto::toDto).toList();
    }

    public DepartementDto maj(Long id, DepartementDto dto) {
        Departement departement = departementRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Departement not found"));
        if (departementRepository.existsByNomAndIdNot(dto.nom(), id)) {
            throw new IllegalArgumentException("Nom already exists");
        }
        DepartementDto.copyToEntity(dto, departement);
        return DepartementDto.toDto(departement);
    }

    @Transactional(readOnly = true)
    public Optional<DepartementDto> recupererParId(Long id) {
        return departementRepository.findById(id).map(DepartementDto::toDto);
    }

    public void supprimer(Long id) {
        Departement departement = departementRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Departement not found"));
        departementRepository.delete(departement);
    }
}
