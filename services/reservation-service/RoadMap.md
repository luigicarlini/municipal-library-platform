🏁 Stato attuale del progetto:
----------------------------------------------------------------------------------------------------------------------------------
1. Prenotazioni (Holds)
----------------------------------------------------------------------------------------------------------------------------------

Crea Prenotazione (POST /holds): Completato e testato, con validazione di duplicati e controllo dell'esistenza del libro.
Recupera Prenotazione (GET /holds/{id}): Completato e testato, funziona correttamente.
Aggiornamento Prenotazione (PUT /holds/{id}): Implementato con validazione dello status e testato con successo via Swagger UI e curl.
Cancelazione (soft-delete) (DELETE /holds/{id}/cancel): Implementato e funzionale.
Eliminazione definitiva (hard-delete) (DELETE /holds/{id}): Completato e funzionante.
Ricerca Avanzata (GET /holds): Implementato il filtro per titolo, autore, stato, genere, anno, posizione, etc.
Dettagli Prenotazione (GET /holds/{id}/details): Restituisce dettagli completi, inclusi i dati del libro, già implementato.

----------------------------------------------------------------------------------------------------------------------------------
2. Gestione dei libri (Books)
----------------------------------------------------------------------------------------------------------------------------------
Crea Libro (POST /books): Completato e testato.
Recupera Libro (GET /books/{id}): Completato e testato.


----------------------------------------------------------------------------------------------------------------------------------
3. Validazioni e Gestione degli errori
----------------------------------------------------------------------------------------------------------------------------------
La gestione degli errori per ID non trovato e duplicazioni è ben impostata.
La validazione dello status nella funzione di aggiornamento (PUT /holds/{id}) per impedire cambiamenti incoerenti (es. da "CANCELLED" a "PLACED") è attiva e funzionante.

----------------------------------------------------------------------------------------------------------------------------------
4. Swagger/OpenAPI
----------------------------------------------------------------------------------------------------------------------------------
Documentazione aggiornata e testata per tutti gli endpoint implementati, con parametri e risposte ben definiti.



🚀 Prossimi step nella Roadmap del progetto:

----------------------------------------------------------------------------------------------------------------------------------
1. Test di Integrazione REST con MockMvc
----------------------------------------------------------------------------------------------------------------------------------
Test di tutti gli endpoint REST per garantire che l'integrazione funzioni correttamente.
Creazione di test per il comando POST /books, GET /books/{id}, POST /holds con validazione duplicati e controllo 409 Conflict, GET /holds/{id}/details e DELETE /holds/{id}/cancel.

----------------------------------------------------------------------------------------------------------------------------------
1.1  🛒  – Nuova funzionalità: Bookshop e Pagamenti
----------------------------------------------------------------------------------------------------------------------------------
Estensione del dominio Book con prezzo, quantità disponibile, ISBN valido per vendita
Creazione entità Order e DTO correlato
Implementazione endpoint POST /orders (acquisto libro)
Logica pagamento (mock iniziale, poi gateway reale: Stripe, PayPal, bonifico)
Validazione stock, prevenzione ordini multipli se non disponibili
Documentazione Swagger/OpenAPI per nuovi endpoint orders



----------------------------------------------------------------------------------------------------------------------------------
2. Refactoring e Ottimizzazione
----------------------------------------------------------------------------------------------------------------------------------
Rivedere la struttura e l'architettura del codice per migliorare la leggibilità e la manutenzione.
Eventuali miglioramenti delle performance, soprattutto nei filtri avanzati, per garantire che le ricerche siano efficienti su grandi volumi di dati.

----------------------------------------------------------------------------------------------------------------------------------
3. Aggiunta di nuove funzionalità
----------------------------------------------------------------------------------------------------------------------------------
Implementare funzionalità aggiuntive, come la gestione dei dueDates per la restituzione dei libri, eventuale supporto per più lingue, ecc.
Monitoraggio e logging per garantire la tracciabilità delle operazioni.

----------------------------------------------------------------------------------------------------------------------------------
4. Documentazione finale
----------------------------------------------------------------------------------------------------------------------------------
Creazione della documentazione tecnica completa, includendo le configurazioni di deploy, di test, e il flusso dei dati tra i servizi.
Redazione della documentazione per i test, con dettagli su come eseguire i test di integrazione, le configurazioni necessarie e le dipendenze.

