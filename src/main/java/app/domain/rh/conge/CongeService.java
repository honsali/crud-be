package app.domain.rh.conge;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.core.exception.ConflictException;
import app.domain.rh.employe.Employe;
import app.domain.rh.employe.EmployeRepository;

@Service
@Transactional
public class CongeService {

    private final CongeRepository congeRepository;
    private final EmployeRepository employeRepository;
    private final CongeMapper congeMapper;

    public CongeService(CongeRepository congeRepository, EmployeRepository employeRepository, CongeMapper congeMapper) {
        this.congeRepository = congeRepository;
        this.employeRepository = employeRepository;
        this.congeMapper = congeMapper;
    }

    public CongeDto creer(Long idEmploye, CongeDto dto) {
        validate(dto);
        if (congeRepository.existsByCode(dto.code())) {
            throw new ConflictException("Code already exists");
        }
        Employe employe = employeRepository.findById(idEmploye).orElseThrow(() -> new NoSuchElementException("Employe not found"));
        Conge conge = congeMapper.toEntity(dto);
        conge.setEmploye(employe);
        Conge saved = congeRepository.save(conge);
        return congeMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<CongeDto> listerParIdEmploye(Long idEmploye) {
        if (!employeRepository.existsById(idEmploye)) {
            throw new NoSuchElementException("Employe not found");
        }
        return congeRepository.findAllByEmploye_IdOrderByCode(idEmploye).stream().map(congeMapper::toDto).toList();
    }

    public CongeDto maj(Long id, CongeDto dto) {
        validate(dto);
        Conge conge = congeRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Conge not found"));
        if (congeRepository.existsByCodeAndIdNot(dto.code(), id)) {
            throw new ConflictException("Code already exists");
        }
        congeMapper.copyToEntity(dto, conge);
        return congeMapper.toDto(conge);
    }

    @Transactional(readOnly = true)
    public Optional<CongeDto> recupererParId(Long id) {
        return congeRepository.findById(id).map(congeMapper::toDto);
    }

    public void supprimer(Long id) {
        Conge conge = congeRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Conge not found"));
        congeRepository.delete(conge);
    }

    private static void validate(CongeDto dto) {
        if (dto.dateDebutConge() != null && dto.dateFinConge() != null
                && dto.dateFinConge().isBefore(dto.dateDebutConge())) {
            throw new IllegalArgumentException("dateFinConge must not be before dateDebutConge");
        }
    }
}
