# Backend CRUD de référence

Application Spring Boot autonome illustrant trois patrons de génération dans deux domaines fonctionnels :

- liste simple : `Departement` et `Role`, avec CRUD et liste complète ordonnée ;
- entité recherchable : `Employe` et `Account`, avec références et recherche paginée ;
- enfant agrégé : `Conge`, créé et listé sous son employé, sans possibilité de transfert.

Le projet utilise Java 25 LTS, Spring Boot 4.1.0, PostgreSQL, Flyway, Spring Security Resource Server et Maven Wrapper. Il contient une authentification locale JWT stateless adaptée à un petit déploiement mono-instance. Docker n'est utilisé que par Testcontainers pour les tests PostgreSQL autonomes.

## Démarrage

Prérequis : un JDK 25 et une base PostgreSQL vide dédiée à l'application.

Sous PowerShell :

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/rh'
$env:DB_USERNAME = '<utilisateur>'
$env:DB_PASSWORD = '<mot-de-passe>'
$env:APP_SECURITY_JWT_SECRET_BASE64 = '<secret-base64-de-32-octets-minimum>'
./mvnw.cmd spring-boot:run
```

Sous un shell Unix :

```bash
export DB_URL='jdbc:postgresql://localhost:5432/rh'
export DB_USERNAME='<utilisateur>'
export DB_PASSWORD='<mot-de-passe>'
export APP_SECURITY_JWT_SECRET_BASE64='<secret-base64-de-32-octets-minimum>'
./mvnw spring-boot:run
```

Flyway applique les migrations `V1` à `V4` au démarrage. Hibernate exécute ensuite `ddl-auto=validate` et ne crée ni ne modifie le schéma. `V4__insert_demo_data.sql` fournit les deux comptes locaux documentés dans la section d'authentification.

Pour lancer le JAR construit :

```powershell
./mvnw.cmd package
java -jar target/rh-reference-backend-1.0.0.jar
```

## API

| Méthode | Route | Usage |
|---|---|---|
| `POST` | `/api/rh/departement` | Créer un département (201 avec corps, sans `Location`) |
| `GET` | `/api/rh/departement` | Liste ordonnée par nom |
| `GET` | `/api/rh/departement/{id}` | Consulter un département |
| `PUT` | `/api/rh/departement/{id}` | Modifier un département |
| `DELETE` | `/api/rh/departement/{id}` | Supprimer un département |
| `POST` | `/api/rh/employe` | Créer un employé (201 avec corps, sans `Location`) |
| `GET` | `/api/rh/employe/{id}` | Consulter un employé |
| `POST` | `/api/rh/employe/filtrer` | Filtrer et paginer les employés |
| `PUT` | `/api/rh/employe/{id}` | Modifier un employé |
| `DELETE` | `/api/rh/employe/{id}` | Supprimer un employé |
| `POST` | `/api/employes/{employeId}/conges` | Créer un congé pour un employé |
| `GET` | `/api/employes/{employeId}/conges` | Lister les congés de l'employé |
| `GET` | `/api/conges/{id}` | Consulter un congé |
| `PUT` | `/api/conges/{id}` | Modifier un congé sans changer son parent |
| `DELETE` | `/api/conges/{id}` | Supprimer un congé |
| `POST` | `/api/admin/roles` | Créer un rôle |
| `GET` | `/api/admin/roles` | Liste ordonnée par code puis identifiant |
| `GET` | `/api/admin/roles/{id}` | Consulter un rôle |
| `PUT` | `/api/admin/roles/{id}` | Modifier le libellé ou la description d'un rôle |
| `DELETE` | `/api/admin/roles/{id}` | Supprimer physiquement un rôle inutilisé |
| `POST` | `/api/admin/accounts` | Créer un compte associé à un rôle |
| `GET` | `/api/admin/accounts` | Rechercher les comptes |
| `GET` | `/api/admin/accounts/{id}` | Consulter un compte |
| `PUT` | `/api/admin/accounts/{id}` | Modifier un compte et son rôle unique |
| `DELETE` | `/api/admin/accounts/{id}` | Supprimer physiquement un compte |
| `POST` | `/api/auth/login` | Obtenir un JWT d'accès |
| `GET` | `/api/auth/me` | Lire l'identité technique courante |
| `PUT` | `/api/auth/password` | Changer son mot de passe et révoquer ses JWT |
| `POST` | `/api/auth/logout-all` | Révoquer tous ses JWT |
| `PUT` | `/api/admin/accounts/{id}/password` | Créer ou réinitialiser la credential locale |

Le filtre des employés est envoyé comme corps JSON à `POST /api/rh/employe/filtrer`. Il couvre les champs textuels, les références `sexe`, `situationFamiliale` et `departement`, ainsi que les intervalles `debutDateNaissance`/`finDateNaissance` et `debutDateEntree`/`finDateEntree`. Les références ont la forme `{"id":"…"}` et sont comparées par identifiant. `page`, `size` et `sort` restent des paramètres de requête Spring (`sort=prenom,desc`, par exemple). L'identifiant est ajouté comme dernier critère de tri afin de stabiliser la pagination.

Conformément au DSL, `matricule`, `nom`, `prenom` et `dateNaissance` sont obligatoires. `sexe`, `situationFamiliale`, `dateEntree`, `departement` et les autres textes sont facultatifs. `email` est volontairement un simple `Text` de 250 caractères : le backend ne lui ajoute ni validation de format ni normalisation non exprimée par le DSL.

La recherche des comptes accepte `username`, `displayName`, `email`, `active`, `roleId`, `page`, `size`, `sort` et `direction`. Les filtres textuels sont partiels, insensibles à la casse et limités à 250 caractères. `active` et `roleId` sont des filtres exacts. Les tris autorisés sont `username`, `displayName`, `email`, `active` et `role`; le tri `role` utilise le code du rôle. La pagination et le départage final par `id ASC` suivent le même contrat que la recherche des employés.

`EmployeService` retourne un `Page<EmployeResponse>`, utile à la logique applicative. Le contrôleur convertit ce résultat en `PageResponse`, contrat REST stable qui n'expose pas la structure interne de Spring :

```json
{
  "items": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": true
}
```

Chaque fonctionnalité possède des DTO explicites : `CreateRequest`, `UpdateRequest`, `Response` et, lorsqu'une association est imbriquée, `Reference`. Les entités JPA ne font donc pas partie du contrat JSON. Les mappers associés sont purs : ils ne consultent aucun repository et ne contiennent aucune règle métier.

Chaque réponse modifiable contient un champ numérique `version`. Ce champ doit être renvoyé dans un `PUT`; une version périmée produit un HTTP 409 au lieu d'écraser silencieusement une modification concurrente. `@Version` conserve en plus la garantie atomique JPA lorsque deux transactions ont lu la même version. Aucun ETag n'est utilisé.

`PUT` protège ainsi le snapshot lu par le client grâce à la version transmise. `DELETE` recharge l'état courant et ne protège pas contre une suppression déclenchée depuis un écran ancien. Cette asymétrie est un choix volontaire du template CRUD minimal : les routes `DELETE` n'acceptent ni version ni ETag.

Les identifiants restent des `Long` dans Java et PostgreSQL mais sont sérialisés comme chaînes dans les réponses et références JSON, par exemple `"id":"9007199254740993"`. Cela évite toute perte de précision dans JavaScript. Les versions, compteurs et métadonnées de pagination restent numériques. Les identifiants reçus dans les corps JSON acceptent leur représentation sous forme de chaîne.

Les associations sont représentées par une référence JSON courte et explicite. Les champs facultatifs nuls sont omis.

### Module Admin V1

`Role` est un référentiel sans données initiales. Son `code`, composé de lettres ASCII, chiffres ou underscores et commençant par une lettre, contient entre 2 et 50 caractères. Il est nettoyé, stocké en majuscules et devient immuable après la création. Le libellé obligatoire est nettoyé et limité à 150 caractères; la description facultative est limitée à 1000 caractères.

`Account` possède un `username` unique de 3 à 100 caractères, composé de lettres ASCII, chiffres, points, underscores ou tirets. Il est nettoyé et stocké en minuscules. Le `displayName` obligatoire est nettoyé et limité à 150 caractères. L'e-mail facultatif est nettoyé, stocké en minuscules, limité à 254 caractères et unique lorsqu'il est présent; une valeur vide devient `null`. Plusieurs comptes sans e-mail sont donc autorisés. `active` reste un simple booléen CRUD.

Chaque compte référence exactement un rôle obligatoire par une clé étrangère restrictive : plusieurs comptes peuvent partager le même rôle, mais il n'existe ni collection inverse, ni table de jointure, ni cascade de suppression. Les comptes et les rôles inutilisés sont supprimés physiquement. La suppression d'un rôle encore référencé retourne HTTP 409 grâce à la contrainte PostgreSQL.

`domain.admin` reste propriétaire des données fonctionnelles administratives. L'authentification, les credentials et les JWT appartiennent exclusivement à `core.security`; aucune permission métier fine n'est introduite.

### Frontière Admin → sécurité

`domain.admin` reste propriétaire des entités `Account` et `Role`. Le package `core.security.account` définit uniquement un contrat interne de lecture, `SecurityAccountProvider`, et son snapshot technique minimal : identifiant, username canonique, état actif et code du rôle. L'implémentation `AccountSecurityAdapter` reste dans `domain.admin.account` et transforme les entités en snapshot dans une transaction en lecture seule.

La dépendance de compilation va donc du domaine vers le cœur, jamais dans l'autre sens :

```text
core.security.account
        ▲
        │ implémente
