# Backend CRUD de démonstration

Backend Spring Boot du mini-SIRH utilisé comme cible de référence par Engine. Le projet privilégie un code court, conventionnel et directement montrable : contrôleurs REST, services transactionnels, repositories Spring Data et DTO explicites.

Le domaine RH illustre trois formes de génération :

- liste CRUD simple avec `Departement` ;
- recherche paginée avec `Employe` ;
- relation parent/enfant avec `Employe` et `Conge`.

Le petit noyau manuel ajoute une authentification JWT et l'administration des comptes. Il ne cherche pas à être une plateforme IAM.

## Démarrage

Prérequis : Java 25, Maven et PostgreSQL.

```bash
export DB_URL='jdbc:postgresql://localhost:5432/rh'
export DB_USERNAME='<utilisateur>'
export DB_PASSWORD='<mot-de-passe>'
export APP_SECURITY_JWT_SECRET_BASE64="$(openssl rand -base64 32)"
mvn spring-boot:run
```

Flyway applique les migrations `V1` à `V5`, puis Hibernate valide le schéma sans le modifier.

Deux comptes de démonstration sont insérés :

| Username | Mot de passe | Rôle public |
|---|---|---|
| `admin` | `Admin-local-2026!` | `ROLE_ADMIN` |
| `gestionnaire-rh` | `Gestionnaire-local-2026!` | `ROLE_GESTIONNAIRE_RH` |

## Authentification

```bash
curl -X POST http://localhost:8080/api/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"Admin-local-2026!"}'
```

La réponse contient `accessToken`, `tokenType` et `expiresIn`. Le JWT HS256 contient seulement les informations attendues par le frontend :

```json
{
  "sub": "admin",
  "role": "ROLE_ADMIN",
  "iat": 1788249600,
  "exp": 1788253200,
  "iss": "crud-reference"
}
```

Une requête protégée utilise ensuite :

```text
Authorization: Bearer <accessToken>
```

Le fonctionnement est volontairement simple : le mot de passe Argon2id est vérifié dans `AuthService`, puis le JWT est autonome jusqu'à son expiration. Il n'existe ni refresh token, blacklist, révocation globale, bootstrap dynamique, cache de sécurité, ni lecture du compte à chaque requête.

Une désactivation, un changement de rôle ou une réinitialisation de mot de passe s'applique donc aux prochaines connexions ; un JWT déjà émis reste valable jusqu'à son expiration. Le TTL vaut une heure par défaut et se règle avec `APP_SECURITY_JWT_TTL`. Un déploiement plus exposé peut réduire ce TTL et placer la limitation des tentatives au niveau du reverse proxy.

Le secret `APP_SECURITY_JWT_SECRET_BASE64` est obligatoire et doit représenter au moins 32 octets. `APP_SECURITY_JWT_ISSUER` et `APP_SECURITY_CORS_ALLOWED_ORIGINS` sont optionnels.

## API d'administration

Les rôles `ADMIN` et `GESTIONNAIRE_RH` sont des données de référence fixes. Ils ne possèdent pas de CRUD REST.

| Méthode | Route | Corps principal |
|---|---|---|
| `POST` | `/api/admin/accounts` | `username`, `password`, `role` |
| `GET` | `/api/admin/accounts` | — |
| `GET` | `/api/admin/accounts/{id}` | — |
| `PUT` | `/api/admin/accounts/{id}` | `role`, `activated` |
| `PUT` | `/api/admin/accounts/{id}/password` | `password` |

Exemples :

```json
{
  "username": "nouveau-compte",
  "password": "mot-de-passe-initial",
  "role": "ROLE_GESTIONNAIRE_RH"
}
```

```json
{
  "role": "ROLE_ADMIN",
  "activated": true
}
```

Les réponses n'exposent jamais le hash :

```json
{
  "id": "3",
  "username": "nouveau-compte",
  "role": {
    "id": "2",
    "code": "ROLE_GESTIONNAIRE_RH",
    "libelle": "Gestionnaire RH"
  },
  "activated": true
}
```

## API RH

| Méthode | Route | Usage |
|---|---|---|
| `POST` | `/api/rh/departements` | Créer un département |
| `GET` | `/api/rh/departements` | Lister les départements |
| `GET` | `/api/rh/departements/{id}` | Consulter un département |
| `PUT` | `/api/rh/departements/{id}` | Modifier un département |
| `DELETE` | `/api/rh/departements/{id}` | Supprimer un département |
| `POST` | `/api/rh/employes` | Créer un employé |
| `POST` | `/api/rh/employes/filtrer` | Filtrer et paginer les employés |
| `GET` | `/api/rh/employes/{id}` | Consulter un employé |
| `PUT` | `/api/rh/employes/{id}` | Modifier un employé |
| `DELETE` | `/api/rh/employes/{id}` | Supprimer un employé |
| `POST` | `/api/rh/employes/{idEmploye}/conges` | Créer un congé |
| `GET` | `/api/rh/employes/{idEmploye}/conges` | Lister les congés d'un employé |
| `GET` | `/api/rh/conges/{id}` | Consulter un congé |
| `PUT` | `/api/rh/conges/{id}` | Modifier un congé |
| `DELETE` | `/api/rh/conges/{id}` | Supprimer un congé |

Les relations de formulaire utilisent des références courtes comme `{"id":"2","libelle":"…"}`. Le backend utilise l'identifiant et n'a pas besoin d'un mapper intermédiaire supplémentaire.

Les réponses modifiables du domaine RH contiennent `version`, à renvoyer lors d'un `PUT`. Une version périmée produit un HTTP 409. Les identifiants Java `Long` sont sérialisés en chaînes afin d'éviter une perte de précision dans JavaScript.

La recherche d'employés reçoit ses filtres dans le corps de `POST /api/rh/employes/filtrer`; `page`, `size` et `sort` restent des paramètres de requête. La réponse utilise `PageResponse` plutôt que le contrat interne de Spring Data.

## Organisation

```text
app/
├── core/
│   ├── exception/     contrat d'erreur commun
│   ├── pagination/    contrat de page REST
│   ├── persistence/   BaseEntity et helpers JPA
│   ├── reference/     référence JSON et identifiants sûrs pour JavaScript
│   └── security/      configuration, login et JWT
└── domain/
    ├── admin/         Account et Role
    └── rh/            domaine générable
```

La sécurité et `domain/admin` forment un petit noyau manuel. Engine se concentre sur le domaine métier RH, car générer une moitié de sécurité conduirait à exposer des champs techniques ou à produire des actions artificielles.

## Tests

Tests unitaires et contrats HTTP :

```bash
mvn test
```

Les tests PostgreSQL peuvent utiliser une base locale dédiée, sans Docker :

```bash
export TEST_DB_URL='jdbc:postgresql://localhost:5432/rh_reference_it'
export TEST_DB_USERNAME='<utilisateur>'
export TEST_DB_PASSWORD='<mot-de-passe>'
export TEST_DB_ALLOW_SCHEMA_MANAGEMENT='true'
mvn verify
```

La suite crée un schéma aléatoire `rh_it_<identifiant>`, exécute réellement Flyway, laisse Hibernate valider le modèle, puis supprime uniquement ce schéma. Sans `TEST_DB_URL`, le scénario alternatif Testcontainers reste disponible.
