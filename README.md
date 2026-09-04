# Backend CRUD de démonstration

Backend Spring Boot du mini-SIRH utilisé comme cible de référence par Engine. Le projet privilégie un code court, conventionnel et directement montrable : contrôleurs REST, services transactionnels, repositories Spring Data et DTO explicites.

Le domaine RH illustre trois formes de génération :

- liste CRUD simple avec `Departement` ;
- recherche paginée avec `Employe` ;
- relation parent/enfant avec `Employe` et `Conge`.

L'administration des comptes et les rôles possèdent une baseline dans le DSL d'Engine. Le petit noyau manuel y ajoute l'authentification JWT, la gestion des mots de passe et les adaptations de sécurité ; il ne cherche pas à être une plateforme IAM.

## Démarrage

Prérequis : Java 25 et PostgreSQL. Le wrapper Maven est fourni. La configuration locale utilise une base et un compte de démonstration nommés `rh` :

```bash
sudo -u postgres psql -c "CREATE ROLE rh LOGIN PASSWORD 'rh'"
sudo -u postgres createdb -O rh rh
```

```bash
./mvnw spring-boot:run
```

Liquibase remet le schéma à zéro, recrée les tables et recharge les données de démonstration à chaque démarrage. Hibernate valide ensuite que les entités correspondent au schéma. Ce comportement destructif est volontaire pour cette application de démonstration.

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

La clé JWT, l'issuer et l'origine CORS locale sont écrits directement dans `application.yml` afin que la démonstration démarre sans configuration supplémentaire.

## API d'administration

Les rôles `ROLE_ADMIN` et `ROLE_GESTIONNAIRE_RH` sont des référentiels fixes dont le `libelle` sert directement d'autorité Spring Security. Ils ne possèdent pas de CRUD REST et ne peuvent donc être ni créés ni modifiés par l'API.

| Méthode | Route | Corps principal |
|---|---|---|
| `POST` | `/api/admin/accounts` | `username`, `password`, `role` |
| `GET` | `/api/admin/accounts` | — |
| `GET` | `/api/admin/accounts/{id}` | — |
| `PUT` | `/api/admin/accounts/{id}` | `role`, `activated`, `version` |
| `PUT` | `/api/admin/accounts/{id}/password` | `password` |

Exemples :

```json
{
  "username": "nouveau-compte",
  "password": "mot-de-passe-initial",
  "role": {
    "id": "2"
  }
}
```

```json
{
  "role": {
    "id": "1"
  },
  "activated": true,
  "version": 0
}
```

Les réponses n'exposent jamais le hash :

```json
{
  "id": "3",
  "username": "nouveau-compte",
  "role": {
    "id": "2",
    "libelle": "ROLE_GESTIONNAIRE_RH"
  },
  "activated": true,
  "version": 0
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

Engine décrit le domaine RH ainsi que la structure commune de `Account` et `Role`. L'encodage et la réinitialisation des mots de passe, la normalisation des identifiants et l'authentification restent des adaptations locales de `domain/admin` et de `core/security` afin de ne jamais exposer un champ sensible dans le code généré.

## Tests

Tests rapides et contrats HTTP :

```bash
./mvnw test
```

Le test d'intégration utilise la même base locale `rh`, dans un schéma temporaire isolé :

```bash
./mvnw verify
```

La suite crée un schéma aléatoire `rh_it_<identifiant>`, exécute réellement Liquibase, vérifie les données de démonstration et les trois patrons générés, puis supprime uniquement ce schéma. Elle ne nécessite ni Docker ni variable d'environnement.
