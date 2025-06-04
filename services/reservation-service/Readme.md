# 📚 Reservation Service – Book & Hold REST API

Servizio REST per la gestione delle prenotazioni di libri in una biblioteca municipale. Il progetto è parte della piattaforma `municipal-library-platform` e si occupa delle risorse `Book` e `Hold`, fornendo un'interfaccia CRUD e funzionalità avanzate di ricerca, il tutto documentato tramite OpenAPI/Swagger.

---

## 🛠️ Tecnologie Utilizzate

- **Java 21**
- **Spring Boot 3.3.0**
- **Maven**
- **PostgreSQL**
- **Flyway 10.10.0** – migrazioni SQL
- **Spring Data JPA**
- **Springdoc OpenAPI 2**
- **Swagger UI** – documentazione interattiva
- **Lombok** – per riduzione del boilerplate
- **MockMvc** – test di integrazione REST

---

## ▶️ Build & Avvio

### ✅ Requisiti

- Java 21
- Maven 3.9+
- PostgreSQL attivo su `localhost:5432`

### ⚙️ Configurazione `application.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/reservation_db
    username: postgres
    password: yourpassword
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    clean-disabled: true

 Build del progetto:
 mvn clean install

 🚀 Esecuzione:
 mvn spring-boot:run

🌐 Swagger & OpenAPI
Il servizio include documentazione interattiva generata dinamicamente da OpenAPI 3.
📄 Accessi disponibili:
Swagger UI → http://localhost:8080/swagger-ui/index.html
OpenAPI JSON → http://localhost:8080/v3/api-docs
Tutti gli endpoint sono annotati con @Tag, @Operation, @ApiResponses, e i DTO sono descritti con @Schema.

📘 Endpoints Principali
📚 Book
| Metodo | Endpoint      | Descrizione                               |
| ------ | ------------- | ----------------------------------------- |
| GET    | `/books`      | Ricerca avanzata per titolo, autore, ecc. |
| GET    | `/books/{id}` | Recupera un libro per ID                  |
| POST   | `/books`      | Crea un nuovo libro                       |
| PUT    | `/books/{id}` | Aggiorna i dati di un libro esistente     |
| DELETE | `/books/{id}` | Cancella un libro per ID                  |

📦 Hold
| Metodo | Endpoint              | Descrizione                                                     |
| ------ | --------------------- | --------------------------------------------------------------- |
| GET    | `/holds`              | Ricerca combinata per title, author, status, pickupBranch, ecc. |
| GET    | `/holds/{id}`         | Recupera una prenotazione per ID                                |
| POST   | `/holds`              | Crea una nuova prenotazione (con prevenzione duplicati)         |
| PUT    | `/holds/{id}`         | Aggiorna lo stato o altri campi modificabili                    |
| DELETE | `/holds/{id}`         | Cancella una prenotazione (hard delete)                         |
| DELETE | `/holds/{id}/cancel`  | Annulla una prenotazione (soft delete → stato = CANCELLED)      |
| GET    | `/holds/{id}/book`    | Recupera il libro associato alla prenotazione                   |
| GET    | `/holds/{id}/details` | Restituisce `HoldDetailsDto` (Hold + Book aggregati)            |

🔐 Regole e Vincoli
⚠️ Prevenzione duplicati: non è possibile creare più Hold per lo stesso patronId e bibId.
✅ Validazione FK: ogni bibId in una Hold deve esistere nella tabella books.
🔒 Flyway Clean disabilitato: per evitare la perdita involontaria di dati nei volumi.

🧪 Test di Integrazione
I test di integrazione sono implementati con Spring Boot Test + MockMvc.

Copertura:
Creazione, modifica, eliminazione Book e Hold
Prevenzione duplicati POST /holds
Query avanzate per GET /books, GET /holds
Endpoints relazionali (/holds/{id}/book, /details)
Validazione su bibId, status, pickupBranch, ecc.

Per eseguire:
mvn test

📁 Struttura del Progetto
reservation-service/
├── src/
│   └── main/
│       ├── java/it/comune/library/reservation/
│       │   ├── controller/
│       │   ├── dto/
│       │   ├── domain/
│       │   ├── mapper/
│       │   ├── repository/
│       │   └── ReservationServiceApplication.java
│       └── resources/
│           ├── application.yml
│           └── db/migration/ (script Flyway)
├── pom.xml
└── README.md

📌 Prossimi Step Possibili
🔐 Integrazione con sistema di autenticazione (es. OAuth2, JWT)
📡 Separazione dei microservizi per Patron, Loan, Notification
🔎 Logging avanzato (Loki, Promtail) + Prometheus Metrics