----------------------------------------------------------------------------------------------------------------------------------
5. Test di carico e stress
----------------------------------------------------------------------------------------------------------------------------------
Esegui dei test per simulare l'uso in scenari di produzione, verificando che il sistema gestisca correttamente il carico previsto.

----------------------------------------------------------------------------------------------------------------------------------
6. Refactoring del codice (opzionale)
----------------------------------------------------------------------------------------------------------------------------------
Considerare un refactoring per migliorare la leggibilità o l'estensibilità del codice, includendo il miglioramento della gestione delle eccezioni e la gestione centralizzata dei messaggi di errore.


Roadmap futura a lungo termine:

----------------------------------------------------------------------------------------------------------------------------------
1. Gestione degli utenti e autenticazione
----------------------------------------------------------------------------------------------------------------------------------
Potresti considerare l'aggiunta di un sistema di autenticazione/gestione utenti (JWT, OAuth2) per gestire l'accesso ai servizi, soprattutto se si prevede una crescita del sistema.
----------------------------------------------------------------------------------------------------------------------------------
2. Microservizi e distribuzione
----------------------------------------------------------------------------------------------------------------------------------
Separazione in microservizi per ciascun modulo (prenotazione, libri, utenti) con l'uso di un API Gateway, eventualmente implementando il pattern di Event-Driven Architecture (Kafka, RabbitMQ).

----------------------------------------------------------------------------------------------------------------------------------
3. CI/CD e DevOps
----------------------------------------------------------------------------------------------------------------------------------
Implementazione dei pipeline CI/CD per il deployment continuo e l'automazione dei test. L'integrazione con strumenti come Jenkins, GitLab o GitHub Actions potrebbe essere utile.

----------------------------------------------------------------------------------------------------------------------------------
4. Scalabilità orizzontale
----------------------------------------------------------------------------------------------------------------------------------
Se prevedi un carico maggiore, dovresti considerare l'adozione di soluzioni per la scalabilità orizzontale, come Kubernetes, per orchestrare e distribuire i microservizi in modo efficiente.

----------------------------------------------------------------------------------------------------------------------------------
✅ Stato attuale del progetto:
----------------------------------------------------------------------------------------------------------------------------------
Area	                    Stato	                  Dettagli
----------------------------------------------------------------------------------------------------------------------------------
Architettura	             ✅	Microservizio reservation-service ben strutturato (Spring Boot 3.3.0, PostgreSQL, Flyway)
Entity & DTO	             ✅	Book, Hold, HoldDto, HoldUpdateDto, HoldDetailsDto
Repository & Mapper	       ✅	Custom queries avanzate, searchByOptionalFilters, mapping pulito con HoldMapper
Controller REST	          ✅	GET, POST, PUT, DELETE implementati con validazioni, Swagger/OpenAPI documentato
Swagger / OpenAPI	          ✅	Tutti gli endpoint documentati e testabili da Swagger UI
Data Persistence	          ✅	PostgreSQL gestito da Flyway, script SQL iniziali, volumi Docker persistenti
----------------------------------------------------------------------------------------------------------------------------------




🧩 Roadmap futura (Step-by-Step)
🔶 Fase 1 – Testing & Robustezza
Step	                                                                                                Descrizione	Priorità
----------------------------------------------------------------------------------------------------------------------------------
🧪 Test con MockMvc	Integrazione SpringBootTest, WebMvcTest per validare i flussi REST	                            Alta
🔐 Sicurezza base (Auth stub)	Simulazione login utente base (anche se solo a livello mock iniziale)	            Media
🛑 Gestione errori globale	Handler @ControllerAdvice con messaggi JSON coerenti	                                Media




🔷 Fase 2 – Altri microservizi (opzionale o futuro)
----------------------------------------------------------------------------------------------------------------------------------
(se si pensa a una piattaforma multi-modulo con API Gateway, Auth, Payments...)
user-service per gestione utenti/log-in
payment-service per iscrizione a corsi o multate
notification-service (eventuale per mail/sms)




🟩 Fase 3 – Frontend (interfaccia utente)
----------------------------------------------------------------------------------------------------------------------------------
Step	                                          Descrizione	                                                     Priorità

🎨 Scaffolding Next.js/React	Setup iniziale progetto web utente (registrazione/login, dashboard)	                   Alta
🧭 API Integration	            Collegamento ai REST del reservation-service tramite Axios o simili	                   Alta
📲 UI/UX Utente finale	        Prenotazione libro, visualizzazione stato, annullamento	                               Alta
🔐 Auth+Ruoli	                Gestione differenziata utente/admin (anche se semplificata inizialmente)	           Media


