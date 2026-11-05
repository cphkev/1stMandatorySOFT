# Reference-data til Postman

I Postman Local View vælges miljøet **FakeInfo - Local med testdata** fra
`postman/environments/FakeInfo - Local med testdata.environment.yaml`.
Det er også dette miljø, CI bruger. Ret `baseUrl`, hvis serveren bruger en anden
adresse end `http://localhost:8080`.

`FakeInfo-reference.postman_environment.json` er en alternativ importfil med
reference-data. En JSON-eksport af collectionen er ikke nødvendig til den nuværende CI.

Miljøet indeholder 536 navnerækker og 589 postnummer/by-par fra de udleverede
datakilder. Det er et fast snapshot, ikke data fra API-svar.
`data-source-tests.js` er referencekopien af datakilde-scriptet, som allerede
ligger i collectionens fælles Post-response-script. Tilføj det ikke en gang til.
Scriptet kontrollerer relevante svar med status 200, inklusive hver person i bulk
og standardværdi-casene BULK-12 og BULK-13. Fejlsvar springes over.

Kontrollerne omfatter navn/køn-par, efternavnets medlemskab og postnummer/by-par.
Om efternavnet skal komme fra samme række som fornavnet er fortsat en åben
fortolkning i testplanen. Scriptet kræver ikke samme række for efternavnet.

Disse API-tests viser, at svarenes værdier findes i reference-data. De beviser
ikke, at applikationen faktisk foretager databaseopslag; det undersøges i
integrationstests.

Kilder og SHA-256 ved udtræk:

- `src/main/resources/person-names.json`: `0019aebda233d0d6990144301781074ab9fa81532eb8d49d65021c01ef9d0da2`
- `src/main/resources/addresses.sql`: `ae6c8e54d0a3709bae7672119fa52c2cf6b8ba53882e356aab80fcdcb6e38fee`

Opdatér kun reference-data, når ændringer i de udleverede datakilder er
bekræftet. Generér aldrig facit fra applikationens egne svar.