domain.admin.account.AccountSecurityAdapter
        │
        └── AccountRepository ──> Account ──> Role
```

`Account` n'implémente pas `UserDetails`, `Role` n'implémente pas `GrantedAuthority` et aucun objet JPA ou DTO REST ne traverse cette frontière. Le bootstrap utilise un second port minimal, `SecurityAdminBootstrapProvider`, implémenté par `AccountBootstrapAdapter`. Ainsi, `core.security` n'importe jamais `app.domain`.

## Authentification locale JWT

La signature est exclusivement HS256, explicitement imposée à `NimbusJwtEncoder` et `NimbusJwtDecoder`. Le secret doit être un Base64 valide représentant au moins 32 octets réels. Il n'a aucune valeur par défaut et l'application refuse de démarrer s'il est absent, invalide ou trop court. Pour générer une valeur locale :

```powershell
$bytes = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
$env:APP_SECURITY_JWT_SECRET_BASE64 = [Convert]::ToBase64String($bytes)
```

```bash
export APP_SECURITY_JWT_SECRET_BASE64="$(openssl rand -base64 32)"
```

Les réglages optionnels sont `APP_SECURITY_JWT_ISSUER`, `APP_SECURITY_JWT_AUDIENCE`, `APP_SECURITY_JWT_TTL` (PT15M par défaut, PT1H maximum) et `APP_SECURITY_JWT_CLOCK_SKEW` (PT30S par défaut, PT1M maximum). Changer le secret invalide volontairement tous les JWT existants.

Le token contient uniquement `sub` (identifiant stable du compte), `iss`, `aud`, `iat`, `nbf`, `exp`, un `jti` aléatoire et `credential_version`. Il ne contient ni rôle, ni username, ni e-mail, ni display name. À chaque requête bearer, l'application relit le compte par son identifiant, son état actif, son username et son rôle courant, puis lit uniquement `tokenVersion` depuis `account_credential`. Il n'existe ni cache de sécurité, ni blacklist, ni stockage des tokens.

Les mots de passe sont stockés dans `account_credential`, séparée de l'entité fonctionnelle `Account`, avec un hash préfixé `{argon2id}`. Les paramètres sont 19 MiB de mémoire, 2 itérations et un parallélisme de 1. La politique accepte 15 à 128 caractères, Unicode et espaces compris; aucune transformation, normalisation ou suppression d'espaces n'est appliquée. `@Version` protège les écritures concurrentes tandis que `tokenVersion` sert exclusivement à la révocation. La FK vers `account` utilise `ON DELETE CASCADE`.

### Comptes de démonstration

`V4__insert_demo_data.sql` crée deux identités locales séparées :

| Usage | Username | Mot de passe | Rôle |
|---|---|---|---|
| Administration | `admin` | `Admin-local-2026!` | `ADMIN` |
| Ressources humaines | `gestionnaire-rh` | `Gestionnaire-local-2026!` | `GESTIONNAIRE_RH` |

Ces identifiants sont réservés au développement et doivent être remplacés avant tout déploiement partagé. Les mots de passe ne sont pas stockés en clair dans la base : V4 insère uniquement leurs hashes Argon2id. Le bootstrap doit rester désactivé avec ces données, car il refuse volontairement de s'exécuter lorsqu'un compte existe déjà.

### Appels usuels

```bash
curl -i -X POST https://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"Admin-local-2026!"}'