➡ Il Frontend sarà tra gli ultimi macro-step, ma va preparato con anticipo a livello di design funzionale. Possiamo parallelizzare alcune fasi (es. layout + auth stub mentre si scrivono i test backend).



🟦 Fase 4 – Deployment e Cloud readiness
----------------------------------------------------------------------------------------------------------------------------------
Docker Compose locale (già in parte presente)
Containerizzazione avanzata
Helm chart per Kubernetes
Deploy GitOps su cluster (facoltativo ma auspicabile)


🧭 Conclusione

Hai :
un backend robusto e ben documentato
codice mantenibile e testabile
architettura già pronta per scalare.


💡 Prossime milestone consigliate:

1. ✅ MockMvc Test di integrazione
2. 🔜 Avvio scaffolding frontend/ (Next.js + Vite o Create Next App)
3. 🚀 Deployment locale via Docker Compose esteso (PostgreSQL + app)


**********************************************************************************************************************************************************
*  ***************************************************** 06/06/2025***************************************************************************************
**********************************************************************************************************************************************************
📌 Dove siamo arrivati:
**Schema DB**  
| • 9 migrazioni Flyway applicate con successo (v1 → v9) <br>• FK `holds.bib_id → books.id` attiva (ON DELETE CASCADE) | tabella `flyway_schema_history`  | 
| • 38 HOLD totali <br>   – 23 originali (v4 + v9) <br>   – 15 di test (script manuale) <br>• 48 BOOKS totali (nessuno orfano) | script `db_consistency_check.sh` |
| • 0 hold orfane <br>• Distribuzione status coerente (PLACED 11, READY 10, COLLECTED 10, …) <br>• 3 patron fissi con 6 hold ciascuno (scenario multi-prenotazione) | query di controllo|
| • Repository & controller “alias” pronti ⇒ **back-compatibility** con i vecchi curl funzionante <br>• Test JUnit OK (context loads) | build `./mvnw clean package`|
| • Script shell di verifica (`db_consistency_check.sh`) <br>• Script SQL rapido per dati di test (`populate_test_holds.sql`) | nella root del repo |


⏭️ Prossimi passi immediati
| Priorità | Attività                                                                                                    | Perché / Outcome |
| **1**    | **Back-compat / API** – rieseguire l’intera suite manuale di curl (GET, POST, PUT, DELETE) con i nuovi dati | assicuriamo che gli alias e i filtri opzionali rispondano correttamente con dataset più ricco|
| **2**    | **Pagination & sorting** per endpoint `GET /holds` e `GET /books`                                           | ora che esistono ≥ 5 prenotazioni per alcuni patron / libri possiamo testare limit/offset e ordinamenti |
| **3**    | **Unit + integration test** extra: <br>• repository search con più parametri <br>• controller façade (parametri diretti) | copertura automatica per evitare regressioni future |
| **4**    | **Scenario “hard-delete cascade”** – cancellare un `book` e verificare che le hold collegate vengano eliminate (ON DELETE CASCADE) | testiamo la FK appena introdotta  |
| **5**    | **Documentazione** breve: aggiornare README con <br>• nuove migrazioni <br>• script di popolamento <br>• esempi curl aggiornati    | hand-over e onboarding più rapidi |
| **6**    | (Opzionale) Creare **Flyway V10** che incorpora lo script `populate_test_holds.sql` come *baseline* di demo – solo se vogliamo che quei dati demo siano sempre presenti in ogni deploy | invece di lanciare manualmente lo script |



☑️ Cosa serve prima del prossimo incontro
Ripristinare docker-compose se era stato fermato e assicurarsi che pg-library, reservation-service e gli altri microservizi siano tutti up.
Tenere a portata i log delle richieste curl che usavi in precedenza – li riusiamo come checklist di back-compat.
Decidere se i 15 record di test devono diventare permanenti (→ migrazione V10) o restare solo in ambiente di sviluppo.
Fammi sapere quale di questi punti vuoi attaccare per primo domani e preparo i dettagli operativi (script, patch di codice o checklist).
                        


**********************************************************************************************************************************************************
*  ***************************************************** 10/06/2025***************************************************************************************
**********************************************************************************************************************************************************

                                                📋 Attività completate:
