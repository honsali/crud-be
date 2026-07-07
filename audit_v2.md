# Audit technique v2 — crud-be

| | |
|---|---|
| **Projet** | `app:core` 0.5.0 — backend CRUD RH (Spring Boot 4.0.6, Java 25, PostgreSQL) |
| **Date de l'audit** | 2026-07-07 |
| **Référence** | Fait suite à [audit.md](audit.md) (v1, 2026-07-06). Porte sur les modifications apportées depuis : 29 fichiers modifiés, 11 nouveaux, 4 supprimés (tous non commités). |
| **Vérification** | `./mvnw -DskipTests compile` exécuté pendant l'audit : **succès** (JDK 25, exit 0). |

---

## 1. Synthèse

**Excellente itération.** Sur les 10 actions priorisées P0/P1 du plan v1, **8 sont traitées, correctement, et la documentation (`README.md`, `AGENTS.md`) a été mise à jour en cohérence** — ce dernier point est rare et mérite d'être souligné : les conventions décrites dans `AGENTS.md` correspondent exactement au nouveau code (mappers, Problem Details, contexte Liquibase `dev`, variables d'environnement).

Deux constats v1 notables restent ouverts (sérialisation `PageImpl`, rôles non appliqués), et les modifications introduisent **trois points de vigilance nouveaux** — aucun critique, mais deux demandent une action locale immédiate (reset de la base de dev, commit de l'ensemble).

Les trois constats critiques de la v1 sont **tous résolus** :

| v1 | Statut |
|---|---|
| C1 — Secret JWT de repli committé, utilisé silencieusement | ✅ Résolu (fail-fast + double verrou, voir §3.1) |
| C2 — Compte `admin/admin` seedé en production | ✅ Résolu (contexte Liquibase `@dev` fail-closed, voir §3.2) |
| C3 — Violations d'intégrité → 500 | ✅ Résolu (`ApiExceptionHandler` → 409, voir §3.3) |

Le niveau général passe de « excellente démo » à « proche du livrable ». Ce qui sépare encore le projet du « livrable sans réserve » : le contrat de pagination (M2), l'autorisation par rôle (M7), le kit de livraison (compose/Dockerfile/health) et les tests générés.

### Notation par domaine (comparatif v1 → v2)

| Domaine | v1 | v2 | Commentaire |
|---|---|---|---|
| Architecture & lisibilité du code généré | 9/10 | 9/10 | Le passage aux `*Mapper` composants garde l'explicite ; DTO records purs = encore plus net. |
| Persistance / JPA | 7,5/10 | 8,5/10 | Références résolues par repository, `conge.employe` non-nullable de bout en bout. |
| Liquibase / schéma | 7/10 | 8,5/10 | Changesets vides supprimés, ordre topologique, `code` unique. Reste la pratique de réécriture (§4.1). |
| Contrat API & gestion d'erreurs | 4,5/10 | 8/10 | Problem Details homogènes, 409/404/400 corrects, tri stable. Reste `PageImpl` (M2) et la langue des messages. |
| Sécurité | 5/10 | 7/10 | Fail-fast secret, `denyAll`, seed gaté. Restent les rôles (M7) et les finitions (issuer, rate limiting). |
| Outillage solo-dev | 5,5/10 | 5,5/10 | Inchangé — docker-compose, `/health`, `.env.example` toujours absents. |
| Documentation | 9,5/10 | 9,5/10 | Mise à jour synchronisée avec le code, y compris les nouvelles conventions de mappers. |
| Tests | 1/10 | 1/10 | Inchangé (voir §6 — recommandation maintenue). |

---

## 2. Suivi complet des constats v1

| ID v1 | Constat | Statut v2 |
|---|---|---|
| §3 | XML invalide `conge_table.xml` | ✅ Corrigé (toujours **non commité** — voir §4.2) |
| C1 | Secret JWT par défaut | ✅ Résolu — §3.1 |
| C2 | Seed `admin/admin` | ✅ Résolu — §3.2 |
| C3 | Intégrité DB → 500 | ✅ Résolu — §3.3 |
| M1 | Pagination sans tri stable | ✅ Résolu — `avecTriStable` avec tie-breaker `id`, au-delà de la recommandation ([EmployeService.java:59-67](src/main/java/app/domain/rh/employe/EmployeService.java)) |
| M2 | Sérialisation `PageImpl` | ❌ **Ouvert** — reste le principal risque de contrat API (rappel §5.1) |
| M3 | Réponses POST/PUT avec refs tronquées (`libelle: null`) | ✅ Résolu — `toEntityAsRef` résout via repository, les réponses sont complètes — §3.4 |
| M4 | Congé orphelin / réaffectation silencieuse | ✅ Résolu — §3.5 |
| M5 | Format d'erreur hétérogène | ✅ Résolu — Problem Details partout (`ApiExceptionHandler` + `spring.mvc.problemdetails.enabled`) |
| M6 | `anyRequest().permitAll()` | ✅ Résolu — `denyAll()` ([SecurityConfiguration.java:53](src/main/java/app/core/security/SecurityConfiguration.java)) |
| M7 | Rôles non appliqués | ❌ **Ouvert** — rappel §5.2 |
| M8 | Changesets Liquibase vides | ✅ Résolu — 4 fichiers supprimés + includes retirés |
| M9 | Ordre des includes `master.xml` | ✅ Résolu — ordre topologique correct (référencées → `employe` → `conge` → contraintes FK) |
| M10 | `?sort=inexistant` → 500 | ✅ Résolu — `PropertyReferenceException` → 400 |
| m1 | Ordre des gardes `CongeService.creer` | ✅ Résolu — le check mort a été supprimé avec la refonte |
| m4 | 400 au lieu de 409 pour les doublons | ✅ Résolu — `ConflictException` → 409 |
| m5 | `code` non unique sur les tables de référence | ✅ Résolu — entités + Liquibase (`ux_sexe_code`, etc.) |
| m2, m3, m6–m16 | Divers mineurs | ❌ Ouverts (id/idX dupliqués, messages anglais, `code` absent de `ReferenceDataDto`, N+1 léger sur GET unitaire, `rememberMe` ignoré, issuer non validé, rate limiting, `/health`, docker, généricité `ReferenceDataService`, `@Version`, timestamps, graceful shutdown) |

**Bilan : 13 constats résolus, 2 majeurs ouverts, les mineurs quasi inchangés.**

---

## 3. Revue détaillée des correctifs

### 3.1 Secret JWT — résolu, avec une conception à deux verrous ✅

[SecurityConfiguration.java:104-116](src/main/java/app/core/security/SecurityConfiguration.java) et [application.yml:36-38](src/main/resources/application.yml).

Le comportement est maintenant : secret explicite → utilisé ; sinon, secret de dev **uniquement si** `APP_SECURITY_ALLOW_UNSAFE_DEV_SECRET=true` **et** profil `prod` inactif ; sinon **échec au démarrage** avec un message actionnable. C'est fail-closed par défaut (le flag vaut `false`), le double verrou (flag + profil) est une bonne défense en profondeur, et `run.bat`/README/AGENTS ont été alignés. Implémentation propre.

Deux réserves mineures :

- **Le second verrou n'est armé que si la production tourne réellement avec le profil `prod`** — or aucune documentation n'impose `spring.profiles.active=prod` en production. Si un déploiement copie-colle `APP_SECURITY_ALLOW_UNSAFE_DEV_SECRET=true` sans profil, le secret public est accepté. Documenter la convention « la prod tourne toujours avec le profil `prod` » (une ligne dans README/`.env.example`), ou inverser la condition (n'autoriser le secret dev que si un profil `dev`/`local` est **explicitement actif**), ce qui serait fail-closed des deux côtés.
- **Le secret de dev reste une constante partagée** par toutes les applications qui sortiront du générateur ([SecurityConfiguration.java:41](src/main/java/app/core/security/SecurityConfiguration.java)). Risque faible (dev uniquement), mais le générateur devrait quand même produire un secret aléatoire par projet — deux tokens de dev de deux projets différents ne devraient pas être interchangeables.

### 3.2 Seed `admin/admin` — résolu, et la sémantique `@dev` est correcte ✅

[security_table.xml:33](src/main/resources/liquibase/changelog/security_table.xml) : `context="@dev"`.

Point vérifié pendant l'audit (c'était le piège classique) : avec un contexte Liquibase *simple* (`context="dev"`), un changeset s'exécute **aussi quand aucun contexte n'est fourni au runtime** — la protection aurait été illusoire en production. Le préfixe `@` (Liquibase ≥ 4.23) inverse précisément ce comportement : un changeset `@dev` **ne s'exécute pas si Liquibase tourne sans contexte explicite**. Sources : [documentation Liquibase — Contexts](https://docs.liquibase.com/concepts/changelogs/attributes/contexts.html), [issue liquibase#3562](https://github.com/liquibase/liquibase/issues/3562), [blog Liquibase sur context filter](https://www.liquibase.com/blog/this-is-a-story-about-control-context-filter-label-filter).

Le montage est donc correct et fail-closed : en production (aucun contexte fourni), le seed ne s'exécute pas ; en local, `run.bat` et les instructions bash exportent `SPRING_LIQUIBASE_CONTEXTS=dev`. La doc a été mise à jour aux quatre endroits concernés.

Réserve : **quiconque lance l'application autrement** (IDE, `mvnw spring-boot:run` sans les exports) obtient une base sans aucun utilisateur → login impossible, sans message explicatif. C'est le prix du fail-closed et il est acceptable ; un mot dans le README (« si le login échoue sur une base fraîche, vérifier `SPRING_LIQUIBASE_CONTEXTS=dev` ») réduirait la friction. Voir aussi §4.1 : ce changement de checksum impose un reset de la base locale existante.

### 3.3 `ApiExceptionHandler` — résolu, couverture plus large que demandé ✅

[ApiExceptionHandler.java](src/main/java/app/core/ApiExceptionHandler.java) couvre : `DataIntegrityViolationException` → 409, `ConflictException` (nouvelle, [ConflictException.java](src/main/java/app/core/ConflictException.java)) → 409, validation (`MethodArgumentNotValidException` + `ConstraintViolationException`) → 400 **avec la liste des champs en propriété `fields`** (très bon pour le frontend), `HttpMessageNotReadableException` → 400, `PropertyReferenceException` → 400, `AuthenticationException` → 401 (le login raté renvoie maintenant un JSON propre au lieu d'un 401 à corps vide), `NoSuchElementException` → 404, `IllegalArgumentException` → 400, et un handler `ResponseStatusException` qui harmonise les erreurs levées par les resources. Combiné à `spring.mvc.problemdetails.enabled: true`, l'API a désormais **un seul format d'erreur** (RFC 9457). Les trois scénarios de reproduction du C3 v1 (suppression avec FK, référence inexistante, course sur l'unicité) aboutissent tous à des statuts corrects.

C'est exactement le « seul fichier générique légitime dans `core` » préconisé. Deux observations :

- **Les try/catch des resources sont devenus largement redondants** : `NoSuchElementException` → 404 et `IllegalArgumentException` → 400 sont maintenant garantis par l'advice, et les doublons (`ConflictException`) ne passent plus par les catches des resources. Le générateur pourrait supprimer ces try/catch de chaque endpoint — **moins de code généré**, comportement identique, un seul endroit à maintenir. `AGENTS.md` documente actuellement les deux mécanismes en parallèle ; à trancher dans la prochaine itération du template (recommandation : l'advice seul).
- La langue des messages reste l'anglais (`"Matricule already exists"`, titres `"Validation failed"`) alors que ces `detail` sont désormais *le* format que le frontend affichera — le constat m3 v1 gagne en importance maintenant que le format est stable.

