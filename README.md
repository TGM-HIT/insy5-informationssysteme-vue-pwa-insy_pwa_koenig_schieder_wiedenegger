![Build Status](https://github.com/TGM-HIT/insy5-informationssysteme-vue-pwa-insy_pwa_koenig_schieder_wiedenegger/actions/workflows/gradle.yml/badge.svg?branch=main)
![Coverage](.github/badges/jacoco.svg)
![Branches](.github/badges/branches.svg)

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

## Quellen

[1] „Quickstart for GitHub Actions“, GitHub Docs. Zugegriffen: 8. Jänner 2026. [Online]. Verfügbar unter: https://docs.github.com/en/actions/quickstart

[2] „GitHub Actions Workflows: How to Create and Manage“, Spacelift. Zugegriffen: 8. Jänner 2026. [Online]. Verfügbar unter: https://spacelift.io/blog/github-actions-workflows

[3] „JaCoCo - Java Code Coverage Library“, Eclemma.org. Zugegriffen: 8. Jänner 2026. [Online]. Verfügbar unter: https://www.eclemma.org/jacoco/