--------------------------------------------------------------------------------------------------------------------------------------------
1) **Book shop extension**: aggiunti campi `price`, `stockQuantity`, `isbn` in <br>`BookDto`, `BookMapper`, `BookController`, `BookRepository`  
   **Evidenza / file modificati**: `BookDto.java`, `BookMapper.java`, `BookController.java`, `BookRepository.java` 
--------------------------------------------------------------------------------------------------------------------------------------------
2) **Mapper updateEntity** per update “in place” (evita ID/Version null)
   **Evidenza / file modificati**:`BookMapper.java`  
--------------------------------------------------------------------------------------------------------------------------------------------
3) **Migrazione Flyway V10** – `orders_seq` con `INCREMENT BY 50` per Hibernate.
   **Evidenza / file modificati**: `V10__add_orders_sequence.sql`
--------------------------------------------------------------------------------------------------------------------------------------------
4) **Ordini** : • creato endpoint `PUT /orders/{id}/cancel` (soft-delete, 204) 
                • creato endpoint `PUT /orders/{id}/mark-paid` (webhook mock, 204/409) 
    **Evidenza / file modificati**: `OrderController.java`, `OrderService.java`
--------------------------------------------------------------------------------------------------------------------------------------------
5) **Fix validation ISBN** (regex 10/13) & gestione duplicate key su ISBN
   **Evidenza / file modificati**: `ISBN.java` (constraint), script test
--------------------------------------------------------------------------------------------------------------------------------------------
6) **Script `regression_test.sh`**  • provisioning dati se mancanti 
                                    • flusso completo: 2 libri → update → hold → ordine → cancel → mark-paid 
                                    • asserzioni su HTTP code (200/201/204/409), status `CANCELLED`
   **Evidenza / file modificati**: `regression_test.sh`
--------------------------------------------------------------------------------------------------------------------------------------------
7) **Passata full-run**: tutti gli step OK (log finale ✅)




--------------------------------------------------------------------------------------------------------------------------------------------
                                                         📋 Copertura test raggiunta:
--------------------------------------------------------------------------------------------------------------------------------------------
Macro area	                     Coperto oggi	                                               Note
Libro (CRUD + search)	         ✅ Create, Read, Update, filtro title/author/genre
                                 ✅  Delete hard & soft	                                     soft-delete da implementare
--------------------------------------------------------------------------------------------------------------------------------------------
Hold (CRUD + filtri)	            ✅ Create, Read, search by status/author
                                 ❌ Update / cancel / expire flow	                            logica scadenza ancora fuori scope
--------------------------------------------------------------------------------------------------------------------------------------------
Order flow	                     ✅ Create → Cancel (204) → Mark-paid (409 se già cancellato)
                                 ❌ Mark-paid positivo (CREATED → PAID)
                                 ❌ stock-check e decremento	                                  da aggiungere business rule
