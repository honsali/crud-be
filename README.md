# CRUD RH Backend

Ce backend constitue, avec [crud-fe](../crud-fe/README.md), une seule application RH de démonstration. Il sert à éprouver une manière de construire des applications CRUD-like avec [Engine](../engine/README.md), puis à disposer d'un point de départ pour un bootstrap ou un POC.

Le parti pris est un code court, conventionnel et facile à comprendre : les parcours de l'application doivent être visibles dans les contrôleurs, les services et la persistance. La simplicité du backend est un choix de contexte ; les mécanismes ajoutés doivent répondre aux besoins réels de cette cible.

## Concevoir une application complète

Le frontend et le backend sont développés et livrés ensemble. Les contrats HTTP se définissent à partir des écrans et des parcours utilisateur : données nécessaires à un formulaire, références à sélectionner, recherche paginée, erreurs à présenter ou résultat d'une action.

Le backend tient donc compte du frontend qui le consomme. Il fournit notamment des identifiants compatibles avec JavaScript, un contrat de pagination explicite et des erreurs exploitables dans l'interface.

Les responsabilités restent claires : le backend porte les décisions métier, la validation faisant autorité, les autorisations, les transactions et l'intégrité des données. Le frontend organise les interactions et présente leurs résultats. Les contrôles backend s'appliquent également lorsqu'un appel arrive sans passer par l'interface.

## Un core commun et une partie issue du générateur

L'application associe un socle réutilisable et du code applicatif produit puis adapté :

| Partie | Rôle |
|---|---|
| `app/core` | Persistance commune, références, pagination, erreurs et authentification |
| `app/domain/rh` | Gros œuvre générable : départements, employés, congés et référentiels |
| `app/domain/admin` | Base Account/Role issue du DSL et adaptations locales de gestion des comptes |

Le core est destiné à être repris dans les applications partageant ces conventions techniques. Il représente le fonctionnement commun que chaque nouveau domaine utilise. L'évolution du socle reste une décision explicite du projet.

Engine décrit le domaine et les actions, puis produit les entités, contrats, contrôleurs, services, repositories et fichiers Liquibase qui s'appuient sur ce core. Son moteur est conçu pour être adapté à d'autres technologies ou architectures ; cette application en constitue la cible Java/Spring actuelle.

Pour Account et Role, la génération fournit une base structurelle. Le traitement des mots de passe, la normalisation des identifiants de connexion et l'authentification restent dans le code de l'application. Les fichiers Account backend générés ne doivent pas écraser cette implémentation de sécurité.

## Le gros œuvre donne des repères

Trois parcours servent de référence :

- les départements illustrent un CRUD en liste simple ;
- les employés illustrent la recherche et la pagination ;
- les congés illustrent une relation parent/enfant et ses parcours associés.

Leur organisation répétée donne une forme prévisible à l'application. En découvrant une nouvelle entité, le développeur retrouve les mêmes emplacements pour ses contrats, son comportement métier et son accès aux données.

Cette forme facilite le démarrage, la lecture et les changements transversaux. Les règles particulières ajoutées ensuite deviennent le travail propre à l'application.

## Reprendre le code, puis choisir ses évolutions

À `t = 0`, une fois le core et les conventions de la cible préparés, le gros œuvre d'un nouveau module peut être repris tel quel depuis `engine/result/be`. Le développeur poursuit ensuite le travail dans ce dépôt.

Lorsqu'Engine ou le DSL évolue, la nouvelle génération fournit une proposition. Le développeur utilise son outil de diff pour récupérer uniquement les fichiers, blocs ou lignes utiles, en préservant les adaptations de l'application. Les printers d'Engine écrivent dans `result` ; le transfert est une opération explicite.

Le résultat de départ conserve sa valeur de plan de base. Comparer l'application actuelle à cette référence permet de retrouver les règles et adaptations ajoutées depuis, comme les transformations d'une maison par rapport à son plan d'origine.

Pour intégrer une nouvelle génération, on distingue l'ancien résultat `G0`, l'application actuelle `P` et le nouveau résultat `G1`. Les fichiers restés identiques peuvent être remplacés ; ceux qui ont été personnalisés demandent un report sélectif. Cette méthode suppose de conserver le bon `G0` ou de pouvoir le reproduire.

Une amélioration qui doit se répéter dans les modules remonte dans Engine. Une règle propre à cette application reste dans son code métier. Le [workflow Engine](../engine/README.md) décrit cette démarche commune au frontend et au backend.

## Une complexité proportionnée à la démonstration

Cette cible privilégie les usages de démonstration, de bootstrap et de POC. Les exigences d'une application bancaire ou d'une plateforme d'identité complète ne constituent pas son contrat de départ.

Les choix actuels rendent ce positionnement concret : données de démonstration recréées au démarrage, configuration locale prête à utiliser, authentification JWT simple et couches Spring conventionnelles. Les détails de ces comportements sont décrits dans le [guide de développement](DEVELOPMENT.md).

La simplicité conserve les mécanismes utiles au fonctionnement de l'application : validation des requêtes, contraintes d'unicité, gestion des références, transactions et versions optimistes. Une exigence supplémentaire se traite lorsqu'elle correspond au contexte de l'application que l'on construit.

## Quand utiliser ce projet

- Pour montrer rapidement des parcours RH complets avec leur interface.
- Pour valider les sorties backend d'Engine sur une application exécutable.
- Pour amorcer une application CRUD-like à partir d'un core et de conventions connus.
- Pour explorer une règle métier ou un changement transversal avant de décider ce qui mérite d'être réutilisé.

## Pour poursuivre

- [Guide de développement](DEVELOPMENT.md) : prérequis, démarrage, données de démonstration, authentification, API et tests.
- [Frontend](../crud-fe/README.md) : l'autre partie de la même application et ses choix d'architecture.
- [Engine](../engine/README.md) : composition, génération et intégration par comparaison.
