-- Données de démonstration reprises des CSV Liquibase historiques.
-- conge.csv ne contient aucune ligne de données.
-- Les comptes historiques sont adaptés au modèle de sécurité actuel et à Argon2id.

INSERT INTO app_role (id, code, libelle, description) VALUES
    (1, 'ADMIN', 'Administrateur', 'Administration des rôles et des comptes'),
    (2, 'GESTIONNAIRE_RH', 'Gestionnaire RH', 'Gestion des données des ressources humaines');

INSERT INTO account (id, username, display_name, email, active, role_id) VALUES
    (1, 'admin', 'Administrateur local', NULL, true, 1),
    (2, 'gestionnaire-rh', 'Gestionnaire RH local', NULL, true, 2);

INSERT INTO account_credential (
    id,
    account_id,
    password_hash,
    token_version,
    password_changed_at
) VALUES
    (
        1,
        1,
        '{argon2id}$argon2id$v=19$m=19456,t=2,p=1$34dR01AOneq8vQS8Nh0Z9w$5RcUCJvE9iOaa3a354oNAwTmJgWuXz+n/8o8k0yYGcA',
        0,
        TIMESTAMPTZ '2026-01-01 00:00:00+00'
    ),
    (
        2,
        2,
        '{argon2id}$argon2id$v=19$m=19456,t=2,p=1$cfyvBA2Cgk2pexTQuhBk5g$5nCzbv4HfXoO7H/9Zgn3l2XnpvWiPVZObJJgSX9OJKM',
        0,
        TIMESTAMPTZ '2026-01-01 00:00:00+00'
    );

INSERT INTO departement (id, nom, description) VALUES
    (1, 'Ressources Humaines', 'Gestion du personnel et des talents'),
    (2, 'Informatique', 'Développement et maintenance des systèmes informatiques'),
    (3, 'Comptabilité', 'Gestion financière et comptable'),
    (4, 'Commercial', 'Vente et relation client'),
    (5, 'Marketing', 'Communication et promotion'),
    (6, 'Production', 'Fabrication et contrôle qualité'),
    (7, 'Logistique', 'Gestion des stocks et transport'),
    (8, 'Recherche & Développement', 'Innovation et développement produits'),
    (9, 'Juridique', 'Affaires légales et conformité'),
    (10, 'Direction Générale', 'Management stratégique');

