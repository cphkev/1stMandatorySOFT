# Resultater og fejl

## Del 1: Resultater

### Overblik

Vi har kørt hele collectionen i Collection Runner med 5 iterationer.
Alle tests bestod, **undtagen BULK-12 og BULK-13** (se del 2).

### Hvad viser testene os?

| Område | Hvad testene viser |
|---|---|
| **Alle ni funktioner** | Svarer med status 200, i JSON, og med alle krævede felter udfyldt |
| **CPR** | 10 cifre, mulig dato, sidste ciffer passer til køn, og CPR matcher fødselsdatoen (CPR-03, PERSON-01 og hver person i bulk) |
| **Fødselsdato** | Altid en rigtig kalenderdato i formatet `ÅÅÅÅ-MM-DD` |
| **Adresse** | Husnummer, etage og dør overholder opgavens regler. Postnummer har fire cifre |
| **Mobilnummer** | 8 cifre og begynder på et tal fra opgavens liste |
| **Datakilder** | Navne, efternavne og postnummer/by-par findes i facitlisten for alle personer, op til 100 ad gangen |
| **Bulk, grænser** | 1 og 101 afvises. 2, 3, 99 og 100 giver præcis det bestilte antal personer |
| **Bulk, ugyldigt** | 0, -1, `abc` og 2.5 afvises med status 400 |
| **Tilfældighed** | 100 personer har forskellige CPR-numre og vejnavne. Generatoren er ikke gået i stå |

**Samlet:** i alle kald, vi har kørt, overholder programmet opgavens regler, og det afviser
ugyldige antal personer.

### Hvad viser testene *ikke*?

| Ikke vist | Hvorfor |
|---|---|
| At bestemte værdier kan forekomme (husnummer 999, 29. februar, alle mobilbegyndelser) | Vi kan ikke bestille bestemte værdier. Tilfældige svar beviser det ikke. Dækkes af unit-tests |
| At programmet faktisk slår op i databasen og navnefilen | Vi ser kun, at svarene findes i facitlisten. Undersøges i integrationstests |
| At data er *meget* tilfældige | BULK-14 viser kun, at generatoren ikke er fastlåst |
| Opførsel, hvis databasen eller navnefilen mangler | Kræver et særskilt testmiljø |

---

## Del 2: Tests med fejl

To tests fejler. De har samme årsag.

### Oversigt

| Test | Kald | Vi forventede | Vi fik | Fejlede kontroller |
|---|---|---|---|---|
| **BULK-12** | `/api/persons?count=` (tom) | Status 400 | Status 200 og 2 personer | Status er 400. Svaret indeholder ikke personer |
| **BULK-13** | `/api/persons` (uden `count`) | Status 400 | Status 200 og 2 personer | Status er 400. Svaret indeholder ikke personer |

Alle datakilde-tests bestod i de to svar. Dataene i de 2 personer er altså gyldige.

### Hvad er fejlen?

Testen forventer, at et tomt eller manglende antal afvises. Programmet afviser ikke, men leverer
**2 personer**.

### Hvorfor kommer den?

Programmet bruger sandsynligvis **2 som standardværdi**, når `count` mangler eller er tom.
Det er en rimelig løsning, fordi 2 ligger inden for det tilladte.

Opgaven siger ikke, hvad der skal ske uden `count`. Forventningen om 400 var vores egen antagelse,
og den er ikke afgjort af gruppen. Vores tidligere udkast til testplan forventede faktisk
standardværdien 2 med status 200 ("skal bekræftes").

Så der er to mulige forklaringer:

- **A)** Standardværdien er et bevidst valg. Så er **testens forventning forkert**.
- **B)** Programmet burde have afvist kaldet. Så er der en **rigtig fejl i programmet**.

Bemærk: i Postman er BULK-12 tom værdi og BULK-13 udeladt. I det tidligere udkast var rækkefølgen omvendt.

### Hvad skal der gøres?

**Trin 1. Spørg gruppen (backend-ansvarlig):** *"Er det med vilje, at tom eller manglende `count`
giver 2 personer?"*

**Trin 2a. Hvis ja (standardværdi 2):** ret testene, så de forventer det. Erstat koden i BULK-12
(skift navnet til BULK-13 i den anden):

```javascript
// Tom count bruger standardværdien 2 (aftalt adfærd)
pm.test("BULK-12: Tom count giver standardværdien 2", function () {
    pm.response.to.have.status(200);

    const body = pm.response.json();

    pm.expect(body).to.be.an("array");
    pm.expect(body.length).to.equal(2);
});
```

**Trin 2b. Hvis nej (skulle afvises):** fejlen skal rettes i programmet, så `count` er påkrævet og
skal være et helt tal fra 2 til 100, ellers status 400. Testene **ændres ikke** og er røde, til
rettelsen er lavet.

**Trin 3. Skriv beslutningen ned** i testplanen: hvad blev besluttet, af hvem og hvornår.

**Trin 4. Kør hele collectionen igen**, så BULK-12 og 13 er grønne.

**Tjek også:** åbn Postman Console og se, at BULK-12 faktisk sender `?count=`. Hvis den tomme
parameter er droppet, tester den i virkeligheden BULK-13.

**Tidsramme:** afleveringen er 19. oktober kl. 23:59. Hvis røde tests skal med i afleveringen,
så beskriv dem som kendt fejl og forklar, hvorfor de er røde.
