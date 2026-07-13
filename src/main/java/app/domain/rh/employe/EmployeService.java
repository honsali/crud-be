package app.domain.rh.employe;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.core.ConflictException;
import app.core.PageableUtils;

@Service
@Transactional
public class EmployeService {

    private final EmployeRepository employeRepository;
    private final EmployeMapper employeMapper;

    public EmployeService(EmployeRepository employeRepository, EmployeMapper employeMapper) {
        this.employeRepository = employeRepository;
        this.employeMapper = employeMapper;
    }

    public EmployeDto creer(EmployeDto dto) {
        if (employeRepository.existsByMatricule(dto.matricule())) {
            throw new ConflictException("Matricule already exists");
        }
        Employe employe = employeMapper.toEntity(dto);
        Employe saved = employeRepository.save(employe);
        return employeMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<EmployeDto> filtrer(EmployeFiltre filtre, Pageable pageable) {
        validate(filtre);
        return employeRepository.findAll(EmployeSpecification.buildSpecification(filtre), PageableUtils.avecTriStable(pageable)).map(employeMapper::toDto);
    }

    public EmployeDto maj(Long id, EmployeDto dto) {
        Employe employe = employeRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Employe not found"));
        if (employeRepository.existsByMatriculeAndIdNot(dto.matricule(), id)) {
            throw new ConflictException("Matricule already exists");
        }
        employeMapper.copyToEntity(dto, employe);
        return employeMapper.toDto(employe);
    }

    @Transactional(readOnly = true)
    public Optional<EmployeDto> recupererParId(Long id) {
        return employeRepository.findById(id).map(employeMapper::toDto);
    }

    public void supprimer(Long id) {
        Employe employe = employeRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Employe not found"));
        employeRepository.delete(employe);
    }

    private static void validate(EmployeFiltre filtre) {
        if (filtre == null) {
            return;
        }
        if (filtre.debutDateNaissance() != null && filtre.finDateNaissance() != null
                && filtre.finDateNaissance().isBefore(filtre.debutDateNaissance())) {
            throw new IllegalArgumentException("finDateNaissance must not be before debutDateNaissance");
        }
        if (filtre.debutDateEntree() != null && filtre.finDateEntree() != null
                && filtre.finDateEntree().isBefore(filtre.debutDateEntree())) {
            throw new IllegalArgumentException("finDateEntree must not be before debutDateEntree");
        }
    }
}
