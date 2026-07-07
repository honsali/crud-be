# Audit technique — crud-be

| | |
|---|---|
| **Projet** | `app:core` 0.5.0 — backend CRUD RH (Spring Boot 4.0.6, Java 25, PostgreSQL) |
| **Date de l'audit** | 2026-07-06 |
| **Périmètre** | Totalité du code source `src/main/java`, changelogs Liquibase, seeds CSV, `application.yml`, `pom.xml`, scripts, documentation (`README.md`, `AGENTS.md`) |
| **Référentiel d'évaluation** | Adapté au positionnement du projet : application **solo-dev**, **générée par un générateur de code**, choix **opinionated** assumés, verbosité préférée à la généricité. Les recommandations respectent cette philosophie — quand un correctif est proposé, il vise le **template du générateur**, pas l'ajout d'une couche d'abstraction. |

---

## 1. Synthèse

Le projet est **structurellement excellent** : architecture package-par-feature exemplaire pour du code généré, mappings DTO explicites et debuggables, conventions documentées avec une rigueur rare, choix techniques modernes (Java 25, virtual threads, `open-in-view: false`, Liquibase-only). C'est une très bonne fondation.

En revanche, il n'est **pas encore livrable chez un client** en l'état, pour trois raisons principales :

1. **Sécurité des valeurs par défaut** : un secret JWT de repli committé dans Git et un compte `admin/admin` seedé signifient que tout déploiement où l'on oublie une variable d'environnement est compromis d'avance (§4, C1–C2).
2. **Robustesse des erreurs** : toute violation d'intégrité en base (suppression d'un employé ayant des congés, référence vers un id inexistant, course sur une contrainte d'unicité) produit une erreur **500** brute au lieu d'un 409/404 propre (§4, C3). Pour une application dont l'argument de vente est « solide pour un client », c'est le point faible n°1 constaté à la lecture.
3. **Contrat API instable par endroits** : pagination sans tri déterministe, sérialisation directe de `PageImpl`, réponses POST/PUT dont les références imbriquées sont incomplètes par rapport aux réponses GET (§4, M1–M3).

Aucun de ces problèmes n'est coûteux à corriger, et **tous se corrigent une seule fois dans les templates du générateur** — c'est précisément la force du modèle choisi.

### Notation par domaine

| Domaine | Note | Commentaire |
|---|---|---|
| Architecture & lisibilité du code généré | 9/10 | Package-par-feature, mappings explicites, zéro magie. Objectif atteint. |
| Persistance / JPA | 7,5/10 | LAZY + EntityGraph bien utilisés, `equals`/`hashCode` corrects ; nullabilité de `conge.employe_id` et rechargement des refs à revoir. |
| Liquibase / schéma | 7/10 | Discipline `ddl-auto: none` exemplaire ; changesets vides et ordre des includes fragile. |
| Contrat API & gestion d'erreurs | 4,5/10 | Les cas nominaux sont propres ; les cas d'erreur fuient en 500 et le format d'erreur n'est pas homogène. |
| Sécurité | 5/10 | Mécanique JWT/BCrypt saine, mais défauts de configuration dangereux et rôles non exploités. |
| Outillage solo-dev (« monter vite chez un client ») | 5,5/10 | Scripts locaux OK ; pas de docker-compose, pas de healthcheck, pas de packaging livrable. |
| Documentation | 9,5/10 | `AGENTS.md` + `README.md` d'une qualité exceptionnelle, y compris pour le travail assisté par IA. |
| Tests | 1/10 | Absence totale — choix documenté, mais voir §7.1 : le générateur change l'économie du problème. |

---

## 2. Points forts (à préserver tels quels)

Ces éléments sont des choix justes qu'il ne faut **pas** « améliorer » :

