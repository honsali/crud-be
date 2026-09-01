package app.domain.rh.conge;

import app.core.reference.Reference;
import app.domain.rh.employe.Employe;
import app.domain.rh.employe.EmployeMapper;
import app.domain.rh.typeconge.TypeConge;
import app.domain.rh.typeconge.TypeCongeMapper;

public final class CongeMapper {

    public static CongeResponse toResponse(Conge conge) {
        return new CongeResponse(
                conge.getId(),
                conge.getCode(),
                conge.getTypeConge() == null ? null : TypeCongeMapper.toReference(conge.getTypeConge()),
                conge.getDateDebutConge(),
                conge.getDateFinConge(),
                conge.getCommentaire(),
                conge.getEmploye() == null ? null : EmployeMapper.toReference(conge.getEmploye()),
                conge.getVersion());
    }

    public static Conge toEntity(CongeCreateRequest request, TypeConge typeConge, Employe employe) {
        return new Conge(
                request.code(),
                typeConge,
                request.dateDebutConge(),
                request.dateFinConge(),
                request.commentaire(),
                employe);
    }

    public static void toEntity(Conge conge, CongeUpdateRequest request, TypeConge typeConge) {
        conge.update(
                request.code(),
                typeConge,
                request.dateDebutConge(),
                request.dateFinConge(),
                request.commentaire());
    }

    public static Reference toReference(Conge conge) {
        return new Reference(conge.getId(), conge.getCode());
    }

    private CongeMapper() {}
}