--------------------------------------------------------------------------------------------------------------------------------------------
Migrazioni DB	                 ✅ V10 sequenza ordini	                                        prossima: trigger stock
--------------------------------------------------------------------------------------------------------------------------------------------
Regression script	              ✅ green run	                                                 export JUnit/HTML per CI - pending
--------------------------------------------------------------------------------------------------------------------------------------------
Legacy alias	                 ✅ test manuale	                                              ✅ automatizzare /books/search/* & /holds/search/*
--------------------------------------------------------------------------------------------------------------------------------------------
Suite:	                                      Obiettivo	                                       Stato
Nuova API (regression_test.sh)	              CRUD completo + ordini	                        ✅ OK
Back-compat (regression_test-old.sh)	        Alias HAL “find-by-*”	                           ✅ OK

--------------------------------------------------------------------------------------------------------------------------------------------
🟢 Migrazione V11 – correzione autori	V11__fix_book_authors.sql con una UPDATE per ciascun id ✅ OK
                                        • (opzionale: JSON array + loop PL/pgSQL per mantenerlo conciso.) 
--------------------------------------------------------------------------------------------------------------------------------------------
🟢1) Test di concorrenza: “optimistic locking”
   - Aggiungere casi di “optimistic locking” (update simultanei sullo stesso book) per verificare il campo version.
   ====> il campo @Version su Book protegge da aggiornamenti concorrenti: il secondo commit fallisce con 409 Conflict.✅ OK
# 1. variabile libro✅ OK
BOOK=c1dd3865-ff8f-4de3-8ab1-0e150b367d88

# 2. crea l’ordine (salva l’id nella variabile OID)  ✅ OK: POST → 201 Created
OID=$(curl -s -X POST localhost:8080/orders \
  -H 'Content-Type: application/json' \
  -d "{\"bookId\":\"$BOOK\",\"patronId\":1,\"quantity\":1}" | jq -r .id)

echo "Creato ordine ID=$OID"✅ OL

# 3. annulla l’ordine✅ OK : PUT → 204 No Content
curl -i -X PUT "localhost:8080/orders/$OID/cancel"

# 4. prova a segnarlo pagato (deve dare 409)✅ OK: PUT /mark-paid → 409 Conflict (perché l’ordine è già in stato CANCELLED).
curl -i -X PUT "localhost:8080/orders/$OID/mark-paid?gatewayRef=PAY-TEST"


🟢2) Aggiornmamento e test su Documentazione Swagger:
   - Aggiungere le nuove rotta /orders/{id}/cancel e /orders/{id}/mark-paid nella spec OpenAPI (arricchisce Swagger-UI).✅ OK
--------------------------------------------------------------------------------------------------------------------------------------------


--------------------------------------------------------------------------------------------------------------------------------------------
📊 Stato dei test “old-curl” + valutazione del dataset
--------------------------------------------------------------------------------------------------------------------------------------------
Gruppo	            End-point legacy	                                Esito test manuali
BOOKS	              `/books/search/find-by-title	                    author✅ OK
HOLDS	              `/holds/search/find-by-title	                    author`✅ OK
Filtri combinati	  /holds?title=…&author=…&pickupBranch=…&status=…	  combinazioni multiple riportano il sotto-insieme atteso.✅ OK
Filtri avanzati	  genre, publicationYear	                          (Distopia, Psicologico, 1949…).✅ OK
Soft / Hard delete  PUT /cancel, DELETE hold, DELETE book	           status, FK e cascata confermati.✅ OK
Order flow	        POST /orders, PUT /cancel, PUT /mark-paid	        sequenza + stato 409 su ordine già cancellato verificati.✅ OK


--------------------------------------------------------------------------------------------------------------------------------------------
                                                             📌 To-do immediati:
--------------------------------------------------------------------------------------------------------------------------------------------

--------------------------------------------------------------------------------------------------------------------------------------------
Ordini – percorso “happy-path”: • test PUT /mark-paid su ordine in stato CREATED → atteso 204 & status=PAID
                                • verifica decremento stockQuantity (da implementare in service + trigger DB)
--------------------------------------------------------------------------------------------------------------------------------------------
Edge-cases: • ordine con quantity > stock → 409/422
            • doppia hold stesso patron/libro → 409
            • ISBN non valido → 400
--------------------------------------------------------------------------------------------------------------------------------------------
Soft-delete libro & cascata hold: • endpoint DELETE /books/{id} → status=CANCELLED, non hard-delete
                                  • verifica che hold restino integri ma non più prenotabili
--------------------------------------------------------------------------------------------------------------------------------------------
Paginazione & filtri combinati: • /holds?title=&author=&status=&pickupBranch=&page=&size=
                                • script per paginare risultati ed asserire X-Total-Count
--------------------------------------------------------------------------------------------------------------------------------------------
CI integration: GitHub Actions: spin-up postgres, run ./regression_test.sh, publish artefatto log
--------------------------------------------------------------------------------------------------------------------------------------------




--------------------------------------------------------------------------------------------------------------------------------------------
⏭️ Proposte di lavoro per oggi:
--------------------------------------------------------------------------------------------------------------------------------------------
Priorità	Task
🔴	        Implementare & testare business-rule stock (1)
🟠	        Soft-delete Book + hold coherence (3)
🟢	        CI pipeline con regression script (5)





**********************************************************************************************************************************************************
******************************************************  12/06/2025  **************************************************************************************
**********************************************************************************************************************************************************
                                   📋 Copertura test — stato aggiornato (12 GIU 2025)

Macro-area	                    Coperto	                                                   Note
--------------------------------------------------------------------------------------------------------------------------------------------
Libro (CRUD + search)	        ✅ Create · Read · Update · filtro title/author/genre
                                ✅ Delete hard & soft	                                      soft-delete flag deleted + hard-delete OK
--------------------------------------------------------------------------------------------------------------------------------------------
Hold (CRUD + filtri)            ✅ Create · Read · search by status/author                    ❌ flow Update/Cancel/Expire ancora da coprire
--------------------------------------------------------------------------------------------------------------------------------------------
Order flow                      ✅ Create → Cancel (204) → Mark-paid (409 se CANCELLED)       ❌ Mark-paid “happy path” (CREATED → PAID)
                                                                                               ❌ controllo & decremento stock
--------------------------------------------------------------------------------------------------------------------------------------------
Migrazioni DB	                 ✅ V10 sequenza ordini
                                ✅ V11 fix autori
                                ✅ V14 soft-delete flag                                      prossima: trigger stock
--------------------------------------------------------------------------------------------------------------------------------------------
Regression script	              ✅ all_tests.sh green	                                      export JUnit/HTML per CI - TODO
--------------------------------------------------------------------------------------------------------------------------------------------
Legacy alias	                 ✅ /books & /holds find-by-… auto-testate                    -


| Script                         | Obiettivo                | Stato  
| ------------------------------ | ------------------------ | -----  
| `regression_test.sh`           | CRUD completo + ordini   | ✅    |
| `regression_test-old.sh`       | alias HAL “find-by-\*”   | ✅    |
| `concurrency_rest_conflict.sh` | optimistic-locking (409) | ✅    |
| `soft_hard_delete_demo.sh`     | soft / hard delete demo  | ✅    |
| `all_tests.sh`                 | orchestra tutte le suite | ✅    |


                                               📌 Prossimi step (ordinati per impatto su funzionalità di business)
--------------------------------------------------------------------------------------------------------------------------------------------------------------
⚙️	   Attività	                                                                    
1      Soft-delete → cascade Hold
       • quando Book.deleted = true ⇒ nuove hold vietate.✅OK
       • update automatico delle hold “PLACED” su quel libro → status=CANCELLED (o “BOOK_REMOVED”).✅OK
       • test REST: DELETE/soft libro con hold attive ⇒ hold non prenotabili + rimangono nella ricerca storico.✅OK

       Output atteso
       ❶ Listener in service o @EntityListener
       ❷ integrazione nei test soft_hard_delete_demo.sh

       Dipendenze:
       flag deleted già disponibile

--------------------------------------------------------------------------------------------------------------------------------------------------------------
2     Attività
      Pagination & filtri combinati su /holds
      GET /holds?title=&author=&status=&pickupBranch=&page=&size= + header X-Total-Count.       

      Output atteso
      ❶ nuova query custom (Spring Data @Query + Pageable)
      ❷ script bash che richiama più pagine e verifica conteggi
      
      Dipendenze:
      richiede eventuali indici
--------------------------------------------------------------------------------------------------------------------------------------------------------------
3     Attività
      Edge-cases (valida business rules)
      • ordine quantity > stock → 409 / 422.
      • doppia hold stesso patron/libro → 409.
      • ISBN non valido → 400 (già validato, serve test).

      Output atteso
      3 test JUnit 5 + 1 script curl

      Dipendenze:
      prezzo/stock presenti
--------------------------------------------------------------------------------------------------------------------------------------------------------------
4    Attività
     Ordini – percorso “happy-path”
     • PUT /orders/{id}/mark-paid?gatewayRef= su ordine CREATED → 204 e status=PAID.
     • Trigger DB (o service) che decrementa stockQuantity e blocca stockQuantity < 0.
     • migrazione Flyway V15 con trigger funzione PL/pgSQL.

     Output atteso
     ❶ OrderService update
     ❷ V15 trigger
     ❸ test di integrazione + script curl

     Dipendenze:
     dipende da stock check (edge-case 3)

--------------------------------------------------------------------------------------------------------------------------------------------------------------
5   Attività
    Export report di test per CI
    Convertire output bash/JUnit in artefatti HTML (Surefire + Allure / Maven Site)

    Output atteso
    pipeline CI con badge & report
    
    Dipendenze:
    scripts stabili   
--------------------------------------------------------------------------------------------------------------------------------------------------------------
🔜 Proposta d’attacco sprint
Implementare cascade hold + test (sblocca regole loan e tiene coerenza).
Integrare pagination (necessario per front-end).
Edge cases + trigger stock (blinda integrità).
Happy-path pagamento (chiude loop Ecommerce).
CI reporting (qualità codice).
Così manteniamo crescita incrementale senza rompere la suite esistente.


*************************************************************************************************************************************
******************************************************  13/06/2025  ****************************************************************
*************************************************************************************************************************************
📋 Stato attuale e prossimo micro-backlog:
Macro-task	                              Implementato	         Verificato	       Note
--------------------------------------------------------------------------------------------------------------------------------------------------------------
Soft-delete » nuove Hold vietate          ✅	                   ✅                 HoldController#createHold ora restituisce 409 se il libro è deleted=true.
--------------------------------------------------------------------------------------------------------------------------------------------------------------
Soft-delete » Hold PLACED ⇒ CANCELLED	  ✅                     ✅                  Entrambe le sotto-regole verificate:
(Soft-delete → cascade Hold)                                                         1️⃣ POST su libro deleted=true ⇒ 409
                                                                                     2️⃣ Trigger PLACED → CANCELLED testato.                                                     
--------------------------------------------------------------------------------------------------------------------------------------------------------------
Soft-delete » Hold storiche 
visibili in ricerche                      ✅	                  ⚠️ da testare       @Where su Book filtra solo i libri, non le hold: 
                                                                                     le righe restano interrogabili.
--------------------------------------------------------------------------------------------------------------------------------------------------------------
Edge-case extra (ISBN, doppia hold, …)	   ➖	                   ➖	                Da scriptare.
--------------------------------------------------------------------------------------------------------------------------------------------------------------
Order → PAID ⇒ stock-1	                 ➖	                   ➖	               Servizio/trigger da implementare.
--------------------------------------------------------------------------------------------------------------------------------------------------------------

*************************************************************************************************************************************
******************************************************  15/06/2025  ****************************************************************
*************************************************************************************************************************************

| Step  | Attività                                  | Stato                                                                    ------------------------------------------------------------------------------------------------------------------------------------|
| **1** | Soft-delete → cascade Hold  
Note:                                               | ✅ **Completato**                
Trigger di cancellazione attivo. Le `HOLD` collegate a `BOOK.deleted=true` sono aggiornate in `CANCELLED`. Il demo script ha validato con successo lo scenario |
------------------------------------------------------------------------------------------------------------------------------------|

| **2** | Pagination & filtri combinati su `/holds` | ⏳ **Da fare**                   
Note:
Richiede implementazione di query custom con `Pageable`, e test multi-pagina  
------------------------------------------------------------------------------------------------------------------------------------|
| **3** | Edge-cases validazione business           | 🔜 **Prossimo step suggerito** 
Note: 
Critico per robustezza. Test JUnit da implementare per `quantity > stock`, `doppia hold`, ISBN non valido                                                      
------------------------------------------------------------------------------------------------------------------------------------|
| **4** | Ordini – percorso happy-path              | ⏳ **Da fare**                  
Note: 
Richiede completamento edge-case 3 prima di procedere con logica `mark-paid` e decremento stock                                                                |
------------------------------------------------------------------------------------------------------------------------------------|
| **5** | Export test CI pipeline (report HTML)     | ⏳ **Posticipato**              
Note: 
Da affrontare una volta che gli script e i test sono stabilizzati                                                                                              |
------------------------------------------------------------------------------------------------------------------------------------|


🔧 Azioni da fare subito
------------------------------------------------------------------------------------------------------------------------------------|
✅ Considerare history_demo.sh superato, ma segnalare come test non deterministico per ora.
🔜 Procedere con la scrittura dei test edge-case (Step 3) per consolidare la coerenza dei dati e proteggere dalle 500 causate da errori noti.
🛠 In parallelo: valutare nel BookService la gestione manuale della duplicazione ISBN per evitare 500 anche in produzione.


------------------------------------------------------------------------------------------------------------------------------------|
🎯 Proposta di roadmap a breve termine:


🔹 Step 1: Edge-case di business logic (prioritario)
------------------------------------------------------------------------------------------------------------------------------------|
 Aggiungere test JUnit:
❗ POST /orders con quantità > stock → aspettarsi 409/422
❗ POST /holds doppia prenotazione stesso patronId + bibId → 409
❗ POST /books con ISBN non valido (formato) → 400
 Scrivere uno script edge_cases_demo.sh per test manuale e CI

 🔹 Step 2: Pagination + filtri GET /holds
 ------------------------------------------------------------------------------------------------------------------------------------
 Aggiungere metodo HoldRepository.findByAdvancedSearch(...) con @Query
 Agganciare paginazione via Pageable a HoldController
 Implementare header X-Total-Count per supporto frontend
 Scrivere script holds_pagination_test.sh per simulare pagine successive

🔹 Step 3: Ordini “happy-path”
------------------------------------------------------------------------------------------------------------------------------------|
 Estendere OrderController con:
PUT /orders/{id}/mark-paid?gatewayRef=...
@Transactional decremento stock solo se stock ≥ quantity
 Creare trigger PostgreSQL (Flyway V17__decrement_stock_trigger.sql)
 Test con ordine che esaurisce stock

************************************************************************************************************************************
******************************************************  17/06/2025  ****************************************************************
************************************************************************************************************************************
✅ 📋 Riepilogo attività svolte oggi
Sessione di test manuale completa sul servizio REST /books, verificando il comportamento di:
Creazione libro (POST)
Lettura libro creato (GET)
Update con versione corretta (PUT)
Tentativi di update con versioni obsolete
Soft-delete e conseguente comportamento del sistema
Hard-delete e successivo reinserimento
Validazione vincoli su ISBN

📊 Risultato complessivo dei test:

Test	Esito	Note principali
POST libro	                  ✅	Corretto
GET libro	                  ✅	Corretto
PUT con versione corretta	   ✅	Corretto, versionamento incrementale funzionante
PUT con versione obsoleta	   ✅	Corretto
PUT successivo con versione	✅	Comportamento coerente, ma OCC ancora aggirabile
Soft-delete	                  ✅	Funziona come atteso
POST dopo soft-delete	      ⚠️	Bloccato da vincolo su ISBN
Hard-delete + nuovo POST	   ✅	Funziona, nuovo ID generato correttamente

🚧 Punto della situazione
Abbiamo individuato 3 punti critici da risolvere nel prossimo step di sviluppo.

⚠️ Criticità 1 – PUT con versione obsoleta accettata
Diagnosi: uso di save() su oggetto non "managed" → OCC (@Version) non scatta.✅ Completato

Obiettivo: recuperare entità gestita con findById, verificare version, aggiornare manualmente i campi, lanciare eccezione su mismatch.✅ Completato

⚠️ Criticità 2 – Retry con versione obsoleta ancora accettato✅ Completato
Diagnosi: come sopra. Il fatto che il versionamento continui a incrementare mostra che la logica OCC è bypassata o non attivata.✅ Risolto

⚠️ Criticità 3 – POST su ISBN soft-deleted produce 409
Diagnosi: il vincolo UNIQUE su ISBN blocca la creazione anche se deleted = true.

Obiettivo: permettere reinserimento di un libro soft-deleted (se desiderato), con logica che consideri solo deleted = false per i vincoli di unicità ISBN.
--------------------------------------------------------------------------------------------------
🔜 Prossimi step (📌 TODO tecnico)
✅ Definito oggi, da iniziare nel prossimo ciclo di sviluppo:

1) Fix del metodo updateBook()
❷ Recupero entità gestita con findById(...)                 ✅ Completato
❷ Verifica di version                                       ✅ Completato
❷ Uso di mapper updateEntityFromDto(...) con @MappingTarget ✅ Completato
❷ Lancio di OptimisticLockException in caso di mismatch     ✅ Completato
--------------------------------------------------------------------------------------------------
Test	Esito	Note principali
POST libro	                                                ✅	Corretto
GET libro	                                                ✅	Corretto
PUT con versione corretta	                                 ✅	Corretto, versionamento incrementale funzionante
PUT con versione obsoleta	                                 ✅	Corretto
PUT successivo con versione	                              ✅	Comportamento coerente, ma OCC ancora aggirabile
Soft-delete	                                                ✅	Funziona come atteso
Test JUnit / MockMvc su edge-case OCC                       ✅	Funziona come atteso
--------------------------------------------------------------------------------------------------

2) Verifica/aggiunta annotazione @Version su Book.version

3) Aggiornamento logica POST per ISBN
❷ Opzione 1: refactoring vincolo DB (es. UNIQUE (isbn, deleted) o partial index PostgreSQL)
❷ Opzione 2: gestione manuale in service con existsByIsbnAndDeletedFalse(...)

4) Test JUnit / MockMvc su edge-case OCC ⚠️ In corso

5) Aggiornamento README (o doc tecnica) con:
❷ Comportamento OCC atteso
❷ Soft-delete vs hard-delete
❷ Gestione ISBN in caso di duplicati soft-deleted