curl https://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $TOKEN"

curl -i -X PUT https://localhost:8080/api/auth/password \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"currentPassword":"mot-de-passe-local","newPassword":"nouveau-mot-de-passe-local"}'

curl -i -X POST https://localhost:8080/api/auth/logout-all \
  -H "Authorization: Bearer $TOKEN"
```

Le login répond avec `Cache-Control: no-store` et `Pragma: no-cache`. Un bearer absent, invalide, expiré ou révoqué produit `401 AUTHENTICATION_REQUIRED` et `WWW-Authenticate: Bearer`; un login incorrect produit `401 INVALID_CREDENTIALS`; un rôle insuffisant produit `403 ACCESS_DENIED`; la limitation du login produit `429 TOO_MANY_LOGIN_ATTEMPTS`.

La désactivation ou suppression d'un compte et un changement de mot de passe, reset Admin ou logout-all invalident immédiatement les JWT déjà émis. Un changement de rôle est effectif dès la requête suivante et un username renommé devient aussitôt le nom du principal. Après un changement de mot de passe ou logout-all, le client doit supprimer son token local et se reconnecter.

L'API est stateless : pas de session, cookie d'authentification, refresh token, formulaire Basic ou token dans une URL. Les bearer sont acceptés uniquement dans `Authorization`. CSRF est désactivé pour cette raison précise; l'introduction future d'un cookie d'authentification imposerait de revoir ce choix. Les origines CORS exactes sont configurées par `APP_SECURITY_CORS_ALLOWED_ORIGINS` (aucune par défaut, jamais `*`, credentials désactivés). HTTPS est obligatoire en production.

Le limiteur d'essais utilise deux caches Caffeine bornés, par username canonique et par adresse source. Avant l'authentification, une opération atomique courte purge les tentatives expirées, vérifie les deux seuils puis réserve immédiatement une place dans les deux buckets. Le verrou est libéré avant tout accès au compte ou calcul Argon2. Chaque tentative admise, réussie ou non, reste comptée pour l'adresse; un succès réinitialise uniquement le bucket du username prouvé. Un échec n'est pas compté une seconde fois après Argon2. Les seuils, fenêtres et tailles sont configurables via `APP_SECURITY_LOGIN_*`.

Les DTO de sécurité limitent le username à 100 unités UTF-16 et chaque mot de passe transporté à 256 unités UTF-16 avant le limiteur et Argon2. `PasswordPolicy` reste l'autorité pour les nouveaux mots de passe, avec 15 à 128 points de code Unicode sans transformation. Ces bornes de champs ne remplacent pas une limite globale de taille du corps HTTP, qui doit aussi être configurée sur le reverse proxy en production.

Le limiteur convient à une seule instance; plusieurs instances nécessiteraient un stockage partagé ou une limitation au reverse proxy. L'application ne lit pas elle-même `X-Forwarded-For`; la confiance proxy relève de la configuration serveur.

Les erreurs utilisent le contrat suivant :

```json
{
  "code": "VALIDATION_FAILED",
  "message": "La requête n'est pas valide",
  "path": "/api/rh/departement",
  "fieldErrors": [
    {"field": "nom", "code": "NotBlank", "message": "ne doit pas être vide"}
  ]
}
```

Les codes principaux sont `INVALID_REQUEST`, `VALIDATION_FAILED`, `RESOURCE_NOT_FOUND`, `CONFLICT`, `DATA_INTEGRITY_VIOLATION`, `INVALID_CREDENTIALS`, `AUTHENTICATION_REQUIRED`, `ACCESS_DENIED` et `TOO_MANY_LOGIN_ATTEMPTS`.

## Tests

Tests unitaires et contrats HTTP :

```powershell
./mvnw.cmd test
```

Build complet, avec PostgreSQL Testcontainers :

```powershell
./mvnw.cmd clean verify
```

Par défaut, `PostgreSqlContainerIT` démarre automatiquement PostgreSQL 17.6. Chaque exécution crée un schéma aléatoire `rh_it_<identifiant>`, y applique réellement la migration Flyway et laisse Hibernate valider ce même schéma. Toutes les connexions de test utilisent ce schéma comme schéma courant. Docker doit être disponible ; son absence fait échouer clairement la validation au lieu d'ignorer silencieusement les tests.

Une base PostgreSQL externe et strictement dédiée aux tests peut remplacer Testcontainers :

```powershell
$env:TEST_DB_URL = 'jdbc:postgresql://localhost:5432/rh_reference_it'
$env:TEST_DB_USERNAME = '<utilisateur>'
$env:TEST_DB_PASSWORD = '<mot-de-passe>'
$env:TEST_DB_ALLOW_SCHEMA_MANAGEMENT = 'true'
./mvnw.cmd clean verify
```

Lorsque `TEST_DB_URL` est définie, le test Testcontainers est désactivé et `PostgreSqlExternalIT` exécute exactement les mêmes scénarios. `TEST_DB_ALLOW_SCHEMA_MANAGEMENT=true` constitue le consentement obligatoire ; sans lui, le contexte échoue avant Flyway et avant toute commande DDL. L'URL doit désigner explicitement une base PostgreSQL et ne doit pas définir elle-même `currentSchema`.

Le compte fourni doit pouvoir créer et supprimer des schémas dans cette base. La suite ne crée et ne supprime jamais la base elle-même : elle crée uniquement son schéma aléatoire vérifié, tronque sans `CASCADE` les tables qualifiées `account_credential`, `account`, `app_role`, `conge`, `employe`, `situation_familiale`, `sexe` et `departement` entre les scénarios, puis supprime ce seul schéma après la suite. Avant chaque nettoyage, une assertion impose que `current_schema()` corresponde exactement au nom isolé attendu ; `public` et les autres schémas ne sont jamais nettoyés.

## Valeurs par défaut proposées pour le générateur

- package vertical plat par fonctionnalité ;
- entité JPA, repository Spring Data, service transactionnel et contrôleur explicites ;
- contrats de création, modification et réponse distincts des entités ;
- mappers statiques, explicites et purs, sans repository ni abstraction commune artificielle ;
- spécification de recherche dédiée, avec champs et tris inscrits en liste blanche ;
- migration Flyway comme source du schéma, validation Hibernate et Open Session in View désactivé ;
- validation Jakarta côté API et contraintes PostgreSQL pour l'intégrité durable ;
- erreurs HTTP communes, références imbriquées courtes et pagination indépendante de Spring ;
- relations `LAZY`, lecture et mapping dans la transaction, sans dépendre d'Open Session in View ;
- verrouillage optimiste avec `@Version` pour toute entité modifiable ;
- identifiants JSON sérialisés en chaînes, sans transformer les autres valeurs `Long` ;
- tri déterministe complété par l'identifiant ;
- suppression physique avec clés étrangères restrictives, afin d'éviter une cascade destructive implicite ;
- mapping écrit en Java ordinaire, sans Lombok, MapStruct, modèle métier parallèle ou couche générique CRUD.

L'unicité reste exacte et sensible à la casse, conformément aux contraintes PostgreSQL ordinaires. Une unicité normalisée ou insensible à la casse devrait être une décision DSL explicite, pas une supposition du générateur.

## Options pilotées par le DSL

- activation de la recherche et de la pagination, champs filtrables et champs triables ;
- taille maximale des pages ;
- unicité d'un champ ;
- canonicalisation configurable d'une chaîne, comme les codes en majuscules ou les identifiants fonctionnels en minuscules ;
- immutabilité d'un champ après création, illustrée par le code du rôle ;
- booléen CRUD obligatoire et filtrable, illustré par `Account.active` ;
- relation obligatoire et forme de sa référence dans les réponses ;
- propriété fonctionnelle d'un enfant, routes de création/liste imbriquées et interdiction de réaffecter le parent ;
- contraintes inter-champs telles que l'ordre des dates ;
- longueurs techniques des chaînes et validation de format telle que l'e-mail.

Restent volontairement hors de ce socle : refresh token, JWT en cookie, MFA, récupération de mot de passe, permissions fines, multi-rôles, audit métier, Keycloak/OIDC, Authorization Server, RSA/JWKS, rotation transparente de clés, blacklist de tokens, suppression logique, historique, observabilité, notifications, événements et workflow de congé.

## Références de versions

- [Spring Boot 4.1 — prérequis système](https://docs.spring.io/spring-boot/system-requirements.html)
- [Spring Boot — migrations Flyway](https://docs.spring.io/spring-boot/how-to/data-initialization.html)
