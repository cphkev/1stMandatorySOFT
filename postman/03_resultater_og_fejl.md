# Resultater og fejl

## 1. Status efter rettelserne

Sander har efter rettelserne oplyst, at Postman-testene kører grønt uden fejl.
Det tidligere screenshot viste 2.227 beståede assertions i én iteration, men blev
taget før de seneste ekstra adressekontroller. Det tal bruges derfor ikke som
assertion-antal for den seneste kørsel. Gem en ny rapport eller et screenshot til afleveringen.

Der er ikke udført en ny testkørsel som del af denne dokumentationsopdatering.

| Område | Hvad kontrolleres? |
|---|---|
| Alle ni funktioner | Successtatus, JSON og relevante indholdskontroller |
| CPR | Ti cifre, mulig dato og sammenhæng med køn; sammenhæng med fødselsdato, hvor begge returneres og kontrolleres |
| Adresse | Husnummer, etage, dør og postnummer; egne formatregler om foranstillede nuller |
| Mobilnummer | Otte cifre og et tilladt præfiks |
| Datakilder | Fornavn/køn-par, efternavn og postnummer/by-par findes i reference-data i relevante succesfulde svar |
| Bulk-grænser | 1 og 101 afvises; 2, 3, 99 og 100 giver det bestilte antal |
| Ugyldigt input | 0, -1, `abc` og 2.5 afvises med status 400 |
| Standardværdi | BULK-12 og BULK-13 forventer status 200 og præcis 2 personer |
| Variation | BULK-14 kræver mere end ét forskelligt CPR og mere end ét forskelligt vejnavn blandt 100 personer |

En grøn kørsel viser, at de udførte assertions bestod for de genererede svar.
Den beviser ikke, at alle mulige output eller fejlforløb er dækket.

## 2. Afklaret: BULK-12 og BULK-13

| Test | Kald | Tidligere forventning | Aftalt forventning |
|---|---|---|---|
| BULK-12 | `/api/persons?count=` | 400 | 200 og præcis 2 personer |
| BULK-13 | `/api/persons` | 400 | 200 og præcis 2 personer |

Ved gennemgangen den 5. november 2026 valgte Sander at beholde standardværdien 2.
Controlleren har eksplicit `@RequestParam(defaultValue = "2")`.
Opgaven kræver bulk på 2–100 personer, men bestemmer ikke, hvad der sker ved tomt
eller manglende `count`. Forventningen om 400 var derfor en antagelse i testene.
Testene er ændret til den valgte kontrakt; backend-adfærden er bevaret.

De to cases holdes adskilt, fordi et tomt parameter og et udeladt parameter er
forskellige requests. De fælles datakilde-tests og kontrollen af foranstillede
nuller kører også på de to personlister.

## 3. Ensartet kontrol af foranstillede nuller

ADDRESS-01 kontrollerede allerede, at husnummer og numerisk dør ikke starter med 0.
PERSON-01 og BULK-02, 03, 04, 05, 07, 12, 13 og 14 har nu samme kontrol.
I bulk kontrolleres hver persons adresse.

Kontrollen foretages på teksten, fordi `Number("007")` bliver til `7` og dermed
kan bestå en almindelig intervalkontrol. Reglen afviser eksempelvis husnummer
`007` og numerisk dør `05`. Den ændrer ikke reglerne for døre som `th` eller `c3`.
Dette er en egen formatbeslutning, ikke et udtrykkeligt forbud i opgaven.

## 4. BULK-14: variation og testnavne

BULK-14 tester, at CPR-numrene ikke alle er ens, og at vejnavnene ikke alle er ens.
Den kræver ikke, at alle 100 værdier er unikke, og den beviser ikke en bestemt
statistisk fordeling.

**Resterende navneoprydning:** Den lokale YAML-fil indeholder ved denne gennemgang
stadig tre assertions med navnet `BULK-17`, selv om requesten hedder BULK-14.
Den nye adressekontrol hedder allerede BULK-14. Gem rettelsen af de tre gamle
navne i Postman, så den kommer med i projektfilerne og næste commit.
Dette påvirker læsbarheden af resultaterne, ikke selve testbetingelserne.

## 5. CI-status

`.github/workflows/ci.yml` indeholder nu opstart af database og API samt kørsel af
collectionen med Postman CLI og miljøet med reference-data. Maven-tests,
JaCoCo og SpotBugs køres før API-testene.

Workflowet starter ved push til `main`, inklusive merges, men springer over,
hvis alle ændrede filer er `.md`-filer. Det starter ikke ved push til andre branches
eller ved oprettelse af en pull request.

**Mangler at blive bekræftet:** En grøn GitHub Actions-kørsel, hvor trinnet
**Run Postman API tests** faktisk er kørt. Den lokale Postman-kørsel er ikke
bevis for, at database, API og CLI også starter korrekt hos GitHub.
Gem link eller screenshot fra den vellykkede CI-kørsel til dokumentationen.

## 6. Begrænsninger og aflevering

| Ikke bevist af disse API-tests | Relevant opfølgning |
|---|---|
| At bestemte værdier som 999, 29. februar og alle mobilpræfikser faktisk genereres | Målrettede unit-tests med styrbar tilfældighed |
| At programmet faktisk læser database og navnefil | Integrationstests med de rigtige komponenter |
| Statistisk tilfældighed eller fuld unikhed | BULK-14 er kun en variationskontrol |
| Adfærd ved manglende database eller navnefil | Særskilte fejlscenarier i et egnet testmiljø |

**Afleveringsfrist ifølge opgavebeskrivelsen: 9. november 2026 kl. 23.59.**
Black-box-testdesignet i fil 01 og 02 skal indgå i den PDF, opgaven kræver.
Markdown-filerne er arbejdsgrundlaget og erstatter ikke PDF-leverancen.
