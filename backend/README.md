# Cypress Testing

Author: Petrovic Pavle, Schieder Laurin

Verion: 10.12.2025
#### Die Gesammte doku befindet sich in der ReadMe des Submodules Frontend

### Einführung
Bei dieser Übung sollen für eine bestehende CRUD Webapplikation E2E-Tests implementiert werden.
### Ziele
Das Ziel dieser Übung ist das funktionale Überprüfen einer bereitgestellten JS-View zur Datenmanipulation.



# Rest Backend

Author: Petrovic Pavle, Schieder Laurin

Verion: 15.10.2025

### JPA Buddy
Zuerst installieren wir das Plugin JPA-Buddy. 
"JPA Buddy ist ein IntelliJ-Plugin, das die Arbeit mit JPA/Hibernate erleichtert, 
indem es Entity-Klassen, Repositories, DTOs und Mapping-Code automatisch generiert und wir es nutzen können, 
um schneller und fehlerfrei Datenbank-Entities und zugehörige Abfragen in unserem Spring Boot-Projekt zu erstellen."

Wenn man eine Entity klasse Analysis hat kann man darauf aufbauend mit dem JPA Buddy Plugin ein Repositorys und die benötigten controller erstellen.
Das gillt weiters für alle folgenden Klassen.
### 2. Datenbank verbinden
Im YML muss angegeben werden werden, wie die Verbindung zur Datenbank hergestellt wird.
```yaml
backend:
    build: .
    container_name: gk911-backend
    restart: unless-stopped
    env_file:
      - .env
    environment:
      POSTGRES_HOST: db
      POSTGRES_PORT: 5432
    depends_on:
      - db
    ports:
      - "8081:8080"
    networks:
      backend:
        ipv4_address: 172.28.0.30
```

### Anotationen Erklärung
| Annotation                                                     | Bedeutung / Zweck                                                                                             |
| -------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------- |
| `@Entity`                                                      | Markiert die Klasse als JPA-Entity, die einer Datenbanktabelle zugeordnet wird.                               |
| `@Table(name = "boxpos", schema = "venlab")`                   | Gibt den Tabellennamen und das Schema der Entity an.                                                          |
| `@EmbeddedId`                                                  | Markiert ein Feld als zusammengesetzten Primärschlüssel, definiert in einer eigenen Klasse.                   |
| `@Column(name = "...", length = ...)`                          | Konfiguriert eine Datenbankspalte: Name, Länge oder andere Eigenschaften.                                     |
| `@Repository`                                                  | Markiert die Klasse als Spring-Repository für Datenzugriff und Exception-Übersetzung.                         |
| `@RestController`                                              | Kombination aus `@Controller` und `@ResponseBody`; macht die Klasse zu einem REST-Controller (JSON-Response). |
| `@RequestMapping("/api/analysis")`                             | Definiert den Basis-URL-Pfad für alle Endpunkte der Klasse.                                                   |
| `@Tag(name = "...", description = "...")`                      | Fügt der OpenAPI/Swagger-Dokumentation einen Tag hinzu.                                                       |
| `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping` | Definiert HTTP-Methoden (GET, POST, PUT, DELETE) und deren Endpunkte.                                         |
| `@Operation(summary = "...", description = "...")`             | Beschreibt jeden API-Endpunkt für Swagger/OpenAPI-Dokumentation.                                              |
| `@PageableDefault(size = 20)`                                  | Legt Standardwerte für Paging (z. B. Seitengröße) fest.                                                       |
| `@PathVariable`                                                | Bindet einen Pfadparameter aus der URL an einen Methodenparameter.                                            |
| `@RequestBody`                                                 | Bindet den Request-Body (JSON) an ein Java-Objekt.                                                            |
| `@Validated`                                                   | Aktiviert Bean Validation (z. B. `@NotNull`, `@Size`) auf dem Request-Objekt.                                 |
| `@ResponseStatus(HttpStatus.NO_CONTENT)`                       | Gibt an, welchen HTTP-Statuscode zurückgegeben wird (hier: 204 – kein Inhalt).                                |
