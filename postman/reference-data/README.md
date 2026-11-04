# Reference-data til Postman

Importér `FakeInfo-reference.postman_environment.json` og vælg miljøet
**FakeInfo - Local med testdata**. Ret `baseUrl`, hvis serveren bruger en anden adresse.

Miljøet indeholder 536 navnerækker og 589 postnummer/by-par
fra projektets udleverede datakilder. Det er et fast snapshot, ikke data fra API-svar.
Tilføj `data-source-tests.js` nederst i collectionens Post-response-script.
Behold eksisterende fælles tests. Scriptet kontrollerer alle relevante gyldige
svar, inklusive hver person i bulk. Negative bulk-kald springes over.

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
