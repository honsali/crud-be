package app.domain.rh.departement;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.core.exception.ConflictException;

@Service
@Transactional
public class DepartementService {

    private final DepartementRepository departementRepository;
    private final DepartementMapper departementMapper;

    public DepartementService(DepartementRepository departementRepository, DepartementMapper departementMapper) {
        this.departementRepository = departementRepository;
        this.departementMapper = departementMapper;
    }

    public DepartementDto creer(DepartementDto dto) {
        if (departementRepository.existsByNom(dto.nom())) {
            throw new ConflictException("Nom already exists");
        }
        Departement departement = departementMapper.toEntity(dto);
        Departement saved = departementRepository.save(departement);
        return departementMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<DepartementDto> lister() {
        return departementRepository.findAllByOrderByNom().stream().map(departementMapper::toDto).toList();
    }

    public DepartementDto maj(Long id, DepartementDto dto) {
        Departement departement = departementRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Departement not found"));
        if (departementRepository.existsByNomAndIdNot(dto.nom(), id)) {
            throw new ConflictException("Nom already exists");
        }
        departementMapper.copyToEntity(dto, departement);
        return departementMapper.toDto(departement);
    }

    @Transactional(readOnly = true)
    public Optional<DepartementDto> recupererParId(Long id) {
        return departementRepository.findById(id).map(departementMapper::toDto);
    }

    public void supprimer(Long id) {
        Departement departement = departementRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Departement not found"));
        departementRepository.delete(departement);
    }
}
