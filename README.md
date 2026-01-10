![Build Status](https://github.com/TGM-HIT/insy5-informationssysteme-vue-pwa-insy_pwa_koenig_schieder_wiedenegger/actions/workflows/gradle.yml/badge.svg?branch=main)
![Coverage](.github/badges/jacoco.svg)

## How to use

**URL:** [https://46.101.148.160/](https://46.101.148.160/)
Empfehlung: In einem privaten Fenster öfnen, um Cokies zu vermeiden. Wenn nicht ist es möglich, dass diese deaktiviert werden müssen, um eine vollsätndige Funktionalität zu gewährleisten.

**Login Credentials:**

Benutzername: admin

Passwort: password

# PWA

Author: König, Schieder, Wiedenegger

### Einführung

Bei dieser Übung soll die vorhandene Implementierung in der Cloud
deployed werden und auch als PWA für mobile Endgeräte angeboten werden.

### Ziele

Das Ziel dieser Übung ist das Bereitstellen einer Webanwendung in der
Cloud. Dabei sollen Werkzeuge zum Einsatz kommen, die das leichte
Umsetzen, Testen und Bereitstellen beschleunigen. Bestehender Code soll
dabei angepasst und erweitert werden ohne die Funktionalität zu
beeinträchtigen.

## Workflows CI/CD

Dieses Projekt nutzt GitHub Actions für automatisierte Tests, Coverage-Reports und Release-Management. Alle Workflows sind in `.github/workflows/` definiert und triggern auf verschiedene Events.

**Was sind GitHub Actions?** [1]

- Automatisierte Abläufe (Workflows), die auf Events reagieren
- Laufen auf GitHub-Servern (Runner) → Ubuntu, Windows, macOS
- Definiert in YAML-Dateien

Was ist ein Job? [2] -> Job = Zusammenhängende Aufgabe, die auf einem Runner ausgeführt wird
zb. das deployen oder testen.

## CD Workflow zur erklärung von Workflows

mit dem Trigger on -> Bedeutet der workflow wird aktiviert wenn ein tag mit v gepusht wird.

```yml
on:
  push:
    tags: ['v*']
```

Weiters werden jobs definiert und runner definiert

```yml
jobs:
  release:
    runs-on: ubuntu-latest
```

Und dann können die Steps gesetzt werden:

```yml
- uses: actions/checkout@v4
- uses: actions/setup-java@v4  # Java 17
- uses: gradle/actions/setup-gradle@v4  # Caching
- run: chmod +x ./gradlew  # Linux braucht executable flag
```

Wichtig: chmod +x nötig -> weil Windows keine Unix-Permissions setzt.

*Release erstellen*

```yml
- run: ./gradlew --no-daemon clean bootJar  # Fat JAR bauen

- uses: softprops/action-gh-release@v2
  with:
    files: backend/build/libs/backend.jar
  env:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

bootJar: Executable JAR mit allen Dependencies, GITHUB_TOKEN: Automatisch verfügbar, Benötigt permissions: contents: write

Nach dem ersten push ist aufgefallen es gibt ein permissionproblem das github nicht umgehen kann desshalb:
So wird gradle exicutable gemacht:

```
git update-index --chmod=+x gradlew 
```

## Badges & JaCoCo Integration

Um die Qualität des Codes sichtbar zu machen, wurden Badges für den Build-Status und die Testabdeckung (Coverage) in die `README.md` integriert.

### 1. Build Status Badge

zeigt an ob der letzte Build erfolgreich war (passing), Dieser wird direkt von GitHubbereitgestellt
Einbindung:

```bash
  ![Build Status](https://github.com/<USER>/<REPO>/actions/workflows/gradle.yml/badge.svg?branch=main)
```

### 2. JaCoCo Coverage Badges [3]

Diese Badges zeigen an, wie viel Prozent des Codes durch Tests abgedeckt sind.
**Tool:** [JaCoCo](https://www.eclemma.org/jacoco/) (Java Code Coverage) Plugin für Gradle.

Im Workflow:

1. Im Gradle-Build (`build.gradle`) wird das JaCoCo-Plugin aktiviert und der Report konfiguriert (`jacocoTestReport`).
2. Die GitHub Action (`gradle.yml`) führt `./gradlew test jacocoTestReport` aus.
3. Ein spezieller Step (`cicirello/jacoco-badge-generator`) liest den generierten CSV-Report (`jacocoTestReport.csv`) aus.
4. Der Generator erstellt SVG-Dateien (Badges) und speichert sie im Ordner `.github/badges/`.
5. Dann wird commited und beim pushen können die infos online eingesehen werden

## Cloud-Deployment

Für das Cloud-Deployment wurde das GitHub Student Developer Pack [4] genutzt, welches 200$ Guthaben für DigitalOcean bereitstellt.

Es wurde ein **Droplet** (virtueller Server) mit folgender Konfiguration erstellt:

- **OS:** Ubuntu mit vorinstalliertem Docker
- **Ressourcen:** 2 vCPUs, 4 GB RAM, 25 GB SSD Speicher

Nach der Erstellung des Droplets wurde das GitHub-Repository direkt auf den Server geklont. Anschließend wurden die Container mittels `docker-compose` gebaut und gestartet, wodurch die Anwendung (Frontend, Backend und Datenbank) nun öffentlich erreichbar ist. [5]

### Schnell start am Droplet oder auch lokal:

```bash
# Am Server zuerst rein SSHn 
ssh root@46.101.148.160

# Backend Builden
cd backend
./gradlew bootJar

## Zurück ins Main Verzeichnis
cd ..

# Falls noch container Laufen
docker compose down

# Applikation Starten
docker compose up --build -d
```

### Backup und Restore ausführen

Um den Backup Ordner auf den Server zu bekommen einfach mit ssh:

```bash
scp -r ./backup root@DEINE_SERVER_IP:/root/DEIN_REPO_NAME/
```

Weiters einfach wieder reinsshn und:

```bash
ls -la backup/

# In Container kopieren
docker cp backup postgres13:/tmp/backup

# Prüfen
docker exec postgres13 ls -la /tmp/backup/
```

Pfade im SQL-File anpassen

```bash
# Falls restore.sql absolute Pfade enthält, anpassen
docker exec postgres13 sed -i 's|/backup/|/tmp/backup/|g' /tmp/backup/restore.sql
```

#### Datenbank neu erstellen und Restore durchführen

```bash
# Backend stoppen (Verbindungen trennen)
docker compose stop backend

# Alte DB droppen und neu erstellen
docker exec postgres13 psql -U postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'venlab';"
docker exec postgres13 psql -U postgres -c "DROP DATABASE IF EXISTS venlab;"
docker exec postgres13 psql -U postgres -c "CREATE DATABASE venlab;"

# Restore durchführen
docker exec -i postgres13 psql -U postgres -d venlab -f /tmp/backup/restore.sql

# Backend wieder starten
docker compose start backend
```

Überprüfen:

```bash
docker exec postgres13 psql -U postgres -d venlab -c "SELECT COUNT(*) FROM venlab.box;"
docker exec postgres13 psql -U postgres -d venlab -c "SELECT COUNT(*) FROM venlab.analysis;"
```

### HTTPS und Mixed-Content-Fehler fixen

Um Service Worker nutzen zu können, ist HTTPS notwendig. Nach der Umstellung kam es zu "Mixed Content"-Fehlern, da das Frontend (via HTTPS) versuchte, das Backend über eine HTTP-URL anzusprechen.

#### Lösung

**Traefik als Reverse Proxy [6]:** In der `docker-compose.yml` wurde Traefik als zentraler Eingangspunkt konfiguriert.
- Alle Anfragen an `https://<domain>/api/...` werden an den Backend-Container weitergeleitet.
- Alle anderen Anfragen an den Frontend-Container.
- Eine automatische Weiterleitung von HTTP zu HTTPS wurde eingerichtet.

**Relative API-Pfade im Frontend [7]:** In `frontend/src/App.vue` wurde die API-Basis-URL von einer absoluten (`http://localhost:8081/api`) zu einer relativen URL (`/api`) geändert.

 const API = '/api'; ```
 Dadurch wird automatisch HTTPS verwendet und "Mixed Content" wird vermieden.

# Login-System für Lab Data Management PWA



#### 1. Spring Security Konfiguration

**Datei:** `backend/src/main/java/.../config/SecurityConfig.java`

- CSRF-Schutz deaktiviert (für REST-API)
- CORS deaktiviert (für lokale Entwicklung)
- Stateless Session Management (keine Server-Sessions)
- BCrypt Password Encoder für sichere Passwort-Speicherung
- In-Memory Benutzerverwaltung mit drei Rollen

#### 2. Authentifizierungs-Controller

**Datei:** `backend/src/main/java/.../controller/AuthController.java`

- `POST /api/auth/login` - Login-Endpoint
- Validierung von Benutzername und Passwort
- Rückgabe von Token, Username und Rolle bei Erfolg
- HTTP 401 Unauthorized bei ungültigen Anmeldedaten

#### 3. Dependencies

**Datei:** `backend/build.gradle`

```groovy
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'
```

### Frontend (Vue.js)

#### 1. Login-Formular Komponente

**Datei:** `frontend/src/components/LoginForm.vue`

- Benutzername und Passwort Eingabefelder
- Fehleranzeige bei ungültigen Anmeldedaten
- Loading-State während des Login-Vorgangs
- Emittiert `login-success` Event bei erfolgreichem Login

#### 2. Auth Service

**Datei:** `frontend/src/services/AuthService.js`

| Methode | Beschreibung |
| --- | --- |
| `login(username, password)` | Sendet Login-Request an API |
| `logout()` | Entfernt Benutzer aus localStorage |
| `getCurrentUser()` | Gibt aktuellen Benutzer zurück |
| `isLoggedIn()` | Prüft ob Benutzer eingeloggt ist |
| `getRole()` | Gibt Benutzerrolle zurück |

#### 3. API Interceptor

**Datei:** `frontend/src/services/api.js`

- Axios-Instanz mit Base-URL `http://localhost:8081/api/`
- Request Interceptor: Fügt automatisch `Authorization: Bearer <token>` Header hinzu
- Response Interceptor: Logging von API-Fehlern

#### 4. App Integration

**Datei:** `frontend/src/App.vue`

- Bedingte Anzeige: Login-Formular ODER Hauptanwendung
- `onLoginSuccess()` - Lädt Daten nach erfolgreichem Login
- `logout()` - Benutzer-Abmeldung mit Daten-Reset
- Automatische Session-Wiederherstellung beim Seitenaufruf

## Benutzer Credential

| Benutzername | Passwort | Rolle |
| --- | --- | --- |
| admin | password | ADMIN |

## Login-Flow

```
┌─────────────┐     POST /api/auth/login       ┌─────────────┐
│   Frontend  │ ──────────────────────────────▶│   Backend   │
│  LoginForm  │    {username, password}        |AuthController│
└─────────────┘                                └─────────────┘
                                                      │
                                                      ▼
                                               ┌─────────────┐
                                               │   Spring    │
                                               │  Security   │
                                               └─────────────┘
                                                      │
                     {token, username, role}          │
┌─────────────┐ ◀──────────────────────────────────────┘
│   Frontend  │
│ AuthService │ ──▶ localStorage speichern
└─────────────┘
       │
       ▼
┌─────────────┐
│   App.vue   │ ──▶ isLoggedIn = true
│  loadData() │ ──▶ Daten laden mit Token
└─────────────┘
```

## Probleme & Lösungen während der Entwicklung

### Problem 1: Login Redirect Loop

**Symptom:** Nach dem Login wurde man sofort wieder ausgeloggt.

**Ursache:** Der Response Interceptor in `api.js` hat bei jedem API-Fehler (z.B. 403) automatisch `window.location.reload()` ausgeführt.

**Lösung:** Interceptor vereinfacht - nur noch Logging, kein automatisches Logout.

### Problem 2: 403 Forbidden auf Login-Endpoint

**Symptom:** `POST /api/auth/login` gab 403 zurück.

**Ursache:** Spring Security 6 erfordert neue Syntax für die Konfiguration.

**Lösung:** `AbstractHttpConfigurer::disable` statt Lambda-Ausdrücke verwenden.

### Problem 3: Gradle Wrapper Corruption

**Symptom:** `zip END header not found` beim Docker Build.

**Ursache:** Beschädigte JAR-Datei im Gradle Cache durch gesperrte Dateien.

**Lösung:**

1. IntelliJ schließen
2. Java-Prozesse beenden
3. Gradle Cache löschen: `rd /s /q C:\Users\<user>\.gradle\wrapper\dists`
4. IntelliJ neu starten


## PWA Konfiguration

Die Progressive Web App Funktionalität wurde mit dem Vite PWA Plugin implementiert. Die Konfiguration erfolgt in `vite.config.js`:

```javascript
import { VitePWA } from 'vite-plugin-pwa'

VitePWA({
    registerType: 'autoUpdate',
    manifest: {
        name: 'Labor Management System',
        short_name: 'LabMS',
        display: 'standalone',
        theme_color: '#667eea',
        icons: [/* ... */]
    },
    workbox: {
        globPatterns: ['**/*.{js,css,html,ico,png,svg}'],
        runtimeCaching: [/* API Caching */]
    }
})
```

Die Service Worker Registrierung erfolgt in `main.js`:

```javascript
import { registerSW } from 'virtual:pwa-register'

registerSW({
    onNeedRefresh() {
        if (confirm('Neue Version verfügbar! Jetzt aktualisieren?')) {
            updateSW(true)
        }
    },
    onOfflineReady() {
        console.log('App ist offline-bereit')
    }
})
```

Das Web App Manifest definiert die Installierbarkeit. Der Parameter `display: 'standalone'` ermöglicht die Darstellung ohne Browser-UI im Vollbildmodus. Die App wird über Icons in zwei Größen (192x192 und 512x512 Pixel) auf dem Startbildschirm darstellbar.

Service Worker ermöglichen Offline-Funktionalität durch Caching von statischen Ressourcen und API-Antworten. Die `globPatterns` definieren welche Dateitypen beim ersten Laden gecacht werden. Die `runtimeCaching` Strategie verwendet `NetworkFirst` für API-Calls mit Fallback auf gecachte Daten bei fehlender Internetverbindung.

Die Offline-Funktionalität erfordert HTTPS. Service Worker funktionieren ausschließlich über HTTPS-Verbindungen oder auf localhost. Für Production wurde ein DuckDNS Domain mit Let's Encrypt SSL-Zertifikat konfiguriert. Nginx dient als Reverse Proxy und leitet Port 80/443 auf den Application Port weiter.

## Auswahlfeld von zu anzeigenden Attributen der einzelnen Tabellen

Die Spaltenauswahl ermöglicht das Ein- und Ausblenden einzelner Tabellenspalten. Die Implementierung besteht aus der `ColumnSelector.vue` Komponente und State Management in `App.vue`.

Der State wird in `columnVisibility` verwaltet:

```javascript
data() {
    return {
        columnVisibility: {
            analysis: {},
            sample: {},
            box: {},
            boxpos: {},
            log: {}
        }
    }
}
```

Eine Computed Property filtert die sichtbaren Spalten:

```javascript
computed: {
    visibleColumns() {
        const view = this.view
        const allCols = this.columns[view]
        const visibility = this.columnVisibility[view]
        return allCols.filter(col => visibility[col] !== false)
    }
}
```

Die Benutzerpräferenzen werden in localStorage persistiert:

```javascript
saveColumnVisibility() {
    localStorage.setItem('columnVisibility', JSON.stringify(this.columnVisibility))
}

loadColumnVisibility() {
    const saved = localStorage.getItem('columnVisibility')
    if (saved) {
        this.columnVisibility = JSON.parse(saved)
    }
}
```

Die `ColumnSelector.vue` Komponente rendert eine Dropdown-Liste mit Checkboxen für jede Spalte. Um zu verhindern dass alle Spalten ausgeblendet werden, wird die letzte sichtbare Spalte deaktiviert. Die Komponente emittiert Events für toggle, select-all und reset Aktionen.

Die DataTable Komponente erhält die gefilterte Spaltenliste über Props:

```vue
<DataTable :columns="visibleColumns" />
```

## Light/Dark Theme

Das Theme-System verwendet CSS Custom Properties und eine zentrale Theme-Verwaltung. Die Implementierung erfolgt mit der `ThemeSwitcher.vue` Komponente und globalen CSS Variables.

Der Theme State wird in `App.vue` verwaltet:

```javascript
data() {
    return {
        isDarkMode: false
    }
},
methods: {
    toggleTheme() {
        this.isDarkMode = !this.isDarkMode
        localStorage.setItem('darkMode', this.isDarkMode)
        this.applyTheme()
    },
    applyTheme() {
        if (this.isDarkMode) {
            document.documentElement.classList.add('dark-mode')
        } else {
            document.documentElement.classList.remove('dark-mode')
        }
    }
}
```

CSS Variables definieren die Theme-Farben:

```css
:root {
    --bg-primary: #f3f4f6;
    --bg-secondary: #ffffff;
    --text-primary: #1f2937;
    --border-color: #e5e7eb;
}

:root.dark-mode {
    --bg-primary: #111827;
    --bg-secondary: #1f2937;
    --text-primary: #f9fafb;
    --border-color: #374151;
}
```

Alle Komponenten referenzieren diese Variables statt feste Farbwerte:

```css
.table-container {
    background: var(--bg-secondary);
    color: var(--text-primary);
}
```

Die Theme-Präferenz wird beim Laden aus localStorage wiederhergestellt. Der Theme-Wechsel erfolgt durch Hinzufügen oder Entfernen der `dark-mode` CSS-Klasse am `document.documentElement`. Alle Farbübergänge werden mit CSS Transitions animiert für eine flüssige Benutzererfahrung.

***

## Quellen

[1] „Quickstart for GitHub Actions“, GitHub Docs. Zugegriffen: 8. Jänner 2026. [Online]. Verfügbar unter: https://docs.github.com/en/actions/quickstart

[2] „GitHub Actions Workflows: How to Create and Manage“, Spacelift. Zugegriffen: 8. Jänner 2026. [Online]. Verfügbar unter: https://spacelift.io/blog/github-actions-workflows

[3] „JaCoCo - Java Code Coverage Library“, Eclemma.org. Zugegriffen: 8. Jänner 2026. [Online]. Verfügbar unter: https://www.eclemma.org/jacoco/

[4] „GitHub Student Developer Pack“, GitHub Education. Zugegriffen: 8. Jänner 2026. [Online]. Verfügbar unter: https://education.github.com/pack

[5] „Quickstart for App Platform“, DigitalOcean Docs. Zugegriffen: 8. Jänner 2026. [Online]. Verfügbar unter: https://docs.digitalocean.com/products/app-platform/getting-started/quickstart/#create-an-app

[6] „Traefik Proxy Documentation“, Traefik Labs. Zugegriffen: 8. Jänner 2026. [Online]. Verfügbar unter: https://doc.traefik.io/traefik/

[7] „Mixed Content“, MDN Web Docs. Zugegriffen: 8. Jänner 2026. [Online]. Verfügbar unter: https://developer.mozilla.org/en-US/docs/Web/Security/Mixed_content