- **Package-par-feature auto-contenu** (`domain/rh/employe` contient entité, DTO, filtre, repository, resource, service, specification). C'est la meilleure structure possible pour du code généré destiné à être retouché à la main ou par IA : chaque entité se comprend, se débogue et se supprime isolément.
- **Mappers statiques dans les records DTO** (`toDto`, `toDtoAsRef`, `toEntity`, `toEntityAsRef`, `copyToEntity`). Zéro réflexion, zéro MapStruct, zéro surprise au runtime. La distinction `toDto`/`toDtoAsRef` pour casser les cycles de graphe est propre.
- **`equals`/`hashCode` JPA corrects** ([Employe.java:199-213](src/main/java/app/domain/rh/employe/Employe.java)) : égalité par id si présent, `hashCode` constant par classe. C'est le pattern de référence (proxy-safe), très souvent raté ailleurs.
- **`@ManyToOne(fetch = LAZY)` partout + `@EntityGraph` sur les requêtes de liste** ([EmployeRepository.java:14-16](src/main/java/app/domain/rh/employe/EmployeRepository.java), [CongeRepository.java:11-12](src/main/java/app/domain/rh/conge/CongeRepository.java)) : les endpoints de liste ne font pas de N+1. Discipline remarquable.
- **`BaseSpecification.escapeLike`** ([BaseSpecification.java:21-27](src/main/java/app/core/BaseSpecification.java)) : échappement de `\`, `%` et `_` avec caractère d'échappement explicite dans le `like`. Détail presque toujours oublié ; ici c'est juste.
- **`ReferenceDataService` strictement allow-listé** ([ReferenceDataService.java:17-22](src/main/java/app/core/referenceData/ReferenceDataService.java)) : entités et champs de filtre validés contre une map statique avant interpolation JPQL, valeurs passées en paramètres bindés. Pas d'injection possible.
- **`spring.jpa.open-in-view: false`** ([application.yml:21](src/main/resources/application.yml)) : choix correct et rarement fait ; combiné avec les DTO mappés en transaction, il élimine les `LazyInitializationException` de rendu.
- **`ddl-auto: none` + Liquibase seul maître du schéma**, séquences `allocationSize = 1` pour des ids prévisibles, seeds CSV rejouables.
- **Garde-fou `max-page-size: 100`** ([application.yml:11](src/main/resources/application.yml)) : empêche un client de demander 10⁶ lignes.
- **Contrôle de cohérence path/body sur les PUT** ([EmployeResource.java:46-48](src/main/java/app/domain/rh/employe/EmployeResource.java)) et **unicité en update via `existsByXAndIdNot`** : les deux pièges classiques du CRUD sont couverts.
- **Stack moderne assumée** : Java 25, Spring Boot 4, virtual threads activés, records. Pour un solo-dev sans comité de validation, c'est exactement le bon usage de la liberté « opinionated ».
- **Documentation `AGENTS.md`** : conventions de génération, inventaire des endpoints, points de vigilance. C'est un multiplicateur de productivité pour le travail IA-assisté ; peu de projets professionnels ont cet artefact.

---

## 3. État du dépôt au moment de l'audit

- Deux modifications non commitées : bump de version `0.4.0 → 0.5.0` dans [pom.xml](pom.xml) et **correction d'un XML invalide** dans [conge_table.xml:18](src/main/resources/liquibase/changelog/conge_table.xml) (`nullable="false"unique="true"` sans espace — le fichier committé en `bab524b` ne parse pas ; Liquibase échouerait au démarrage sur un clone propre). **Commiter ce correctif en priorité.**
- `audit.md` vide (réservé pour ce document).

---

## 4. Constats

Classement : **C = critique** (bloquant pour une mise chez un client), **M = majeur** (dégrade la solidité ou le contrat API), **m = mineur** (finition, cohérence).

### C1 — Secret JWT de repli committé dans Git, utilisé silencieusement

**Localisation** : [application.yml:34](src/main/resources/application.yml)

```yaml
jwt-base64-secret: ${APP_SECURITY_JWT_BASE64_SECRET:ODljMGVhM2Q1...}
```

**Problème** : si la variable d'environnement n'est pas définie — le cas par défaut de tout déploiement pressé — l'application signe ses JWT avec un secret **public** (présent dans l'historique Git, donc dans tout fork/backup/partage du repo). N'importe qui connaissant ce secret forge un token `ROLE_ADMIN` valide et obtient un accès complet à l'API. Le pire aspect est le caractère **silencieux** : l'application démarre normalement, rien ne signale que l'on tourne sur le secret de démonstration.

**Recommandation (côté générateur, deux couches)** :
1. Le générateur produit un **secret aléatoire unique par projet généré** (64 octets, base64) au lieu d'une constante partagée par toutes les applications issues du template.
2. Ajouter un garde-fou explicite au démarrage — dans l'esprit verbeux du projet, quelques lignes dans `SecurityConfiguration` suffisent : si le secret actif est celui du profil de dev par défaut **et** que le profil n'est pas `dev`/`local`, lever une exception au boot. Échouer bruyamment plutôt que tourner compromis.

### C2 — Compte `admin/admin` seedé en base

**Localisation** : [app_user.csv:2](src/main/resources/liquibase/data/app_user.csv), documenté dans README/AGENTS.

**Problème** : le changeset `security-app-user-data` charge un compte `admin` avec le mot de passe connu `admin` et `ROLE_ADMIN`. Ce changeset s'exécute **aussi en production** (aucun contexte Liquibase ne le restreint). Toute application livrée à un client embarque donc un compte de porte dérobée documenté publiquement dans le README.

**Recommandation** : marquer le changeset de seed utilisateur avec `context="dev"` et activer `spring.liquibase.contexts: dev` uniquement en local ; pour la production, générer le hash BCrypt d'un mot de passe aléatoire affiché une seule fois (script d'installation), ou exiger la création du premier compte via variable d'environnement. Une solution `context="dev"` coûte deux lignes et supprime le risque.

### C3 — Violations d'intégrité en base → 500 systématique

**Localisation** : tous les services/resources ; aucun handler pour `DataIntegrityViolationException`.

**Problème** : trois scénarios courants et parfaitement légitimes côté client produisent une stacktrace 500 :

| Scénario | Résultat actuel | Attendu |
|---|---|---|
| `DELETE /api/employe/1` alors que l'employé a des congés (`fk_conge_employe_id`) | 500 | 409 Conflict avec message exploitable |
| `POST /api/employe` avec `sexe.id = 999` (référence inexistante — `toEntityAsRef` fabrique un stub sans vérifier l'existence) | 500 au flush (violation FK) | 400/404 propre |
| Deux `POST /api/employe` simultanés avec le même matricule (le `existsByMatricule` puis `save` n'est pas atomique) | 500 (violation `ux_employe_matricule`) | 400/409 comme le chemin nominal |

La convention actuelle (`IllegalArgumentException` → 400, `NoSuchElementException` → 404) couvre les erreurs **détectées par le code**, mais tout ce que la base rejette passe au travers. Or les contraintes FK/unique de la base sont justement le dernier filet — il *va* être touché en usage réel.

**Recommandation** : c'est le cas légitime d'une **unique classe partagée dans `core`** (au même titre que `BaseSpecification`) : un `@RestControllerAdvice` généré, explicite et verbeux, qui mappe `DataIntegrityViolationException` → 409, `MethodArgumentNotValidException` → 400 avec la liste des champs, et sert de format d'erreur unique (voir M5). Cela ne contredit pas la doctrine anti-généricité : c'est de l'infrastructure transverse, comme la sécurité, pas un moteur CRUD caché. Alternative pour rester 100 % par-entité : ajouter `catch (DataIntegrityViolationException e)` dans chaque méthode de Resource générée — verbeux mais cohérent avec le style actuel.

### M1 — Pagination sans tri stable

**Localisation** : [EmployeResource.java:40](src/main/java/app/domain/rh/employe/EmployeResource.java), [EmployeService.java:31](src/main/java/app/domain/rh/employe/EmployeService.java)

**Problème** : `POST /api/employe/filtrer` sans paramètre `sort` exécute un `SELECT ... LIMIT 20 OFFSET n` **sans `ORDER BY`**. PostgreSQL ne garantit alors aucun ordre : entre la page 1 et la page 2, une ligne peut apparaître deux fois ou jamais (l'ordre physique change avec les updates, le VACUUM, les parallel scans). C'est le bug de pagination le plus sournois qui soit — invisible en démo, réel chez le client.

**Recommandation** : dans le template du service ou de la resource générée, imposer un tri de repli explicite :

```java
if (pageable.getSort().isUnsorted()) {
    pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id"));
}
```

Idéalement, toujours **suffixer** le tri utilisateur par `id` (tie-breaker) pour garantir un ordre total même quand on trie sur une colonne non unique (`nom`).

### M2 — Sérialisation directe de `PageImpl`

**Localisation** : [EmployeResource.java:40-42](src/main/java/app/domain/rh/employe/EmployeResource.java) (retour `Page<EmployeDto>`)

**Problème** : depuis Spring Data 3.3, sérialiser `PageImpl` directement en JSON est officiellement non supporté (warning au démarrage, structure JSON non garantie entre versions). Le contrat consommé par le frontend (`totalElements`, `content`, …) peut changer à une montée de version — exactement le genre de casse silencieuse qu'un socle générateur doit interdire.

**Recommandation** : deux options compatibles avec la philosophie :
1. `@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)` sur `CoreApplication` — une ligne, contrat `PagedModel` stable.
2. Plus « opinionated » et à mon sens meilleure pour un générateur : un petit record `PageDto<T>(List<T> content, long totalElements, int totalPages, int page, int size)` dans `core`, construit explicitement dans chaque service `filtrer`. Contrat API **possédé par vous**, découplé des choix de sérialisation de Spring Data, documentable dans la DSL.

### M3 — Réponses POST/PUT incohérentes avec les réponses GET (références tronquées)

**Localisation** : [EmployeService.java:20-27,34-41](src/main/java/app/domain/rh/employe/EmployeService.java) combiné à [EmployeDto.java:90-105](src/main/java/app/domain/rh/employe/EmployeDto.java)

**Problème** : `copyToEntity` affecte aux associations des **stubs** créés par `toEntityAsRef` (id seul, tous les autres champs `null`). La réponse est ensuite construite par `toDto(saved)` qui lit `entity.getSexe().getLibelle()` sur ce stub → le client reçoit `{"sexe": {"id": 1, "libelle": null}}` après un POST/PUT, alors que le GET du même employé renvoie `{"sexe": {"id": 1, "libelle": "Masculin"}}`. Le frontend doit soit re-fetcher systématiquement, soit gérer deux formes du même objet.

**Recommandation** : dans le template `creer`/`maj`, recharger les associations avant le mapping retour. Version explicite fidèle au style du projet : résoudre les références via leur repository (`sexeRepository.findById(dto.sexe().id()).orElseThrow(...)`) au lieu de fabriquer des stubs. Bénéfice double : la réponse est complète **et** une référence inexistante devient un 404 propre au lieu du 500 de C3. C'est plus verbeux (une injection de repository par association) — précisément le compromis que ce projet revendique.

### M4 — Un congé peut être orphelin ou changer d'employé silencieusement

**Localisation** : [conge_table.xml:32-34](src/main/resources/liquibase/changelog/conge_table.xml) (`employe_id` nullable), [CongeDto.java:64-71](src/main/java/app/domain/rh/conge/CongeDto.java), [CongeService.java:46-53](src/main/java/app/domain/rh/conge/CongeService.java)

**Problème** : la création est proprement imbriquée (`POST /api/employe/{id}/conge` vérifie l'employé et le mismatch d'id), mais l'update ne l'est pas : `PUT /api/conge/{id}` avec `"employe": null` met `employe_id` à `NULL` (le schéma l'autorise) — le congé disparaît de toutes les listes par employé sans être supprimé. Avec `"employe": {"id": 2}`, le congé est réaffecté à un autre employé sans aucun contrôle. Un congé sans employé n'a pas de sens métier ; le schéma et le code devraient l'interdire.

**Recommandation** : dans la DSL, distinguer les relations **obligatoires** (composition : `conge → employe`) des relations optionnelles (`employe → departement`). Pour une relation obligatoire, générer : `nullable="false"` en Liquibase, `@NotNull` sur le champ DTO, `optional = false` sur le `@ManyToOne`, et dans `maj` soit interdire le changement d'employé, soit exiger la même vérification de cohérence que `creer`.

### M5 — Format d'erreur hétérogène, plusieurs fuites en 500

**Localisation** : transverse.

**Problème** : le client frontend reçoit aujourd'hui, selon le cas : le JSON d'erreur par défaut de Spring Boot (`@Valid` échoué), un `ResponseStatusException` avec message texte anglais (règles métier), un 401 à corps vide via l'entry point Bearer (mauvais identifiants sur `/api/authenticate`), un 500 brut (violation FK — C3, propriété de tri inconnue `?sort=nimportequoi` → `PropertyReferenceException`, `Pageable` malformé). Quatre formats pour une seule API, et deux 500 évitables.

**Recommandation** : standardiser sur **RFC 9457 Problem Details**, nativement supporté (`spring.mvc.problemdetails.enabled: true`), et concentrer le mapping dans le `@RestControllerAdvice` de C3 : `DataIntegrityViolationException` → 409, `PropertyReferenceException` → 400, `BadCredentialsException` → 401 avec corps JSON. Un seul fichier généré dans `core`, contrat d'erreur unique documenté pour le frontend.

### M6 — `anyRequest().permitAll()`

**Localisation** : [SecurityConfiguration.java:51](src/main/java/app/core/security/SecurityConfiguration.java)

**Problème** : tout ce qui n'est pas sous `/api/**` est public. Aujourd'hui il n'y a rien d'autre — mais le jour où l'on ajoute Actuator, une console, un endpoint de debug, il est exposé par défaut. Un socle sécurisé doit être **fail-closed**.

**Recommandation** : `anyRequest().denyAll()`. Coût nul, supprime toute une classe d'accidents futurs.

### M7 — Rôles présents mais jamais exploités

**Localisation** : [SecurityConfiguration.java:47-51](src/main/java/app/core/security/SecurityConfiguration.java), [UserResource.java](src/main/java/app/core/security/UserResource.java)

**Problème** : la chaîne complète des autorités fonctionne (rôles en base → claim `auth` → `GrantedAuthority`), le frontend reçoit `role`/`roles`… et côté backend, `ROLE_USER` et `ROLE_ADMIN` donnent exactement les mêmes droits : tout utilisateur authentifié peut tout créer/modifier/supprimer. Pour une application cliente, « les employés consultent, la RH modifie » est la toute première exigence d'autorisation qui arrivera. La sécurité appliquée uniquement côté frontend n'est pas une sécurité.

**Recommandation** : prévoir dans la DSL un attribut de rôle par entité ou par opération, et générer des règles **explicites et lisibles** dans le style du projet :

```java
.requestMatchers(HttpMethod.DELETE, "/api/employe/**").hasAuthority("ROLE_ADMIN")
.requestMatchers(HttpMethod.POST, "/api/employe/**").hasAuthority("ROLE_ADMIN")
```

La liste exhaustive des matchers générés dans `SecurityConfiguration` est verbeuse, auditables d'un coup d'œil — parfaitement dans la philosophie. À défaut, documenter explicitement que la v1 est mono-rôle.

### M8 — Changesets Liquibase vides générés

**Localisation** : [sexe_constraints.xml:9-10](src/main/resources/liquibase/changelog/sexe_constraints.xml), [departement_constraints.xml](src/main/resources/liquibase/changelog/departement_constraints.xml), [typeConge_constraints.xml](src/main/resources/liquibase/changelog/typeConge_constraints.xml), [situationFamiliale_constraints.xml](src/main/resources/liquibase/changelog/situationFamiliale_constraints.xml)

**Problème** : quatre changesets `<changeSet id="…-2">` **vides** sont exécutés et enregistrés dans `DATABASECHANGELOG`. Outre la pollution, c'est un piège différé : si le générateur (ou vous) remplit plus tard ce changeset en gardant le même id, toute base existante lèvera une erreur de checksum au démarrage.

**Recommandation** : le générateur ne doit émettre le fichier `*_constraints.xml` (et son include dans `master.xml`) que si l'entité a effectivement des FK. Un fichier absent est plus propre qu'un changeset vide.

### M9 — Ordre des includes de `master.xml` fragile

**Localisation** : [master.xml:15-27](src/main/resources/liquibase/master.xml)

**Problème** : `conge_table.xml` est inclus **avant** `employe_table.xml` et `typeConge_table.xml`, et son `loadData` s'exécute donc avant que les tables référencées existent. Cela fonctionne aujourd'hui uniquement parce que (a) les FK sont différées dans les fichiers `constraints` et (b) `conge.csv` est vide. Le jour où le générateur émet des données de congés, elles seront insérées sans validation FK, puis l'ajout de la contrainte échouera si une référence est mauvaise — erreur au mauvais endroit, difficile à diagnostiquer.

**Recommandation** : le générateur doit trier les includes par ordre topologique des dépendances (tables référencées d'abord : `sexe`, `situationFamiliale`, `departement`, `typeConge`, puis `employe`, puis `conge`). L'information est déjà dans la DSL.

### M10 — Réponse 500 sur `Pageable` hostile *(voir M5 — rappel dédié car exposé publiquement)*

`POST /api/employe/filtrer?sort=inexistant` → `PropertyReferenceException` → 500. Toute propriété de l'entité est par ailleurs triable (pas de liste blanche de tri). Pour `Employe` aucune colonne n'est sensible, mais le générateur devrait borner les colonnes triables aux champs exposés dans le DTO — l'allow-list existe déjà conceptuellement dans `ReferenceDataService`, appliquer la même rigueur au tri.

---

### Constats mineurs

**m1 — Ordre des vérifications dans `CongeService.creer`** ([CongeService.java:24-29](src/main/java/app/domain/rh/conge/CongeService.java)) : `dto.code()` est déréférencé ligne 24, mais le garde `dto != null` n'apparaît que ligne 27. Le null-check est donc mort (NPE avant lui si `dto` était null). Sans conséquence en pratique (`@Valid @RequestBody` garantit un corps), mais un template doit être irréprochable sur l'ordre des gardes.

**m2 — Champs dupliqués `id`/`idEmploye`, `id`/`idDepartement`, …** dans tous les DTO, et `username`/`login`, `roles`/`authorities` dans `UserInfo`. Dette de compatibilité frontend documentée — acceptable, mais à résorber : chaque champ dupliqué est un point de divergence potentiel. Cible : une seule clé par concept dans la prochaine itération front+générateur.

**m3 — Langue des messages d'erreur** : domaine et API en français (`creer`, `maj`, `/api/employe`), messages en anglais (`"Matricule already exists"`, `"Employe not found"`). Ces messages remontent au frontend et donc potentiellement à l'utilisateur final francophone. Choisir une langue unique dans les templates (le français, vu le positionnement).

**m4 — 400 pour les doublons d'unicité** : `"Matricule already exists"` est sémantiquement un **409 Conflict**, pas un 400. À aligner avec le mapping de C3.

**m5 — `code` non unique sur les tables de référence** ([sexe_table.xml](src/main/resources/liquibase/changelog/sexe_table.xml), idem `type_conge`, `situation_familiale`) : `libelle` est unique mais `code` — l'identifiant métier typique — ne l'est pas. Deux lignes `M` possibles. Si la DSL marque `code` comme `.isId()`, la contrainte manque ; sinon, elle mérite de l'être.

**m6 — `ReferenceDataDto` n'expose pas `code`** ([ReferenceDataDto.java](src/main/java/app/core/referenceData/ReferenceDataDto.java)) : le frontend reçoit `(id, libelle)` ; dès qu'une logique conditionnelle côté client s'appuie sur le code (`"MAR"`, `"CEL"`), il devra le récupérer autrement. Ajouter `code` au record coûte une ligne et évite un futur endpoint.

**m7 — `recupererParId` sans EntityGraph** ([EmployeService.java:44](src/main/java/app/domain/rh/employe/EmployeService.java)) : le GET unitaire déclenche 1 + 3 selects (lazy sur `sexe`, `situationFamiliale`, `departement`). Négligeable en charge solo-dev ; si le template est retouché, une surcharge `findById` avec `@EntityGraph` harmoniserait avec `findAll`.

**m8 — `rememberMe` accepté et ignoré** ([AuthResource.java:59](src/main/java/app/core/security/AuthResource.java)) : soit l'implémenter (TTL étendu quand `rememberMe=true` — deux lignes), soit le retirer du record. Un champ de contrat sans effet est un mensonge d'API.

**m9 — Issuer non validé au décodage** ([SecurityConfiguration.java:88-90](src/main/java/app/core/security/SecurityConfiguration.java)) : l'encodeur émet `issuer("app_core")` mais le décodeur ne le vérifie pas. Avec un secret par application (cf. C1) le risque est faible ; ajouter `JwtValidators` avec issuer serait la finition cohérente.

**m10 — Pas de limitation de débit sur `/api/authenticate`** : BCrypt(12) ralentit naturellement le brute-force (~100 ms/essai), c'est une défense honnête pour le positionnement. À documenter comme choix assumé ; un compteur d'échecs en mémoire par username/IP serait l'étape suivante si un client l'exige.

**m11 — Pas d'endpoint de santé** : Actuator a été retiré (choix documenté), mais un déploiement client a besoin d'un healthcheck (Docker, reverse proxy, monitoring uptime). Dans l'esprit du projet : un `HealthResource` généré de dix lignes (`GET /health` public → `SELECT 1` + statut) plutôt que réintroduire Actuator.

**m12 — Rien pour provisionner l'environnement** : pas de `docker-compose.yml` (PostgreSQL), pas de `Dockerfile`/`bootBuildImage` documenté, pas de profil de production exemple. Pour l'objectif « monter rapidement une application solide pour un client », le générateur devrait émettre : `compose.yaml` (postgres + volume), un `Dockerfile` ou la commande `spring-boot:build-image`, et un `.env.example` listant les variables obligatoires en production (miroir de la section env du README).

**m13 — `ReferenceDataService` contredit la doctrine anti-généricité** : c'est le seul endroit du projet avec un moteur générique (map de métadonnées + JPQL interpolé), alors qu'`AGENTS.md` proscrit précisément « les moteurs CRUD génériques et abstractions cachées ». Il est bien écrit et sûr (allow-list), donc pas urgent — mais la cohérence voudrait soit générer un `ReferenceResource` explicite par entité de référence (3 petits fichiers de plus par entité, assumés), soit documenter explicitement cette exception dans `AGENTS.md` et son invariant de sécurité (« tout champ ajouté à `REFERENCES` doit rester allow-listé »). Accessoirement, lui ajouter `@Transactional(readOnly = true)`.

**m14 — Pas de verrouillage optimiste (`@Version`)** : deux modifications concurrentes du même employé → dernier-écrit-gagne, silencieusement. Pour du multi-utilisateur léger chez un client, un champ `@Version` généré (+ colonne Liquibase + mapping de `OptimisticLockingFailureException` → 409) est peu coûteux et très « solide ». À défaut, documenter le choix last-write-wins.

**m15 — Pas de timestamps `date_creation`/`date_modification`** : l'auditing JHipster a été retiré, très bien — mais « quand cette fiche a-t-elle été modifiée ? » est une demande client quasi systématique. Version fidèle à la philosophie, sans framework : deux champs + `@PrePersist`/`@PreUpdate` explicites dans l'entité générée. Option de DSL plutôt que réflexe systématique.

**m16 — Divers config** : `server.shutdown: graceful` absent (arrêts brutaux en plein requête lors des redéploiements) ; compression HTTP désactivée (listes JSON) ; `spring.jackson` non configuré (les défauts ISO-8601 conviennent — rien à faire, juste noté comme vérifié).

**m17 — Sort de secours documenté nulle part pour `GET /api/departement` et la liste des congés** : ces listes non paginées sont triées (bien) mais sans pagination ; acceptable pour des volumes de référence, à surveiller si une entité « liste complète » dépasse quelques milliers de lignes.

---

## 5. Sécurité — vue d'ensemble

| Aspect | État | Verdict |
|---|---|---|
| Authentification (BCrypt 12, JWT HS512, stateless) | Solide | ✅ |
| CSRF désactivé | Justifié (aucun cookie de session, `allowCredentials(false)`) | ✅ |
| CORS | Origins explicites par env, headers bornés | ✅ |
| Injection SQL/JPQL | Paramètres bindés partout, allow-lists sur le dynamique | ✅ |
| Échappement LIKE | Correct | ✅ |
| Secrets | Repli committé + admin/admin seedé | ❌ C1/C2 |
| Autorisation | Authentification seule, rôles non appliqués | ❌ M7 |
| Posture par défaut | `permitAll` hors `/api` | ⚠️ M6 |
| Exposition d'infos | Stacktraces non exposées (défaut Boot), messages d'erreur sobres | ✅ |
| Token 24 h sans révocation | Choix assumé cohérent avec le stateless | ✅ à documenter |

La mécanique est saine ; ce sont les **valeurs par défaut de livraison** qui sont dangereuses. C'est la meilleure nouvelle possible : tout se corrige dans les templates sans toucher à l'architecture.

---

## 6. Adéquation à la philosophie « code généré, verbeux, explicite »

Verdict global : **très bonne exécution** de la doctrine. Le code généré ressemble effectivement à du bon code écrit à la main ; la répétition entre entités est régulière (donc diff-able et régénérable) ; aucune magie au runtime.

Trois écarts internes à la doctrine :

1. **`ReferenceDataService`** est un moteur générique dans un projet qui les proscrit (m13).
2. **Les conventions imposent du code mort** : chaque `creer` catch `NoSuchElementException` « même sans référence » — assumé et documenté, OK ; mais m1 montre que la duplication de gardes finit par produire des incohérences d'ordre. Plus le template est riche en try/catch répétés, plus l'argument pour le `@RestControllerAdvice` unique (C3/M5) se renforce : il *supprime* du code généré répétitif au lieu d'en ajouter.
3. **La duplication `id`/`idX`** (m2) est de la généricité inversée — deux chemins pour la même donnée. À résorber côté contrat front.

---

## 7. Recommandations stratégiques (au-delà du code actuel)

### 7.1 Tests : le générateur change l'économie du problème

« Pas de tests » est documenté comme choix. L'argument habituel contre les tests en solo-dev est leur coût de maintenance — mais ici, **les tests sont générés** : leur coût marginal de maintenance est nul (régénération), et ils valident le générateur lui-même, pas seulement l'application. Un bug de template corrompt *toutes* les applications générées ; c'est le composant qui mérite le plus de tests de tout l'écosystème.

Recommandation minimale à fort levier : le générateur émet par entité un test d'intégration standard (Testcontainers PostgreSQL) qui déroule le cycle créer → lire → filtrer → modifier → supprimer + les cas d'erreur conventionnels (404, doublon, mismatch d'id). Un seul template de test → couverture mécanique de chaque application générée, et détection immédiate des régressions du générateur. Les scénarios C3/M1/M3 de cet audit auraient tous été attrapés par un tel test.

### 7.2 Livrable « client-ready » généré

Pour tenir la promesse « monter rapidement une application solide pour un client », le générateur devrait émettre, en plus du code : `compose.yaml` (PostgreSQL), `Dockerfile` ou recette `bootBuildImage`, `.env.example` (variables obligatoires, secret pré-généré), un `GET /health`, et un `openapi.yaml` **statique généré depuis la DSL** (zéro dépendance runtime, dans la droite ligne de la philosophie — la spec est un artefact de génération comme le code).

### 7.3 Ce qu'il ne faut PAS faire

Pour verrouiller les bons choix existants contre les « bonnes pratiques » génériques qui dégraderaient ce projet : pas de MapStruct/ModelMapper (les mappers manuels sont le cœur de la valeur), pas d'architecture hexagonale/couches supplémentaires, pas de HATEOAS, pas de microservices, pas de cache tant qu'aucune mesure ne le justifie, pas de Spring Session/refresh tokens tant que le TTL 24 h suffit, pas de réintroduction d'Actuator complet pour un simple healthcheck. L'`AGENTS.md` protège déjà bien ce périmètre.

---

## 8. Plan d'action priorisé

| # | Action | Constats | Effort | Où |
|---|---|---|---|---|
| 1 | Commiter le correctif XML de `conge_table.xml` (repo actuellement cassé au clone) | §3 | 1 min | repo |
| 2 | Secret JWT : aléatoire par projet + fail-fast si secret de dev hors profil dev | C1 | 1 h | générateur + template |
| 3 | Seed `admin/admin` restreint au contexte Liquibase `dev` | C2 | 30 min | template Liquibase |
| 4 | `@RestControllerAdvice` généré : `DataIntegrityViolation` → 409, Problem Details partout, `PropertyReference` → 400, `BadCredentials` → 401 JSON | C3, M5, M10, m4 | ½ j | template `core` |
| 5 | Tri de repli + tie-breaker `id` dans `filtrer` | M1 | 1 h | template service |
| 6 | Contrat de page stable (`PageDto` explicite ou `VIA_DTO`) | M2 | 1–2 h | template + front |
| 7 | Résolution des références par repository dans `creer`/`maj` (réponses complètes + 404 propres) | M3, C3 | ½ j | template service |
| 8 | Relations obligatoires dans la DSL → `nullable=false` + `@NotNull` + contrôle en update | M4 | ½ j | DSL + templates |
| 9 | `anyRequest().denyAll()` | M6 | 5 min | template sécurité |
| 10 | Ne plus émettre de changesets vides ; tri topologique des includes | M8, M9 | 2 h | générateur |
| 11 | Autorisation par rôle pilotée par la DSL | M7 | 1 j | DSL + templates |
| 12 | Kit livraison : compose, Dockerfile, `.env.example`, `/health`, openapi statique | m11, m12, 7.2 | 1 j | générateur |
| 13 | Tests d'intégration générés par entité | 7.1 | 1–2 j | générateur |
| 14 | Finitions : m1–m10, m13–m16 | mineurs | au fil de l'eau | templates |

Les actions 1 à 10 représentent environ **3 jours de travail** et font passer le socle de « excellente démo » à « livrable chez un client sans réserve ». Les actions 11 à 13 sont l'investissement qui différencie réellement ce générateur : autorisation déclarative, livraison en une commande, et tests gratuits à la régénération.

---

## 9. Conclusion

La thèse du projet — *la généricité vit dans le générateur, l'application générée est verbeuse, explicite et retouchable* — est juste, originale par rapport aux générateurs existants (JHipster produit l'inverse : du générique difficile à retoucher), et l'exécution architecturale est déjà au niveau. Les faiblesses relevées sont concentrées sur les **chemins d'erreur** et les **valeurs par défaut de livraison**, deux domaines où un template corrigé une fois protège toutes les applications futures. C'est exactement le type de dette qu'il faut payer maintenant, avant que le générateur ne produise sa dixième application.