### 3.4 Mappers `@Component` avec résolution par repository — résolu, bon compromis ✅

Le refactoring est plus ambitieux que la recommandation v1 : les helpers statiques des DTO ont été extraits vers des classes `*Mapper` injectables ([EmployeMapper.java](src/main/java/app/domain/rh/employe/EmployeMapper.java), [CongeMapper.java](src/main/java/app/domain/rh/conge/CongeMapper.java), etc.), les DTO records ne portent plus que la forme de l'API et la validation, et `toEntityAsRef` fait un `findById(...).orElseThrow(NoSuchElementException)` au lieu de fabriquer un stub détaché.

Conséquences vérifiées :
- Réponses POST/PUT complètes (`libelle` renseigné) — M3 v1 clos.
- Référence inexistante → 404 avec message nommé (`"Sexe not found"`) au lieu d'un 500 FK — bonus par rapport à la v1.
- Nouveaux repositories minimalistes pour les entités de référence ([SexeRepository.java](src/main/java/app/domain/rh/sexe/SexeRepository.java), etc.) — cohérents avec le style.
- Coût : jusqu'à 3 SELECT supplémentaires par create/update d'employé. Compromis explicitement le bon ici : `getReferenceById` les éviterait mais ferait resurgir les 500 FK. Rien à changer.
- La séparation « DTO = forme, Mapper = conversion » est *plus* propre pour la génération que la version précédente ; `AGENTS.md` a été réécrit en ce sens. Seule perte marginale : les mappers nécessitent maintenant le contexte Spring (impact uniquement si des tests unitaires purs arrivent un jour — non bloquant).

