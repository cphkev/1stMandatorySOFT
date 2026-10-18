# Tests, begrundelse og fremgangsmåde

Denne fil svarer på tre spørgsmål:

1. Hvilke tests har vi lavet?
2. Hvorfor har vi lavet dem?
3. Hvordan kom vi frem til, at det var netop disse?

Bulk-testene (mange personer) er forklaret mere detaljeret i **02_bulk_bva.md**.
Resultater og fejl står i **03_resultater_og_fejl.md**.

---

## 1. Hvilke tests har vi lavet?

Vi har **22 requests** i Postman. Otte tester ét endpoint hver. Fjorten tester bulk (`/api/persons`).

### Enkelt-endpoints

| Test | Endpoint | Hvad tjekker den? | Hvorfor? |
|---|---|---|---|
| CPR-01 | `/api/cpr` | CPR og køn | Kernen i opgaven: gyldigt CPR med korrekt kønscifre |
| NAME-01 | `/api/name-gender` | Fornavn, efternavn, køn | Navn og køn skal altid komme sammen |
| NAME-02 | `/api/name-gender-dob` | Som NAME-01 plus fødselsdato | Datoen skal være en rigtig kalenderdato |
| CPR-02 | `/api/cpr-name-gender` | CPR, navne, køn | CPR og køn skal passe sammen, også når navnet er med |
| CPR-03 | `/api/cpr-name-gender-dob` | Som CPR-02 plus fødselsdato | CPR og fødselsdato skal beskrive samme dag |
| ADDRESS-01 | `/api/address` | Vej, nummer, etage, dør, postnummer, by | Adressen har flest regler og dermed flest steder, det kan gå galt |
| MOBILE-01 | `/api/mobile` | Mobilnummer | 8 cifre og et tilladt begyndelsestal |
| PERSON-01 | `/api/person` | Alle oplysninger for én person | Viser, at alle dele virker sammen i samme person |

Opgaven kræver ni funktioner, og hver er skrevet for sig. Derfor tester vi dem alle, også selv om de overlapper.

### Bulk

| Test | `count` | Forventet | Rolle |
|---|---|---|---|
| BULK-01 | 1 | 400 | Lige under nedre grænse |
| BULK-02 | 2 | 200, 2 personer | På nedre grænse |
| BULK-03 | 3 | 200, 3 personer | Lige over nedre grænse |
| BULK-04 | 99 | 200, 99 personer | Lige under øvre grænse |
| BULK-05 | 100 | 200, 100 personer | På øvre grænse |
| BULK-06 | 101 | 400 | Lige over øvre grænse |
| BULK-07 | 50 | 200, 50 personer | Typisk værdi midt i intervallet |
| BULK-08 | 0 | 400 | Nul |
| BULK-09 | -1 | 400 | Negativt tal |
| BULK-10 | `abc` | 400 | Tekst |
| BULK-11 | 2.5 | 400 | Decimaltal |
| BULK-12 | tom (`count=`) | afklares | Erfaringsbaseret case |
| BULK-13 | udeladt | afklares | Erfaringsbaseret case |
| BULK-14 | 100 | 200 | Tilfældighed: data må ikke være ens |

I de gyldige bulk-kald (BULK-02 til 07) tjekker vi **hver eneste person**, ikke kun den første.
En fejl kan gemme sig i person 73 af 100.

### Fælles tests (kører automatisk)

To typer tests ligger i collectionens fælles script og kører efter hvert svar:

- **Fælles: Svaret er JSON.** Alle svar skal være i det aftalte format.
- **Datakilder.** Navne og adresser skal findes i de udleverede filer. Se afsnit 4.

---

## 2. Hvordan kom vi frem til testene?

Vi har brugt fire trin. Hvert trin gav en gruppe tests.

### Trin 1: Læs opgaven og find reglerne

Facit kommer fra **opgavebeskrivelsen**, ikke fra det, generatoren tilfældigvis giver.
Eksempel: opgaven siger husnummer 1 til 999. Så er 1000 forkert, uanset hvad programmet gør.

Nogle ting er ikke afgjort af opgaven. Dem har vi valgt selv og skrevet ned som fortolkninger:

| Valg | Hvad vi har besluttet |
|---|---|
| Svarformat | JSON, felter på engelsk, værdier som tekst, dato som `ÅÅÅÅ-MM-DD` |
| Fejl | Ugyldigt `count` giver status 400 |
| Vejnavn | Kun bogstaver (også æ, ø, å) |
| Husnummer og dør | Ingen foranstillede nuller |

### Trin 2: Black-box på det, vi selv kan sende (bulk)

Eneste sted, vi vælger input, er `count`. Her bruger vi to teknikker:

- **Ækvivalensklasser:** del tallene i grupper, der opfører sig ens (for få, tilladt, for mange, ikke-tal).
- **Grænseværdianalyse:** test tallene lige under, på og lige over hver grænse (1, 2, 3 og 99, 100, 101).

Det giver **BULK-01 til BULK-11**. Detaljer i fil 02.

### Trin 3: Regler som kontroller på det, vi ikke kan styre

Alle andre endpoints har ingen input. Programmet vælger selv. Så vi kan ikke sende `husnummer=1000`.
I stedet laver vi hver regel fra opgaven om til en kontrol på svaret:

