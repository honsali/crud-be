package app.domain.rh.employe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import app.domain.rh.departement.Departement;
import app.domain.rh.sexe.Sexe;
import app.domain.rh.situationfamiliale.SituationFamiliale;

class EmployeDslFieldsTest {

    private final Sexe sexe = mock(Sexe.class);
    private final SituationFamiliale situationFamiliale = mock(SituationFamiliale.class);
    private final Departement departement = mock(Departement.class);

    @Test
    void creationPreservesEveryValueDefinedByTheDsl() {
        LocalDate dateNaissance = LocalDate.of(1990, 5, 12);
        LocalDate dateEntree = LocalDate.of(2020, 9, 1);

        Employe employe = new Employe(
                "M-001",
                "Martin",
                "Alice",
                dateNaissance,
                sexe,
                situationFamiliale,
                dateEntree,
                "Mixed.Case@Example.COM",
                "0600000000",
                "Rabat",
                "10 rue des Fleurs",
                "Analyste",
                "Description complète",
                departement);

        assertThat(employe.getMatricule()).isEqualTo("M-001");
        assertThat(employe.getNom()).isEqualTo("Martin");
        assertThat(employe.getPrenom()).isEqualTo("Alice");
        assertThat(employe.getDateNaissance()).isEqualTo(dateNaissance);
        assertThat(employe.getSexe()).isSameAs(sexe);
        assertThat(employe.getSituationFamiliale()).isSameAs(situationFamiliale);
        assertThat(employe.getDateEntree()).isEqualTo(dateEntree);
        assertThat(employe.getEmail()).isEqualTo("Mixed.Case@Example.COM");
        assertThat(employe.getTelephone()).isEqualTo("0600000000");
        assertThat(employe.getVille()).isEqualTo("Rabat");
        assertThat(employe.getAdresse()).isEqualTo("10 rue des Fleurs");
        assertThat(employe.getFonction()).isEqualTo("Analyste");
        assertThat(employe.getDescription()).isEqualTo("Description complète");
        assertThat(employe.getDepartement()).isSameAs(departement);
    }

    @Test
    void updateCanClearEveryOptionalValueWithoutAlteringRequiredValues() {
        Employe employe = new Employe(
                "M-001", "Martin", "Alice", LocalDate.of(1990, 5, 12),
                sexe, situationFamiliale, LocalDate.of(2020, 9, 1),
                "email@example.test", "0600000000", "Rabat", "Adresse",
                "Analyste", "Description", departement);

        LocalDate nouvelleDateNaissance = LocalDate.of(1991, 6, 13);
        employe.update(
                "M-002", "Durand", "Alicia", nouvelleDateNaissance,
                null, null, null, null, null, null, null, null, null, null);

        assertThat(employe.getMatricule()).isEqualTo("M-002");
        assertThat(employe.getNom()).isEqualTo("Durand");
        assertThat(employe.getPrenom()).isEqualTo("Alicia");
        assertThat(employe.getDateNaissance()).isEqualTo(nouvelleDateNaissance);
        assertThat(employe.getSexe()).isNull();
        assertThat(employe.getSituationFamiliale()).isNull();
        assertThat(employe.getDateEntree()).isNull();
        assertThat(employe.getEmail()).isNull();
        assertThat(employe.getTelephone()).isNull();
        assertThat(employe.getVille()).isNull();
        assertThat(employe.getAdresse()).isNull();
        assertThat(employe.getFonction()).isNull();
        assertThat(employe.getDescription()).isNull();
        assertThat(employe.getDepartement()).isNull();
    }

    @Test
    void plainTextEmailIsNotImplicitlyNormalizedByTheDomain() {
        EmployeCreateRequest request = new EmployeCreateRequest(
                "M-003", "Martin", "Alice", LocalDate.of(1990, 5, 12),
                null, null, null, "   ", null, null, null, null, null, null);
        Employe employe = new Employe(
                request.matricule(), request.nom(), request.prenom(), request.dateNaissance(),
                null, null, null, request.email(), null, null, null, null, null, null);

        assertThat(request.email()).isEqualTo("   ");
        assertThat(employe.getEmail()).isEqualTo("   ");
    }
}
