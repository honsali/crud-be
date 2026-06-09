package app.domain.rh.conge;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.domain.rh.employe.Employe;
import app.domain.rh.employe.EmployeDto;
import app.domain.rh.employe.EmployeRepository;
import app.domain.rh.typeConge.TypeConge;
import app.domain.rh.typeConge.TypeCongeDto;
import jakarta.persistence.EntityManager;

@Service
@Transactional
public class CongeService {

    private final CongeRepository congeRepository;
    private final EmployeRepository employeRepository;
    private final EntityManager entityManager;

    public CongeService(CongeRepository congeRepository, EmployeRepository employeRepository, EntityManager entityManager) {
        this.congeRepository = congeRepository;
        this.employeRepository = employeRepository;
        this.entityManager = entityManager;
    }

    public CongeDto creer(Long idEmploye, CongeDto dto) {
        if (dto != null && dto.employe() != null && dto.employe().id() != null && !dto.employe().id().equals(idEmploye)) {
            throw new IllegalArgumentException("Employe ID mismatch");
        }

        Employe employe = employeRepository.findById(idEmploye).orElseThrow(() -> new NoSuchElementException("Employe not found"));
        Conge conge = new Conge();
        copyToManagedEntity(dto, conge);
        conge.setEmploye(employe);
        Conge saved = congeRepository.save(conge);
        return CongeDto.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<CongeDto> listerParIdEmploye(Long idEmploye) {
        if (!employeRepository.existsById(idEmploye)) {
            throw new NoSuchElementException("Employe not found");
        }
        return congeRepository.findAllByEmploye_IdOrderByCode(idEmploye).stream().map(CongeDto::toDto).toList();
    }

    public CongeDto maj(Long id, CongeDto dto) {
        Conge conge = congeRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Conge not found"));
        Employe employe = conge.getEmploye();
        copyToManagedEntity(dto, conge);
        if (dto.employe() == null) {
            conge.setEmploye(employe);
        }
        return CongeDto.toDto(conge);
    }

    @Transactional(readOnly = true)
    public Optional<CongeDto> recupererParId(Long id) {
        return congeRepository.findById(id).map(CongeDto::toDto);
    }

    public void supprimer(Long id) {
        if (!congeRepository.existsById(id)) {
            throw new NoSuchElementException("Conge not found");
        }
        congeRepository.deleteById(id);
    }

    private void copyToManagedEntity(CongeDto dto, Conge entity) {
        CongeDto.copyToEntity(dto, entity);
        TypeConge typeConge = TypeCongeDto.toEntityAsRef(dto.typeConge());
        Employe employe = EmployeDto.toEntityAsRef(dto.employe());

        entity.setTypeConge(reference(TypeConge.class, typeConge == null ? null : typeConge.getId(), "Type conge not found"));
        entity.setEmploye(reference(Employe.class, employe == null ? null : employe.getId(), "Employe not found"));
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