### 3.5 Relation obligatoire `conge → employe` — résolu de bout en bout ✅

Chaîne complète et cohérente : `@NotNull @ManyToOne(optional = false) @JoinColumn(nullable = false)` ([Conge.java:43-47](src/main/java/app/domain/rh/conge/Conge.java)), `nullable="false"` en Liquibase ([conge_table.xml:33](src/main/resources/liquibase/changelog/conge_table.xml)), `CongeMapper.copyToEntity` **ne touche plus** à `employe` (impossible d'orphaniner ou de réaffecter via PUT), et le champ `employe` du DTO est marqué `@JsonProperty(access = READ_ONLY)` ([CongeDto.java:17](src/main/java/app/domain/rh/conge/CongeDto.java)) — l'affectation ne passe que par le chemin de création imbriqué `POST /api/employe/{id}/conge`. Design net.

Deux notes de contrat, à communiquer côté frontend :
- Un `employe` envoyé dans un corps POST/PUT est maintenant **silencieusement ignoré** — le contrôle « Employe ID mismatch » → 400 de la v1 a disparu. C'est un choix défendable (READ_ONLY documenté) mais c'est un changement de comportement observable.
- Même si `READ_ONLY` était mal honoré à la désérialisation (le support sur les composants de record a eu des caprices selon les versions de Jackson), le code n'exploite plus `dto.employe()` en entrée : la protection est **structurelle**, pas seulement déclarative. Bon réflexe. Un test manuel rapide (PUT avec `employe` renseigné → vérifier qu'il est ignoré) fermerait définitivement le point.

