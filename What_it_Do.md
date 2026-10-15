# Opgavefordeling: ansvar, testteknikker og færdigt resultat

## 1. Hvad hører til hvilken teknik?

| Teknik | Hvordan arbejder I? | Eksempel i opgaven |
|---|---|---|
| **Black-box** | Find testcases ud fra kravene uden at bruge implementeringen som facit. | Bulk skal acceptere 2–100 personer. Test `1, 2, 3, 99, 100, 101`. |
| **White-box** | Undersøg kodens grene og brug coverage til at finde manglende tests. | Kontrollér, at tests udfører både `female`-grenen og `male`-grenen i CPR-generatoren. |
| **Experience-based** | Find mulige fejl ud fra erfaring og viden om systemet. | Databasen er utilgængelig, navne indeholder `Æ/Ø/Å`, eller et API-kald bruger `count=abc`. |
| **Statisk analyse** | Undersøg kildekoden med et analyseværktøj uden at køre programmet. | Brug SpotBugs, vurder fund, ret relevante fejl og dokumentér forbedringen. |

Samme test kan dække flere formål. En test af kvinders CPR kan både kontrollere et krav fra black-box-analysen og dække en gren i white-box-analysen.

## 2. De fire medlemmers opgaver

### Medlem 1 — Generatorlogik og unit tests

**Black-box-opgaver:**

- Beskriv gyldige og ugyldige output for CPR, mobilnummer og adresse.
- Lav grænseværdier for CPR-længde, mobilnummerlængde, husnummer, etage, numerisk dør og bulk-antal.
- Lav en beslutningstabel for sammenhængen mellem køn og CPR’s sidste ciffer.
- Skriv JUnit-tests for kravene:
  - CPR har ti cifre og matcher fødselsdatoen.
  - Kvinder har lige sidste ciffer; mænd har ulige.
  - Mobilnummer har otte cifre og et tilladt præfiks.
  - Adressefelterne følger opgavens formater.
  - Bulk accepterer 2–100 personer og afviser antal udenfor.

**White-box-opgaver:**

- Undersøg generatorens `if/else`, løkker og øvrige valg.
- Brug JaCoCo-rapporten til at finde grene, testene ikke udfører.
- Tilføj målrettede tests for eksempelvis begge køn, husnummer med/uden bogstav og alle dørtyper.
- Gør tilfældigheden styrbar, så bestemte grene og grænser kan testes stabilt.

**Experience-based-opgaver:**

- Test skuddag, årsskifte og CPR med indledende nul.
- Kontrollér, at gentagne kald stadig overholder kravene; kræv ikke unikke personer, da opgaven ikke angiver dette.

**For at være færdig:**

- Unit tests kører uden Spring Boot og uden rigtig database eller filer.
- Tests bruger AAA og faste forventninger fra kravene.
- Fundne fejl i generatoren er rettet.
- Black-box-afsnit og forklaring af relevante dækkede grene er afleveret til medlem 3.

### Medlem 2 — Datakilder og integrationstests

**Implementering:**

- Opsæt MariaDB og indlæs de udleverede data fra `addresses.sql`.
- Erstat startkodens læsning af SQL som tekst med databaseopslag.
- Adskil dataadgang fra generatorlogikken, så medlem 1 kan teste logikken isoleret.
- Dokumentér lokal databaseopsætning og opsæt et isoleret testmiljø.

**Black-box-opgaver:**

- Test, at returnerede postnummer/by-par findes sammen i datakilden.
- Test, at fornavn, efternavn og køn stammer fra navnefilen med korrekt sammenhæng mellem fornavn og køn.
- Test samspillet mellem controller, service og datakilder med Spring Boot.
- Brug de eksisterende MockMvc-tests som udgangspunkt.

**White-box-opgaver:**

- Undersøg dataindlæsningens og fejlhåndteringens grene.
- Brug JaCoCo til at identificere relevante forløb, som integrationstestene ikke rammer.
- Tilføj tests for de understøttede succes- og fejlforløb.

**Experience-based-opgaver:**

- Test utilgængelig database, tomme data samt manglende eller ugyldig navnefil.
- Kontrollér håndtering af danske tegn.
- Definér forventet fejlhåndtering tydeligt; egne valg skal ikke præsenteres som udtrykkelige opgavekrav.

**For at være færdig:**

- Applikationen bruger den rigtige database.
- Integrationstestene tester rigtige komponenter sammen.
- Testdata er adskilt fra almindelige data.
- Database-script, opsætningsvejledning og tests virker lokalt og i CI.
- Dokumentation af integrationer og fejlsituationer er afleveret til medlem 3.

### Medlem 3 — API-tests og black-box-PDF

**Black-box-opgaver:**

- Lav en Postman-collection for alle ni funktioner:
  - CPR.
  - Navn og køn.
  - Navn, køn og fødselsdato.
  - CPR, navn og køn.
  - CPR, navn, køn og fødselsdato.
  - Adresse.
  - Mobilnummer.
  - Én komplet person.
  - 2–100 komplette personer.
- Kontrollér statuskode, JSON-content-type, felter, formater og sammenhæng mellem værdier.
- Test bulk-grænserne `1, 2, 3, 99, 100, 101`.
- Kontrollér det præcise antal returnerede personer ved gyldige kald.
- Test ugyldige parametre som `count=abc` og `count=2.5`.

