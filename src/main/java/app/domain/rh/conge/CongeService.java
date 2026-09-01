package app.domain.rh.conge;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.core.exception.FieldConflictException;
import app.core.exception.ResourceNotFoundException;
import app.core.exception.StaleVersionException;
import app.core.reference.Reference;
import app.domain.rh.employe.Employe;
import app.domain.rh.employe.EmployeRepository;
import app.domain.rh.typeconge.TypeConge;
import app.domain.rh.typeconge.TypeCongeRepository;

@Service
public class CongeService {

    private final CongeRepository congeRepository;
    private final TypeCongeRepository typeCongeRepository;
    private final EmployeRepository employeRepository;

    public CongeService(
            CongeRepository congeRepository,
            TypeCongeRepository typeCongeRepository,
            EmployeRepository employeRepository) {
        this.congeRepository = congeRepository;
        this.typeCongeRepository = typeCongeRepository;
        this.employeRepository = employeRepository;
    }

    @Transactional
    public CongeResponse creer(Long idEmploye, CongeCreateRequest request) {
        if (congeRepository.existsByCode(request.code())) {
            throw new FieldConflictException("Conge", "code", request.code());
        }

        TypeConge typeConge = recupererTypeConge(request.typeConge());
        Employe employe = recupererEmploye(idEmploye);
        Conge conge = CongeMapper.toEntity(request, typeConge, employe);
        Conge saved = congeRepository.saveAndFlush(conge);
        return CongeMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CongeResponse> listerParIdEmploye(Long idEmploye) {
        if (!employeRepository.existsById(idEmploye)) {
            throw new ResourceNotFoundException("Employe", idEmploye);
        }
        return congeRepository.findAllByEmployeIdOrderByCode(idEmploye).stream().map(CongeMapper::toResponse).toList();
    }

    @Transactional
    public CongeResponse maj(Long id, CongeUpdateRequest request) {
        Conge conge = congeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conge", id));
        if (conge.getVersion() != request.version()) {
            throw new StaleVersionException("Conge", id);
        }
        if (congeRepository.existsByCodeAndIdNot(request.code(), id)) {
            throw new FieldConflictException("Conge", "code", request.code());
        }

        TypeConge typeConge = recupererTypeConge(request.typeConge());
        CongeMapper.toEntity(conge, request, typeConge);
        congeRepository.flush();
        return CongeMapper.toResponse(conge);
    }

    @Transactional(readOnly = true)
    public CongeResponse recupererParId(Long id) {
        Conge conge = congeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conge", id));
        return CongeMapper.toResponse(conge);
    }

    @Transactional
    public void supprimer(Long id) {
        Conge conge = congeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conge", id));
        congeRepository.delete(conge);
        congeRepository.flush();
    }

    private TypeConge recupererTypeConge(Reference reference) {
        if (reference == null) {
            return null;
        }
        Long id = reference.id();
        return typeCongeRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TypeConge", id));
    }

    private Employe recupererEmploye(Long idEmploye) {
        return employeRepository.findById(idEmploye)
                .orElseThrow(() -> new ResourceNotFoundException("Employe", idEmploye));
    }

}
