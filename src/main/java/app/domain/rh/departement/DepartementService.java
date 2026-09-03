package app.domain.rh.departement;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.core.exception.FieldConflictException;
import app.core.exception.ResourceNotFoundException;
import app.core.exception.StaleVersionException;

@Service
public class DepartementService {

    private final DepartementRepository departementRepository;

    public DepartementService(DepartementRepository departementRepository) {
        this.departementRepository = departementRepository;
    }

    @Transactional
    public DepartementResponse creer(DepartementCreateRequest request) {
        if (departementRepository.existsByNom(request.nom())) {
            throw new FieldConflictException("Departement", "nom", request.nom());
        }
        Departement departement = DepartementMapper.toEntity(request);
        Departement saved = departementRepository.saveAndFlush(departement);
        return DepartementMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DepartementResponse> lister() {
        return departementRepository.findAllByOrderByNom().stream().map(DepartementMapper::toResponse).toList();
    }

    @Transactional
    public DepartementResponse maj(Long id, DepartementUpdateRequest request) {
        Departement departement = recupererDepartement(id);
        if (departement.getVersion() != request.version()) {
            throw new StaleVersionException("Departement", id);
        }
        if (departementRepository.existsByNomAndIdNot(request.nom(), id)) {
            throw new FieldConflictException("Departement", "nom", request.nom());
        }
        DepartementMapper.toEntity(departement, request);
        departementRepository.flush();
        return DepartementMapper.toResponse(departement);
    }

    @Transactional(readOnly = true)
    public DepartementResponse recupererParId(Long id) {
        return DepartementMapper.toResponse(recupererDepartement(id));
    }

    @Transactional
    public void supprimer(Long id) {
        Departement departement = recupererDepartement(id);
        departementRepository.delete(departement);
        departementRepository.flush();
    }

    private Departement recupererDepartement(Long id) {
        return departementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departement", id));
    }

}