INSERT INTO employe (
    id,
    matricule,
    nom,
    prenom,
    date_naissance,
    sexe_id,
    situation_familiale_id,
    date_entree,
    email,
    telephone,
    ville,
    adresse,
    fonction,
    description,
    departement_id
) VALUES
    (1, 'EMP001', 'Durand', 'Pierre', DATE '1985-03-15', 1, 2, DATE '2020-01-15', 'pierre.durand@entreprise.fr', '+33123456789', 'Paris', '15 rue de la Paix', 'Développeur Senior', 'Développement applications Java', 2),
    (2, 'EMP002', 'Martin', 'Sophie', DATE '1990-07-22', 2, 1, DATE '2021-05-10', 'sophie.martin@entreprise.fr', '+33234567890', 'Lyon', '8 avenue des Fleurs', 'Responsable RH', 'Gestion du personnel', 1),
    (3, 'EMP003', 'Bernard', 'Jean', DATE '1982-11-08', 1, 2, DATE '2019-09-03', 'jean.bernard@entreprise.fr', '+33345678901', 'Marseille', '22 boulevard Victor Hugo', 'Comptable', 'Gestion comptable', 3),
    (4, 'EMP004', 'Petit', 'Marie', DATE '1988-04-12', 2, 3, DATE '2022-02-28', 'marie.petit@entreprise.fr', '+33456789012', 'Toulouse', '5 place du Capitole', 'Commercial Senior', 'Vente B2B', 4),
    (5, 'EMP005', 'Robert', 'Paul', DATE '1975-12-30', 1, 2, DATE '2018-03-20', 'paul.robert@entreprise.fr', '+33567890123', 'Nice', '12 promenade des Anglais', 'Directeur Marketing', 'Stratégie marketing', 5),
    (6, 'EMP006', 'Richard', 'Claire', DATE '1992-09-18', 2, 1, DATE '2023-01-09', 'claire.richard@entreprise.fr', '+33678901234', 'Bordeaux', '18 cours de l''Intendance', 'Ingénieur Qualité', 'Contrôle qualité production', 6),
    (7, 'EMP007', 'Moreau', 'François', DATE '1987-06-25', 1, 2, DATE '2020-11-15', 'francois.moreau@entreprise.fr', '+33789012345', 'Nantes', '7 rue Crébillon', 'Responsable Logistique', 'Gestion des stocks', 7),
    (8, 'EMP008', 'Simon', 'Anne', DATE '1991-02-14', 2, 1, DATE '2021-08-22', 'anne.simon@entreprise.fr', '+33890123456', 'Strasbourg', '25 rue des Serruriers', 'Chercheur', 'Innovation produits', 8),
    (9, 'EMP009', 'Laurent', 'Michel', DATE '1984-10-05', 1, 2, DATE '2019-12-01', 'michel.laurent@entreprise.fr', '+33901234567', 'Lille', '14 rue Nationale', 'Juriste', 'Contrats et conformité', 9),
    (10, 'EMP010', 'Leroy', 'Isabelle', DATE '1979-08-17', 2, 2, DATE '2017-06-12', 'isabelle.leroy@entreprise.fr', '+33012345678', 'Rennes', '9 place de la Mairie', 'Directrice Générale', 'Management stratégique', 10),
    (11, 'EMP011', 'Roux', 'Thomas', DATE '1989-01-28', 1, 1, DATE '2022-07-04', 'thomas.roux@entreprise.fr', '+33123456790', 'Paris', '45 avenue Montaigne', 'Développeur Front-end', 'Interface utilisateur', 2),
    (12, 'EMP012', 'Garcia', 'Laura', DATE '1993-05-09', 2, 1, DATE '2023-03-15', 'laura.garcia@entreprise.fr', '+33234567801', 'Lyon', '33 rue de la République', 'Assistante RH', 'Support administratif', 1),
    (13, 'EMP013', 'Gonzalez', 'David', DATE '1986-12-03', 1, 2, DATE '2020-04-20', 'david.gonzalez@entreprise.fr', '+33345678912', 'Marseille', '17 canebière', 'Analyste Financier', 'Analyse budgétaire', 3),
    (14, 'EMP014', 'Rodriguez', 'Emma', DATE '1991-09-11', 2, 1, DATE '2022-09-05', 'emma.rodriguez@entreprise.fr', '+33456789023', 'Toulouse', '28 rue Alsace Lorraine', 'Chargée de Clientèle', 'Relation client', 4),
    (15, 'EMP015', 'Lopez', 'Julien', DATE '1983-07-19', 1, 2, DATE '2019-01-28', 'julien.lopez@entreprise.fr', '+33567890134', 'Nice', '6 rue de France', 'Chef de Produit', 'Développement produits', 5),
    (16, 'EMP016', 'Dubois', 'Camille', DATE '1994-03-08', 2, 1, DATE '2023-06-12', 'camille.dubois@entreprise.fr', '+33678901245', 'Montpellier', '11 rue Jean Jaurès', 'Développeur Junior', 'Développement web', 2),
    (17, 'EMP017', 'Fournier', 'Alexandre', DATE '1981-11-14', 1, 4, DATE '2018-08-07', 'alexandre.fournier@entreprise.fr', '+33789012356', 'Grenoble', '29 cours Jean Jaurès', 'Contrôleur de Gestion', 'Analyse financière', 3),
    (18, 'EMP018', 'Girard', 'Océane', DATE '1990-06-29', 2, 2, DATE '2021-10-18', 'oceane.girard@entreprise.fr', '+33890123467', 'Angers', '16 place du Ralliement', 'Attachée Commercial', 'Prospection commerciale', 4),
    (19, 'EMP019', 'Morel', 'Sébastien', DATE '1987-02-03', 1, 1, DATE '2020-12-14', 'sebastien.morel@entreprise.fr', '+33901234578', 'Clermont-Ferrand', '23 avenue de la République', 'Community Manager', 'Communication digitale', 5),
    (20, 'EMP020', 'Lefebvre', 'Céline', DATE '1985-09-21', 2, 2, DATE '2019-05-25', 'celine.lefebvre@entreprise.fr', '+33012345689', 'Le Havre', '8 boulevard François 1er', 'Technicienne Production', 'Contrôle technique', 6),
    (21, 'EMP021', 'Michel', 'Kevin', DATE '1992-12-16', 1, 1, DATE '2022-11-28', 'kevin.michel@entreprise.fr', '+33123456701', 'Reims', '20 rue de Vesle', 'Magasinier', 'Gestion entrepôt', 7),
    (22, 'EMP022', 'Mercier', 'Manon', DATE '1988-04-07', 2, 3, DATE '2020-07-06', 'manon.mercier@entreprise.fr', '+33234567812', 'Dijon', '13 rue des Forges', 'Ingénieur R&D', 'Développement produits', 8),
    (23, 'EMP023', 'Blanc', 'Maxime', DATE '1993-10-25', 1, 1, DATE '2023-02-20', 'maxime.blanc@entreprise.fr', '+33345678923', 'Tours', '19 rue Nationale', 'Stagiaire Juridique', 'Assistance juridique', 9),
    (24, 'EMP024', 'Guerin', 'Nathalie', DATE '1980-01-12', 2, 2, DATE '2017-11-30', 'nathalie.guerin@entreprise.fr', '+33456789034', 'Caen', '4 rue Saint-Pierre', 'Secrétaire Direction', 'Assistance direction', 10),
    (25, 'EMP025', 'Boyer', 'Romain', DATE '1986-08-04', 1, 2, DATE '2019-04-15', 'romain.boyer@entreprise.fr', '+33567890145', 'Orléans', '12 rue Jeanne d''Arc', 'Administrateur Système', 'Infrastructure IT', 2);

SELECT setval(
    pg_get_serial_sequence('departement', 'id'),
    GREATEST(COALESCE((SELECT MAX(id) FROM departement), 0), 99),
    true
);

SELECT setval(
    pg_get_serial_sequence('employe', 'id'),
    GREATEST(COALESCE((SELECT MAX(id) FROM employe), 0), 99),
    true
);

SELECT setval(
    pg_get_serial_sequence('app_role', 'id'),
    GREATEST(COALESCE((SELECT MAX(id) FROM app_role), 0), 99),
    true
);

SELECT setval(
    pg_get_serial_sequence('account', 'id'),
    GREATEST(COALESCE((SELECT MAX(id) FROM account), 0), 99),
    true
);

SELECT setval(
    pg_get_serial_sequence('account_credential', 'id'),
    GREATEST(COALESCE((SELECT MAX(id) FROM account_credential), 0), 99),
    true
);
