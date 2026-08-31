-- Données de référence reprises des CSV Liquibase historiques.

INSERT INTO sexe (id, libelle) VALUES
    (1, 'Masculin'),
    (2, 'Féminin');

INSERT INTO situation_familiale (id, libelle) VALUES
    (1, 'Célibataire'),
    (2, 'Marié'),
    (3, 'Divorcé'),
    (4, 'Veuf');

INSERT INTO type_conge (id, libelle) VALUES
    (1, 'Maladie'),
    (2, 'Payé');

SELECT setval(
    pg_get_serial_sequence('sexe', 'id'),
    GREATEST(COALESCE((SELECT MAX(id) FROM sexe), 0), 99),
    true
);

SELECT setval(
    pg_get_serial_sequence('situation_familiale', 'id'),
    GREATEST(COALESCE((SELECT MAX(id) FROM situation_familiale), 0), 99),
    true
);

SELECT setval(
    pg_get_serial_sequence('type_conge', 'id'),
    GREATEST(COALESCE((SELECT MAX(id) FROM type_conge), 0), 99),
    true
);