**Dokumentationsopgaver:**

- Saml gruppens black-box-analyse i én PDF.
- Brug en enkel struktur for hvert område: **krav → partitioner → BVA → testcases → forventet resultat**.
- Medtag beslutningstabellen for køn og CPR.
- Giv testcases ID’er, så dokumentation og automatiske tests kan forbindes.
- Skeln mellem krav fra opgaven og gruppens egne beslutninger.

**White-box-ansvar:**

- Har ikke hovedansvaret for white-box.
- Hjælper med at samle gruppens forklaring af coverage og de tests, analysen førte til.

**For at være færdig:**

- Alle ni funktioner er dækket af automatiske API-assertions.
- Collection og miljøopsætning kan køres med Newman uden manuel indtastning.
- Black-box-PDF’en indeholder alle medlemmers relevante bidrag.
- Den samlede afleverings-ZIP er pakket og kontrolleret af gruppen.

### Medlem 4 — Frontend, E2E, white-box-værktøj og CI

**Black-box-opgaver:**

- Skriv Playwright-tests fra brugerens perspektiv.
- Test, at brugeren kan generere fem personer og se præcis fem resultater.
- Kontrollér viste CPR- og mobilnummerformater.
- Test brugerfladens håndtering af ugyldigt antal personer.
- Ret frontend-fejl, som testene finder.

**White-box-opgaver:**

- Opsæt JaCoCo og sørg for, at både unit- og integrationstests indgår i coverage-rapporten.
- Gennemgå udækkede linjer og grene sammen med medlem 1 og 2.
- Dokumentér mindst et konkret eksempel på, at coverage afslørede en manglende test.
- Forklar forskellen på line coverage og branch coverage. Høj coverage er ikke i sig selv bevis for korrekte tests.

**Statisk analyse — separat fra white-box:**

- Opsæt SpotBugs.
- Vurdér fund sammen med kodeejeren.
- Gem screenshots af relevante fund og resultatet efter rettelser.
- Beskriv den konkrete værdi frem for kun at vise et grønt resultat.

**CI-opgaver:**

- Opsæt GitHub Actions ved push og pull requests.
- Sørg for database, build og en kørende applikation til API- og E2E-tests.
- Kør unit tests, integrationstests, SpotBugs, JaCoCo, Newman og Playwright.
- Lad pipeline fejle ved testfejl og gem rapporter som artifacts.

**For at være færdig:**

- E2E-testene kører automatisk uden manuel browserbetjening.
- Hele pipeline virker fra en ren checkout.
- Coverage- og analyserapporter er tilgængelige.
- Screenshots og forklaring af værktøjernes værdi er klar til aflevering.

## 3. Arbejdsrækkefølge og fælles aftaler

1. **Fastlås den eksisterende API-kontrakt:** `/api/...`, `count` og nuværende JSON-felter. Kravene er facit for indholdets gyldighed.
2. **Skriv black-box-testdesign først.** Brug `SQuality` som inspiration til struktur, men udled reglerne fra denne opgave.
3. **Adskil datakilder og generatorlogik tidligt.** Medlem 1 ejer generatoren; medlem 2 ejer fil- og databaseadgang.
4. **Arbejd parallelt.** Medlem 3 starter API-tests mod startkoden, mens medlem 4 starter E2E og CI.
5. **Kør tests og ret fundne fejl.** Hver kodeejer retter fejl på sit område.
6. **Brug white-box bagefter til at supplere tests.** Gennemgå JaCoCo, find manglende grene, tilføj tests og dokumentér resultatet.
7. **Kør statisk analyse og dokumentér forbedringer.**
8. **Afprøv hele afleveringen fra en ren checkout og øv præsentationen.**

Fælles værktøjer: Java/Spring Boot, JUnit, MariaDB, Postman/Newman, Playwright, JaCoCo, SpotBugs og GitHub Actions. Der sættes ikke et kunstigt krav om 100 % coverage; relevante udækkede grene skal undersøges og forklares.

## 4. Hvornår er I i mål?

- [ ] Applikationen har alle ni funktioner og en fungerende grafisk brugerflade.
- [ ] Genererede oplysninger overholder opgaven og er indbyrdes konsistente.
- [ ] Unit-, integrations-, API- og E2E-tests er implementeret og består.
- [ ] Black-box-PDF’en viser partitioner, grænseværdier og konkrete testcases.
- [ ] White-box-arbejdet kan forklares med coverage og eksempler på supplerende tests.
- [ ] Experience-based testcases er beskrevet og relevante cases automatiseret.
- [ ] Statisk analyse ud over linting er gennemført, og dens værdi dokumenteret med screenshots.
- [ ] CI kører alle testtyper og den statiske analyse automatisk.
- [ ] ZIP’en indeholder kildekode, database-script, tests, CI-konfiguration, black-box-PDF og screenshots.
- [ ] En anden i gruppen kan starte projektet og køre tests ud fra README.
- [ ] Alle fire har en del af præsentationen, og I holder jer inden for ti minutter.

**Aflevering:** 19. oktober 2026 kl. 23.59.  
**Præsentation:** 20. oktober 2026. Alle viser deres eget område, og E2E-testen køres under præsentationen.
