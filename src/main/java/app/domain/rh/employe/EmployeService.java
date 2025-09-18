package app.domain.rh.employe;

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
        Employe employe = EmployeDto.toEntity(dto);
        Employe saved = employeRepository.save(employe);
        return EmployeDto.toDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<EmployeDto> filtrer(EmployeFiltre filtre, Pageable pageable) {
        return employeRepository.findAll(EmployeSpecification.buildSpecification(filtre), pageable).map(EmployeDto::toDto);
    }

    public EmployeDto maj(Long id, EmployeDto dto) {
        if (!employeRepository.existsById(id)) {
            throw new IllegalArgumentException("Employe not found");
        }
        Employe employe = EmployeDto.toEntity(dto, id);
        Employe saved = employeRepository.save(employe);
        return EmployeDto.toDto(saved);
    }

    @Transactional(readOnly = true)
    public Optional<EmployeDto> recupererParId(Long id) {
        return employeRepository.findById(id).map(EmployeDto::toDto);
    }

    public void supprimer(Long id) {
        if (!employeRepository.existsById(id)) {
            throw new IllegalArgumentException("Employe not found");
        }
        employeRepository.deleteById(id);
    }
}