### 3.6 Autres correctifs vérifiés ✅

- **Tri stable** (M1) : implémentation supérieure à la demande — tie-breaker `id` ajouté même quand l'utilisateur trie (`sort.and(Sort.by("id"))`), ce qui garantit un ordre total sur les colonnes non uniques.
- **`master.xml`** (M9) : ordre topologique correct ; les `loadData` s'exécutent désormais après création des tables référencées.
- **Unicité de `code`** (m5) : appliquée dans l'entité **et** le schéma, seeds CSV vérifiés compatibles (M/F, CEL/MAR/DIV/VEU, MLD/PAY — aucun doublon).
- **`denyAll()`** (M6) : posture fail-closed en place.

---

## 4. Nouveaux points de vigilance introduits par cette itération

### V1 — Les changesets modifiés cassent les bases existantes (action locale requise) ⚠️

Les changesets **déjà exécutés** ont été modifiés en place : ajout de `unique="true"` sur `code` (3 tables), `employe_id` non-nullable, `context="@dev"` sur le seed utilisateur. Sur toute base ayant déjà tourné (votre base locale `crud_db`), Liquibase va **refuser de démarrer** (checksum mismatch), et même en forçant les checksums, les contraintes ajoutées dans un changeset déjà joué ne seraient jamais appliquées physiquement.

Ce n'est pas un défaut — `AGENTS.md` assume la réécriture pré-release avec reset destructif — mais c'est une **action à faire maintenant** : reset local (`SPRING_LIQUIBASE_DROP_FIRST=true` une fois, ou `init.sql`) avant le prochain démarrage. Et le rappel v1 tient plus que jamais : dès la première base client réelle, cette pratique doit basculer en append-only. Suggestion : noter la date/version de « gel » des changesets dans `AGENTS.md` le moment venu.

### V2 — Rien n'est commité (29 fichiers modifiés, 11 nouveaux, 4 supprimés) ⚠️

L'intégralité du travail — y compris la correction du XML invalide qui rend le dernier commit `bab524b` non fonctionnel au clone — vit dans le working tree. Un `git checkout` malheureux efface tout. Commiter maintenant, idéalement en 2–3 commits thématiques (sécurité / erreurs+mappers / Liquibase) pour garder un historique lisible.

### V3 — Divergence possible entre le code retouché et les templates du générateur ℹ️

Cette itération a été faite (à raison) directement dans l'application générée. Si les templates du générateur n'ont pas reçu les mêmes changements, la prochaine génération écrasera ou contredira ces correctifs. Les changements v2 sont exactement ceux qui doivent **remonter dans les templates** : `ApiExceptionHandler`, le pattern `*Mapper` + repositories de référence, le tri stable, la résolution de secret, le contexte `@dev`, l'ordre topologique de `master.xml`, la règle « pas de changeset vide ». C'est le point structurant de la suite.

