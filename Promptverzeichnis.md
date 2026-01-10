# Prompt-Verzeichnis: Vue PWA Projekt

**Projekt:** INSY Informationssysteme - Vue PWA  
**Team:** Koenig, Schieder, Wiedenegger



**Prompt:**
> Wie konfiguriere ich meine Vue.js App als PWA? Sie soll auf dem Startbildschirm installierbar sein und im Vollbild ohne Browser-UI öffnen.

**Output:**

```bash
npm install vite-plugin-pwa -D
```

In `vite.config.js`:
```javascript
import { VitePWA } from 'vite-plugin-pwa'

plugins: [vue(), VitePWA({
  registerType: 'autoUpdate',
  manifest: {
    name: 'Lab Data Management',
    short_name: 'LabData',
    display: 'standalone',  // Vollbild ohne Browser-UI
    icons: [
      { src: '/icon-192.png', sizes: '192x192', type: 'image/png' },
      { src: '/icon-512.png', sizes: '512x512', type: 'image/png' }
    ]
  }
})]
```

Icons (192x192 und 512x512 PNG) in `public/` ablegen.


**Prompt:**
> Die API soll mit einem einfachen Login geschützt werden. Wie implementiere ich das mit Spring Security?

**Output:**

Dependencies in `build.gradle`:
```groovy
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
```

`SecurityConfig.java` - In-Memory Users für Demo:
```java
@Bean
public UserDetailsService userDetailsService(PasswordEncoder encoder) {
    return new InMemoryUserDetailsManager(
        User.builder().username("admin").password(encoder.encode("password")).roles("ADMIN").build(),
        User.builder().username("reader").password(encoder.encode("password")).roles("READER").build()
    );
}
```


**Prompt:**
> Erstelle einen AuthController mit POST /api/auth/login Endpoint der Username, Passwort validiert und Token zurückgibt.

**Output:**

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        // Validierung gegen UserDetailsService
        return ResponseEntity.ok(Map.of(
            "token", "simple-token-" + username,
            "username", username,
            "role", "ADMIN"
        ));
    }
}
```


**Prompt:**
> Wie speichere ich den Login-Token im Frontend und füge ihn automatisch zu API-Requests hinzu?

**Output:**

`AuthService.js`:
```javascript
login(username, password) {
  return api.post('/auth/login', { username, password })
    .then(response => {
      localStorage.setItem('user', JSON.stringify(response.data));
      return response.data;
    });
}
```

`api.js` - Axios Interceptor:
```javascript
api.interceptors.request.use(config => {
  const user = JSON.parse(localStorage.getItem('user'));
  if (user?.token) {
    config.headers['Authorization'] = 'Bearer ' + user.token;
  }
  return config;
});
```


**Prompt:**
> Wie implementiere ich Dark/Light Theme mit localStorage Speicherung?

**Output:**

In `App.vue`:
```javascript
data() { 
  return { theme: localStorage.getItem('theme') || 'light' } 
},
methods: { 
  toggleTheme() { 
    this.theme = this.theme === 'light' ? 'dark' : 'light';
    localStorage.setItem('theme', this.theme);
  }
}
```

Template: `<div :class="theme">` und CSS-Variablen für Farben.


**Prompt:**
> Wie erstelle ich ein Auswahlfeld für Tabellen-Attribute (Column Selector)?

**Output:**

`ColumnSelector.vue` mit Checkbox pro Spalte:
```vue
<div v-for="col in allColumns" :key="col">
  <input type="checkbox" :checked="visibility[col]" @change="$emit('toggle', col)">
  {{ col }}
</div>
```

In `App.vue` filtern:
```javascript
visibleColumns() {
  return this.columns[this.view].filter(col => this.columnVisibility[this.view][col] !== false);
}
```


**Prompt:**
> Ich habe lokale Änderungen und will vom Remote pullen, aber Git sagt "divergent branches". Was tun?

**Output:**

Merge-Strategie setzen (empfohlen für Teamarbeit):
```bash
git config pull.rebase false
git pull
```

Bei Konflikten: `git status` → Dateien editieren → `git add . && git commit`


**Prompt:**
> Wie richte ich CI/CD mit GitHub Actions für das repo ein?

**Output:**

`.github/workflows/ci-cd.yml`:
```yaml
name: CI/CD
on: [push]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
    - uses: actions/setup-node@v4
      with: { node-version: 20 }
    - run: cd frontend && npm ci && npx cypress run
    - uses: actions/setup-java@v4
      with: { java-version: 17 }
    - run: cd backend && ./gradlew test
```

GitHub erkennt `.github/workflows/` automatisch nach Push.


**Prompt:**
> Wie deploye ich das repo auf DigitalOcean?

**Output:**

1. GitHub Student Pack aktivieren
2. DigitalOcean App Platform → Create App → Monorepo auswählen
3. Services konfigurieren:
    - Frontend: Static Site, Source `/frontend`, Build `npm run build`
    - Backend: Docker, Source `/backend`
    - Database: Managed PostgreSQL hinzufügen
4. Auto-deploy on push aktiviert, HTTPS inklusive



**Prompt:**
> Nach dem Login werde ich sofort wieder ausgeloggt. Die Seite lädt sich ständig neu.

**Output:**

Problem: Der Response Interceptor in `api.js` macht bei jedem API-Fehler `window.location.reload()`.

Lösung - Interceptor vereinfachen:
```javascript
api.interceptors.response.use(
  response => response,
  error => {
    console.log('API Error:', error.response?.status);
    return Promise.reject(error);  // Kein reload!
  }
);
```


**Prompt:**
> Gradle Build schlägt fehl mit "zip END header not found". Was ist das Problem?

**Output:**

Korrupter Gradle Cache. Lösung:
1. IntelliJ schließen
2. Java-Prozesse im Task Manager beenden
3. Cache löschen: `rd /s /q C:\Users\<user>\.gradle\wrapper\dists`
4. IntelliJ neu starten
5. Gradle Tab → backend → Tasks → build → bootJar


