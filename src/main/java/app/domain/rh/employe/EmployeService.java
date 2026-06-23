package app.domain.rh.employe;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmployeService {

    private final EmployeRepository employeRepository;

    public EmployeService(EmployeRepository employeRepository) {
        this.employeRepository = employeRepository;
    }

    public EmployeDto creer(EmployeDto dto) {
        verifierMatriculeDisponible(dto.matricule());
        Employe employe = EmployeDto.toEntity(dto);
        Employe saved = employeRepository.save(employe);
        return EmployeDto.toDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<EmployeDto> filtrer(EmployeFiltre filtre, Pageable pageable) {
        if (filtre != null) {
            filtre.valider();
        }
        return employeRepository.findAll(EmployeSpecification.buildSpecification(filtre), pageable).map(EmployeDto::toDto);
    }

    public EmployeDto maj(Long id, EmployeDto dto) {
        Employe employe = employeRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Employe not found"));
        verifierMatriculeDisponible(id, dto.matricule());
        EmployeDto.copyToEntity(dto, employe);
        return EmployeDto.toDto(employe);
    }

    @Transactional(readOnly = true)
    public Optional<EmployeDto> recupererParId(Long id) {
        return employeRepository.findById(id).map(EmployeDto::toDto);
    }

    public void supprimer(Long id) {
        Employe employe = employeRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Employe not found"));
        employeRepository.delete(employe);
    }

    private void verifierMatriculeDisponible(String matricule) {
        if (employeRepository.existsByMatricule(matricule)) {
            throw new IllegalArgumentException("Matricule already exists");
        }
    }

    private void verifierMatriculeDisponible(Long id, String matricule) {
        if (employeRepository.existsByMatriculeAndIdNot(matricule, id)) {
            throw new IllegalArgumentException("Matricule already exists");
        }
    }
}
