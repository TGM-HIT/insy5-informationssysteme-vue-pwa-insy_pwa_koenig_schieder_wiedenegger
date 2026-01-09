![tests](https://img.shields.io/badge/tests-59%20passed-success)
![total](https://img.shields.io/badge/total-59%20tests-blue)

# E2E Testing

# Cypress E2E Tests

Author: Petrovic Pavle, Schieder Laurin
Version: 8.1.2026

## Installation

```bash
cd frontend/
npm install --save-dev cypress
npm install --save-dev mochawesome mochawesome-merge mochawesome-report-generator
```

## Verzeichnisstruktur

```
frontend/
├── tests/
│   └── e2e/
│       ├── analysis.cy.js
│       ├── sample.cy.js
│       ├── box.cy.js
│       ├── boxpos.cy.js
│       └── log.cy.js
├── cypress/
│   ├── support/
│   │   ├── e2e.js
│   │   └── commands.js
│   ├── videos/
│   ├── screenshots/
│   └── reports/
└── cypress.config.js
```

### package.json Scripts

Die Scripts zum ausführen müssen in package.jason aktiviert werden.

```json
{
  "scripts": {
    "cypress:open": "cypress open",
    "cypress:run": "cypress run",
    "test:e2e": "cypress run"
  }
}
```

## Vue Components - data-cy Attribute hinzufügen

Damit die Tests leichter zum Umsetzen sind haben wir die originalen attribute des Frontends auf cy Attribute geändert

### DataTable.vue

```vue
<button data-cy="btn-add-new">Add New</button>
<table data-cy="data-table">...</table>
<button data-cy="btn-edit">Edit</button>
<button data-cy="btn-delete">Delete</button>
```

## Tests ausführen

```bash
# Tests ausführen (Headless)
npm run cypress:run

# Tests interaktiv
npm run cypress:open

# Einzelne Test-Datei
npx cypress run --spec "tests/e2e/analysis.cy.js"

# Ohne Video (schneller zb wenn es ganz schnell sein muss)
npx cypress run --config video=false
```

## Wichtige Cypress-Befehle

```javascript
// Navigation & Interaktion
cy.visit('/')
cy.get('[data-cy="selector"]').click()
cy.get('[data-cy="selector"]').click({ force: true })

// Formulare
cy.get('[data-cy="input"]').type('text')
cy.get('[data-cy="input"]').clear()

// Assertions
cy.get('[data-cy="element"]').should('be.visible')
cy.get('[data-cy="element"]').should('exist')
cy.get('[data-cy="element"]').should('not.exist')

// API Requests
cy.request('POST', url, data)
cy.request('GET', url)
cy.request('PUT', url, data)
cy.request({ method: 'GET', url, failOnStatusCode: false })

// Intercepts
cy.intercept('POST', '**/api/**').as('apiCall')
cy.wait('@apiCall').then((interception) => {
    expect(interception.response.statusCode).to.eq(201)
})

// Finden von Elementen
cy.contains('td', 'text')
cy.get('tbody').contains('td', id.toString())

// Waits
cy.wait(1000)
cy.wait('@apiCall')
```

Die wichtigsten Cypress Befehle werden erklärt [1].

## Videos / Screenshots / html-Testreports

Packages installieren

```bash
npm install --save-dev cypress-mochawesome-reporter
```

in der Datei cipress.config.js müssen nun alle speicherorte definiert werden. Hier für haben wir folgende einstellungen gewählt:

```bash
    // Video-Konfiguration
    video: true,
    videoCompression: 32,
    videosFolder: 'cypress/videos',

    // Screenshot-Konfiguration
    screenshotOnRunFailure: true,
    screenshotsFolder: 'cypress/screenshots',

    // HTML Report-Konfiguration
    reporter: 'cypress-mochawesome-reporter',
    reporterOptions: {
      reportDir: 'cypress/reports',
      charts: true,
      reportPageTitle: 'REST API E2E Tests',
      embeddedScreenshots: true,
      inlineAssets: true,
      videoOnFailOnly: false
    }
```

### Ergebnisse

Alles wird nach dem runnen in den angegebenen Pfaden gespeichert und funktioniert.

- Videos: `cypress/videos/*.mp4`
- Screenshots: `cypress/screenshots/`
- HTML Report: `cypress/reports/html/index.html`

Ohne vidoe ausführen:

```bash
# Ohne Video
npx cypress run --config video=false
```

## Einbinden der results als simple Badges

Nach jedem vollständigdurchgeführtem Testdurchlauf wird ein results.json erstellt. Das Script Update-Badges nimmt sich die werte aus diesem file und erstellt mithilfe von moccaswome-reporter Badges.

```bash
npm install --save-dev cypress-json-results cypress-mochawesome-reporter
```

In cypress.config.js muss das Plugin aktiviert werden und das result.json erstellt werden.

```bash
require('cypress-mochawesome-reporter/plugin')(on)

// JSON Results für Badge-Update
require('cypress-json-results')({
  on,
  filename: 'results.json'  // Erstellt cypress/results.json
})
```

Weiters auch in den Scripts muss hinzugefügt werden:

```bash
"update-badge": "node update-badge.cjs"
```

## Literatur

Perplexity AI, "Cypress Befehle Erklärung," 2025. [Online]. Available: https://www.perplexity.ai/search/bitte-erklare-mir-die-wichtigs-1jut6Q..SPO_2835xUdDRQ. (Accessed: Dez. 10, 2025])

"Why Cypress?," Cypress Documentation. [Online]. Available: https://docs.cypress.io/app/get-started/why-cypress. (Accessed: Jan. 08, 2026)

"Writing Your First End-to-End Test," Cypress Documentation. [Online]. Available: https://docs.cypress.io/app/end-to-end-testing/writing-your-first-end-to-end-test. (Accessed: Jan. 08, 2026)

"Component Testing: Get Started," Cypress Documentation. [Online]. Available: https://docs.cypress.io/app/component-testing/get-started. (Accessed: Jan. 08, 2026)

"Reporters," Cypress Documentation. [Online]. Available: https://docs.cypress.io/app/tooling/reporters. (Accessed: Jan. 08, 2026)

"Custom Commands," Cypress Documentation. [Online]. Available: https://docs.cypress.io/api/cypress-api/custom-commands. (Accessed: Jan. 08, 2026)

"Best Practices," Cypress Documentation. [Online]. Available: https://docs.cypress.io/app/core-concepts/best-practices. (Accessed: Jan. 08, 2026)

# Vue CRUD -> Vue-DataTable

## Einführung

Dieses Projekt implementiert eine Web-Oberfläche zur Verwaltung von Labor-Daten basierend auf einer bestehenden ReST-Schnittstelle. Es ermöglicht die Anzeige, Bearbeitung, Erstellung und Löschung von Datensätzen in verschiedenen Tabellen mittels eines modernen JavaScript-Frameworks (Vue.js).

## Funktionen

- CRUD (Create, Read, Update, Delete) Funktionen auf den Tabellen:
  - Analysis
  - Sample
  - Box
  - BoxPos
- Die Tabelle Log ist nur lesbar.

## Voraussetzungen

- Funktionierende ReST-API (ReST Backend Übung)

## Projektstruktur

- `frontend/` enthält den Vue.js Quellcode der Webanwendung.
- `docker-compose.yml` zum Starten der Anwendung und des Backends.
- `.gitignore` enthält vordefinierte Regeln, um Binaries, Class-Files und `node_modules` vom Commit auszuschließen.

## Nutzung

1. Docker Compose starten, um die benötigten Services laufen zu lassen.
2. Anwendung im Browser öffnen (meist unter `http://localhost:8080` oder entsprechend konfiguriert).
3. Über die Navigationsleiste zwischen den Tabellen wechseln.
4. Datensätze einsehen, bearbeiten, anlegen oder löschen.
5. Fehler bei der Konsistenzprüfung werden direkt im UI angezeigt.

## Implementierung

- Editierbare Tabellen verwenden vue-Komponenten `DataTable` und `EditModal`.
- Axios ist das HTTP-Client-Modul für API-Anfragen.
- Nicht editierbare Felder sind in den Modalen deaktiviert (z.B. ID-Felder).
- Sortierung und Pagination werden über API-Parameter gesteuert.

## Quellen

- [Vuetify Documentation](https://vuetifyjs.com/en/)
- [Vue.js Documentation](https://vuejs.org/)
- [Axios Documentation](https://axios-http.com/docs)
- [Vue CLI Documentation](https://cli.vuejs.org/)
- Beispiel: "Vue 3 + Vuetify + Axios" von mborko (GitHub)
