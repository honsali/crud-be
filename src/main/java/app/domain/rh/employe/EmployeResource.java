package app.domain.rh.employe;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import app.domain.rh.departement.Departement;
import app.domain.rh.departement.DepartementRepository;

@RestController
@RequestMapping("/api/employe")
@Transactional
public class EmployeResource {

    private final EmployeRepository employeRepository;
    private final DepartementRepository departementRepository;

    public EmployeResource(EmployeRepository employeRepository, DepartementRepository departementRepository) {
        this.employeRepository = employeRepository;
        this.departementRepository = departementRepository;
    }
}