| Regel i opgaven | Kontrol på svaret |
|---|---|
| CPR er 10 cifre | Længde 10, kun tal |
| De første 6 cifre er fødselsdatoen | Mulig dato, og matcher `date_of_birth` |
| Kvinde: lige sidste ciffer. Mand: ulige | Sidste ciffer mod `gender` |
| Fødselsdato | Format `ÅÅÅÅ-MM-DD`, findes i kalenderen (inkl. skudår) |
| Vejnavn: alfabetiske tegn | Kun bogstaver |
| Husnummer 1 til 999, evt. ét stort bogstav | Tal inden for interval, højst ét A-Z |
| Etage: `st` eller 1 til 99 | `st` eller tal inden for interval |
| Dør: `th`, `mf`, `tv`, 1 til 50, eller bogstav + evt. bindestreg + 1 til 3 cifre | Én af tre formgrupper |
| Mobil: 8 cifre og tilladt begyndelse | Længde 8, og begynder på listen fra opgaven |
| Postnummer 4 cifre | Fire tal, og byen er udfyldt |

Det giver CPR-01, NAME-01 og 02, CPR-02 og 03, ADDRESS-01, MOBILE-01 og PERSON-01.

Vi brugte også en **beslutningstabel** for CPR og køn:

| Køn | Sidste ciffer | Gyldigt? |
|---|---|---|
| female | 0, 2, 4, 6, 8 | Ja |
| female | 1, 3, 5, 7, 9 | Nej |
| male | 1, 3, 5, 7, 9 | Ja |
| male | 0, 2, 4, 6, 8 | Nej |

Ugyldige eksempler (fx et CPR med 9 cifre) kan vi ikke sende ind. De er det, kontrollerne skal opdage,
hvis programmet fejler.

### Trin 4: Erfaringsbaserede tests

Her bruger vi erfaring med, hvad der plejer at gå galt:

- **BULK-12 og 13:** hvad sker der med et tomt eller manglende `count`? Opgaven siger ikke noget.
- **BULK-14:** kan generatoren være gået i stå og give den samme person igen og igen?
- **Datakilder:** kommer værdierne fra de rigtige filer og ikke bare fra en tilfældig tekst?

---

## 3. De små kontroller i detaljer

| Kontrol | Regel | Fejltype, den fanger |
|---|---|---|
| Status 200 | Gyldigt kald giver 200 | Crash (500), forkert adresse (404) |
| JSON-objekt | Svar er JSON, ét objekt | Forkert format, liste i stedet for objekt |
| Felter udfyldt | Alle felter findes, er tekst og ikke tomme | Manglende, tomme eller forkert typede felter |
| CPR 10 cifre | Længde 10, kun tal | For kort/lang, bogstaver |
| CPR mulig dato | Dag 1 til 31 inden for månedens længde, måned 1 til 12 | Dag 32, måned 13, 31. april |
| CPR og køn | Lige/ulige sidste ciffer | Kvinde med ulige tal |
| Fødselsdato | `ÅÅÅÅ-MM-DD`, findes i kalenderen | `2001-02-29`, forkert format |
| CPR matcher dato | `ddMMåå` er det samme som `date_of_birth` | Forskellige datoer i samme person |
| Vejnavn | Kun bogstaver | Cifre eller tegn |
| Husnummer | 1 til 999, evt. ét stort bogstav | `0`, `1000`, `43b`, `43BB` |
| Etage | `st` eller 1 til 99 | `0`, `100`, andre ord |
| Dør | Tre tilladte typer | `51`, `A3`, `a1234` |
| Foranstillet nul | Husnummer og tal-dør starter ikke med 0 | `007`, `05` |
| Postnummer | 4 cifre, by udfyldt | Manglende/forkert postnummer |
| Mobil 8 cifre | Længde 8, kun tal | For kort/lang |
| Mobil begyndelse | Står på opgavens liste | Fx `10…` eller `99…` |

---

## 4. Datakilder: hvorfor og hvordan

### Hvorfor?

Opgaven siger, at navne kommer fra `person-names.json`, og postnummer og by fra `addresses.sql`.
Kontrollerne i afsnit 3 kan kun se, om en adresse *ser rigtig ud*. `1234 Fantasibyen` har fire cifre, men
findes ikke. Derfor slår vi hvert svar op i en facitliste.

### De tre filer

| Fil | Hvad er det? | Hvor bruges den? |
|---|---|---|
| `FakeInfo-reference.postman_environment.json` | Facitliste: 536 navnerækker og 589 postnummer/by-par samt `baseUrl` | Postman **Environment** |
| `data-source-tests.js` | Script, der slår hvert svar op i facitlisten | Collectionens **Scripts → Post-response** |
| `README.md` | Vejledning og SHA-256 for kildefilerne | Kun læsning |

Facitlisten er trukket ud af de udleverede kildefiler. Den er et **fast snapshot**, ikke data fra
programmets egne svar. Ellers ville testen kun bekræfte, at programmet er enig med sig selv.

### Hvilke tests laver scriptet?

| Test | Tjekker | Endpoints |
|---|---|---|
| Fornavn og køn fra samme række | Parret findes sammen i navnefilen | name-gender, name-gender-dob, cpr-name-gender, cpr-name-gender-dob, person, persons |
| Efternavn fra navnefilen | Efternavnet findes i filen | Samme som ovenfor |
| Postnummer og by fra samme par | Parret findes sammen i databasen | address, person, persons |

Scriptet springer over, hvis svaret ikke er status 200. Fejlsvar indeholder ingen personer.

### Sådan bruges det

1. Importér environment-filen i Postman.
2. Vælg miljøet **FakeInfo - Local med testdata**.
3. Tjek, at `baseUrl` passer (nu `http://localhost:8080`).
4. Scriptet ligger i collectionens Post-response.
5. Kør requests eller Collection Run.

Hvis miljøet ikke er valgt, fejler testen *"Datakilder: Reference-data og svar kan læses"*
med beskeden "Vælg miljøet med expectedNames". Det er så en opsætningsfejl, ikke en programfejl.

I CI med Newman skal environment-filen med:

```
newman run collection.json -e FakeInfo-reference.postman_environment.json
```

(Kommandoen er et forslag og er ikke kørt endnu. Tjek filnavnet.)
