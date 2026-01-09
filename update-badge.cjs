const fs = require('fs')
const path = require('path')

const resultsPath = path.join(__dirname, 'results.json')

try {
    if (!fs.existsSync(resultsPath)) {
        console.log('Keine results.json gefunden - Badge wird nicht aktualisiert')
        console.log('Gesucht in:', resultsPath)
        process.exit(0)
    }

    const results = JSON.parse(fs.readFileSync(resultsPath, 'utf8'))

    const passed = results.totals?.passed || 0
    const failed = results.totals?.failed || 0
    const total = results.totals?.tests || 0
    const color = failed > 0 ? 'red' : 'success'

    const badgeUrl = `https://img.shields.io/badge/tests-${passed}%20passed-${color}`
    const totalBadgeUrl = `https://img.shields.io/badge/total-${total}%20tests-blue`

    const date = new Date().toLocaleDateString('de-DE', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    })

    const readmePath = path.join(__dirname, 'README.md')

    if (!fs.existsSync(readmePath)) {
        console.log('README.md nicht gefunden')
        process.exit(0)
    }

    let readme = fs.readFileSync(readmePath, 'utf8')

    readme = readme.replace(
        /!\[tests\]\(https:\/\/img\.shields\.io\/badge\/tests-[^)]+\)/i,
        `![tests](${badgeUrl})`
    )

    readme = readme.replace(
        /!\[total\]\(https:\/\/img\.shields\.io\/badge\/total-[^)]+\)/i,
        `![total](${totalBadgeUrl})`
    )

    readme = readme.replace(
        /\*\*Letzte Ausführung\*\*: [^\n]+/,
        `**Letzte Ausführung**: ${date}`
    )

    readme = readme.replace(
        /\*\*Ergebnis\*\*: [^\n]+/,
        `**Ergebnis**: ${failed > 0 ? '⚠️' : '✅'} ${passed} passed, ${failed} failed (${total} total)`
    )

    fs.writeFileSync(readmePath, readme)
    console.log(`README aktualisiert: ${passed} passed, ${failed} failed von ${total} total`)

} catch (error) {
    console.error('Fehler beim Aktualisieren der README:', error.message)
    process.exit(1)
}
