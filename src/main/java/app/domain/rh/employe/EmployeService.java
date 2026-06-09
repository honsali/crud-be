package app.domain.rh.employe;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.domain.rh.departement.Departement;
import app.domain.rh.departement.DepartementDto;
import app.domain.rh.sexe.Sexe;
import app.domain.rh.sexe.SexeDto;
import app.domain.rh.situationFamiliale.SituationFamiliale;
import app.domain.rh.situationFamiliale.SituationFamilialeDto;
import jakarta.persistence.EntityManager;

@Service
@Transactional
public class EmployeService {

    private final EmployeRepository employeRepository;
    private final EntityManager entityManager;

    public EmployeService(EmployeRepository employeRepository, EntityManager entityManager) {
        this.employeRepository = employeRepository;
        this.entityManager = entityManager;
    }

    public EmployeDto creer(EmployeDto dto) {
        Employe employe = new Employe();
        copyToManagedEntity(dto, employe);
        Employe saved = employeRepository.save(employe);
        return EmployeDto.toDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<EmployeDto> filtrer(EmployeFiltre filtre, Pageable pageable) {
        return employeRepository.findAll(EmployeSpecification.buildSpecification(filtre), pageable).map(EmployeDto::toDto);
    }

    public EmployeDto maj(Long id, EmployeDto dto) {
        Employe employe = employeRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Employe not found"));
        copyToManagedEntity(dto, employe);
        return EmployeDto.toDto(employe);
    }

    @Transactional(readOnly = true)
    public Optional<EmployeDto> recupererParId(Long id) {
        return employeRepository.findById(id).map(EmployeDto::toDto);
    }

    public void supprimer(Long id) {
        if (!employeRepository.existsById(id)) {
            throw new NoSuchElementException("Employe not found");
        }
        employeRepository.deleteById(id);
    }

    private void copyToManagedEntity(EmployeDto dto, Employe entity) {
        EmployeDto.copyToEntity(dto, entity);
        Sexe sexe = SexeDto.toEntityAsRef(dto.sexe());
        SituationFamiliale situationFamiliale = SituationFamilialeDto.toEntityAsRef(dto.situationFamiliale());
        Departement departement = DepartementDto.toEntityAsRef(dto.departement());

        entity.setSexe(reference(Sexe.class, sexe == null ? null : sexe.getId(), "Sexe not found"));
        entity.setSituationFamiliale(reference(SituationFamiliale.class, situationFamiliale == null ? null : situationFamiliale.getId(), "Situation familiale not found"));
        entity.setDepartement(reference(Departement.class, departement == null ? null : departement.getId(), "Departement not found"));
    }

    private <T> T reference(Class<T> type, Long id, String notFoundMessage) {
        if (id == null) {
            return null;
        }
        T entity = entityManager.find(type, id);
        if (entity == null) {
            throw new NoSuchElementException(notFoundMessage);
        }
        return entity;
    }
}
