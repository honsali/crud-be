package app.domain.rh.conge;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.domain.rh.employe.Employe;
import app.domain.rh.employe.EmployeRepository;

@Service
@Transactional
public class CongeService {

    private final CongeRepository congeRepository;
    private final EmployeRepository employeRepository;

    public CongeService(CongeRepository congeRepository, EmployeRepository employeRepository) {
        this.congeRepository = congeRepository;
        this.employeRepository = employeRepository;
    }

    public CongeDto creer(Long idEmploye, CongeDto dto) {
        if (dto != null && dto.employe() != null && dto.employe().id() != null && !dto.employe().id().equals(idEmploye)) {
            throw new IllegalArgumentException("Employe ID mismatch");
        }
        Employe employe = employeRepository.findById(idEmploye).orElseThrow(() -> new java.util.NoSuchElementException("Employe not found"));
        Conge conge = CongeDto.toEntity(dto);
        conge.setEmploye(employe);
        Conge saved = congeRepository.save(conge);
        return CongeDto.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<CongeDto> listerParIdEmploye(Long idEmploye) {
        if (!employeRepository.existsById(idEmploye)) {
            throw new IllegalArgumentException("Employe not found");
        }
        return congeRepository.findAllByEmploye_IdOrderByCode(idEmploye).stream().map(CongeDto::toDto).collect(Collectors.toList());
    }

    public CongeDto maj(Long id, CongeDto dto) {
        if (!congeRepository.existsById(id)) {
            throw new IllegalArgumentException("Conge not found");
        }
        Conge conge = CongeDto.toEntity(dto, id);
        Conge saved = congeRepository.save(conge);
        return CongeDto.toDto(saved);
    }

    @Transactional(readOnly = true)
    public Optional<CongeDto> recupererParId(Long id) {
        return congeRepository.findById(id).map(CongeDto::toDto);
    }
}
