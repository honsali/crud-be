package app.domain.rh.employe;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.core.exception.FieldConflictException;
import app.core.exception.ResourceNotFoundException;
import app.core.exception.StaleVersionException;
import app.core.pagination.PageableUtils;
import app.core.reference.Reference;
import app.domain.rh.departement.Departement;
import app.domain.rh.departement.DepartementRepository;
import app.domain.rh.sexe.Sexe;
import app.domain.rh.sexe.SexeRepository;
import app.domain.rh.situationFamiliale.SituationFamiliale;
import app.domain.rh.situationFamiliale.SituationFamilialeRepository;

@Service
public class EmployeService {

    private final EmployeRepository employeRepository;
    private final SexeRepository sexeRepository;
    private final SituationFamilialeRepository situationFamilialeRepository;
    private final DepartementRepository departementRepository;

    public EmployeService(EmployeRepository employeRepository, SexeRepository sexeRepository, SituationFamilialeRepository situationFamilialeRepository, DepartementRepository departementRepository) {
        this.employeRepository = employeRepository;
        this.sexeRepository = sexeRepository;
        this.situationFamilialeRepository = situationFamilialeRepository;
        this.departementRepository = departementRepository;
    }

    @Transactional
    public EmployeResponse creer(EmployeCreateRequest request) {
        if (employeRepository.existsByMatricule(request.matricule())) {
            throw new FieldConflictException("Employe", "matricule", request.matricule());
        }

        Sexe sexe = recupererSexe(request.sexe());
        SituationFamiliale situationFamiliale = recupererSituationFamiliale(request.situationFamiliale());
        Departement departement = recupererDepartement(request.departement());
        Employe employe = EmployeMapper.toEntity(request, sexe, situationFamiliale, departement);
        Employe saved = employeRepository.saveAndFlush(employe);
        return EmployeMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<EmployeResponse> filtrer(EmployeFiltre filtre, Pageable pageable) {
        Pageable pagination = PageableUtils.paginationValide(pageable);
        return employeRepository.findAll(EmployeSpecification.buildSpecification(filtre), pagination).map(EmployeMapper::toResponse);
    }

    @Transactional
    public EmployeResponse maj(Long id, EmployeUpdateRequest request) {
        Employe employe = employeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Employe", id));
        if (employe.getVersion() != request.version()) {
            throw new StaleVersionException("Employe", id);
        }
        if (employeRepository.existsByMatriculeAndIdNot(request.matricule(), id)) {
            throw new FieldConflictException("Employe", "matricule", request.matricule());
        }

        Sexe sexe = recupererSexe(request.sexe());
        SituationFamiliale situationFamiliale = recupererSituationFamiliale(request.situationFamiliale());
        Departement departement = recupererDepartement(request.departement());
        EmployeMapper.toEntity(employe, request, sexe, situationFamiliale, departement);
        employeRepository.flush();
        return EmployeMapper.toResponse(employe);
    }

    @Transactional(readOnly = true)
    public EmployeResponse recupererParId(Long id) {
        Employe employe = employeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Employe", id));
        return EmployeMapper.toResponse(employe);
    }

    @Transactional
    public void supprimer(Long id) {
        Employe employe = employeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Employe", id));
        employeRepository.delete(employe);
        employeRepository.flush();
    }

    private Sexe recupererSexe(Reference reference) {
        if (reference == null) {
            return null;
        }
        Long id = reference.id();
        return sexeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sexe", id));
    }

    private SituationFamiliale recupererSituationFamiliale(Reference reference) {
        if (reference == null) {
            return null;
        }
        Long id = reference.id();
        return situationFamilialeRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SituationFamiliale", id));
    }

    private Departement recupererDepartement(Reference reference) {
        if (reference == null) {
            return null;
        }
        Long id = reference.id();
        return departementRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departement", id));
    }

}