---

## 5. Constats majeurs restants (inchangés depuis v1, rappel court)

### 5.1 M2 — `Page<EmployeDto>` sérialisé directement

Toujours le principal risque de contrat : la structure JSON de `PageImpl` n'est pas garantie par Spring Data (warning officiel depuis 3.3). Recommandation inchangée, avec préférence renforcée pour le **`PageDto` explicite dans `core`** maintenant que le projet possède déjà son format d'erreur (Problem Details) : posséder aussi son format de page est cohérent, et le frontend n'a plus qu'un seul contrat à connaître.

### 5.2 M7 — Autorisation par rôle

`ROLE_ADMIN`/`ROLE_USER` circulent dans les tokens mais ne protègent toujours rien : tout utilisateur authentifié peut tout écrire. Première exigence client à anticiper ; la recommandation v1 (matchers explicites générés depuis un attribut de la DSL) reste le bon format pour ce projet.

### 5.3 Reste du backlog v1

Sans changement : kit de livraison (compose, Dockerfile, `.env.example`, `/health`) — le plus rentable pour l'objectif « monter vite chez un client » ; tests générés (§6) ; openapi.yaml statique ; et les mineurs listés au §2 (langue des messages — devenue plus visible avec Problem Details —, champs dupliqués `id`/`idX`, `code` absent de `ReferenceDataDto`, `rememberMe`, issuer, rate limiting, `@Version`, timestamps, graceful shutdown, `@Transactional(readOnly = true)` sur `ReferenceDataService`).

---

## 6. Recommandation maintenue : les tests générés

L'itération v2 illustre exactement l'argument v1 : huit correctifs de comportement ont été livrés d'un coup, **vérifiés uniquement par compilation**. Le tri stable, le 409 sur doublon, le 404 sur référence inexistante, le seed gaté par contexte, le `READ_ONLY` sur `employe` — chacun de ces comportements est aujourd'hui non prouvé et peut régresser à la prochaine retouche (humaine ou IA). Un unique template de test d'intégration par entité (Testcontainers PostgreSQL : cycle CRUD + cas d'erreur conventionnels) aurait validé les huit d'un coup, et son coût de maintenance est nul puisqu'il se régénère. C'est l'investissement n°1 restant sur la robustesse.

---

## 7. Plan d'action restant

| # | Action | Origine | Effort |
|---|---|---|---|
| 1 | Reset de la base locale (checksums), puis **commit** de l'ensemble (2–3 commits thématiques) | V1, V2 | 30 min |
| 2 | Reporter les changements v2 dans les templates du générateur | V3 | selon générateur |
| 3 | Contrat de page stable (`PageDto` dans `core`) | M2 | 1–2 h |
| 4 | Documenter/renforcer la convention profil `prod` (ou inverser la garde du secret dev) ; secret dev aléatoire par projet généré | §3.1 | 1 h |
| 5 | Supprimer les try/catch redondants des resources (l'advice suffit) — moins de code généré | §3.3 | 1 h |
| 6 | Passer les messages d'erreur en français (ils sont désormais le contrat affiché) | m3 | 1 h |
| 7 | Autorisation par rôle pilotée par la DSL | M7 | 1 j |
| 8 | Kit livraison : compose, Dockerfile, `.env.example`, `/health` | m11, m12 | 1 j |
| 9 | Tests d'intégration générés par entité | §6 | 1–2 j |
| 10 | Mineurs restants au fil de l'eau (`ReferenceDataDto.code`, `rememberMe`, issuer, `@Version`, timestamps, graceful shutdown…) | §5.3 | — |

---

## 8. Conclusion

La v2 corrige l'essentiel de ce qui rendait le socle non livrable : les trois critiques de sécurité/robustesse sont clos avec des implémentations soignées (le `@dev` fail-closed et le double verrou du secret JWT sont au-dessus du niveau demandé), le format d'erreur est unifié, et le refactoring en mappers améliore à la fois la correction (M3, M4) et la propreté du modèle de génération — le tout documenté en cohérence. Il reste deux chantiers de fond (contrat de page, rôles), un impératif immédiat (reset + commit), et un chantier structurant : faire remonter ces correctifs dans les templates pour que la prochaine application générée naisse avec. La trajectoire vers « la meilleure application CRUD possible » est bonne — et la prochaine marche est celle qui verrouille tout le reste : les tests générés.
