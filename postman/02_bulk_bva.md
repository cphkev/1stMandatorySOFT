# Bulk: ækvivalensklasser og grænseværdier

## Kravet

> Return fake person information in bulk (all information for **2 to 100** persons)

Man må bestille fra 2 til 100 personer, begge ender inkluderet.

```
GET /api/persons?count=5
```

## Hvorfor to teknikker?

**Ækvivalensklasser.** Der findes uendeligt mange tal. Mange opfører sig ens, så vi deler dem i
grupper og vælger repræsentanter. 37 og 38 er begge "et tilladt antal", så vi behøver ikke teste begge.

**Grænseværdianalyse (BVA).** Fejl sidder oftest lige ved kanten. Hvis programmeren har skrevet `>` i
stedet for `>=`, bliver præcis 2 afvist, men 3 går igennem. Det finder man kun ved at teste tallet
**lige under, på og lige over** hver grænse.

## Ækvivalensklasser

| Gruppe | Eksempler | Forventet |
|---|---|---|
| Under 2 | 1, 0, -1 | 400 |
| 2 til 100 | 2, 3, 50, 99, 100 | 200, præcis det antal personer |
| Over 100 | 101 | 400 |
| Decimaltal | 2.5 | 400 |
| Ikke et tal | abc | 400 |
| Tom eller udeladt | `count=` / ingen `count` | Afklares (se fil 03) |

## Grænseværdier

Der er to grænser, nederste (2) og øverste (100).

```
  ugyldigt  |        gyldigt         |  ugyldigt
  0   1     2   3  ...  99   100     101  ...
```

| Grænse | Lige under | På | Lige over |
|---|---|---|---|
| Nedre: 2 | `1` afvises | `2` ok | `3` ok |
| Øvre: 100 | `99` ok | `100` ok | `101` afvises |

Det giver de seks tal **1, 2, 3, 99, 100, 101**.

## Fra analyse til tests

| Test | `count` | Forventet | Kommer fra |
|---|---|---|---|
| BULK-01 | 1 | 400 | Nedre grænse, under |
| BULK-02 | 2 | 200, 2 personer | Nedre grænse, på |
| BULK-03 | 3 | 200, 3 personer | Nedre grænse, over |
| BULK-04 | 99 | 200, 99 personer | Øvre grænse, under |
| BULK-05 | 100 | 200, 100 personer | Øvre grænse, på |
| BULK-06 | 101 | 400 | Øvre grænse, over |
| BULK-07 | 50 | 200, 50 personer | Repræsentant i gyldig gruppe |
| BULK-08 | 0 | 400 | Gruppe: under 2 |
| BULK-09 | -1 | 400 | Gruppe: under 2 |
| BULK-10 | abc | 400 | Gruppe: ikke et tal |
| BULK-11 | 2.5 | 400 | Gruppe: decimaltal. Må ikke bare afrundes |
| BULK-12 | tom | afklares | Erfaringsbaseret |
| BULK-13 | udeladt | afklares | Erfaringsbaseret |
| BULK-14 | 100 | 200 | Erfaringsbaseret: tilfældighed |

## Hvorfor ikke alle tal fra 2 til 100?

Det ville være 99 tests, der fortæller det samme. Hvis 2, 3, 50, 99 og 100 virker, er risikoen
lille for, at 37 ikke gør. Pointen er **få tests, der finder de fleste fejl**.

## Hvad tjekker vi i et gyldigt svar?

I BULK-02 til 07 kontrolleres:

1. Status 200.
2. Svaret er en liste.
3. Listen har **præcis** det bestilte antal personer.
4. Hver person har alle felter udfyldt.
5. Hver person har gyldigt CPR og køn, dato der matcher CPR, gyldig adresse og gyldigt mobilnummer.

## Hvad dækker vi ikke?

| Ikke dækket | Bemærkning |
|---|---|
| Meget store tal, fx 99999999999999999999 | Hører til gruppen "over 100", men kan afsløre overflow. Kan tilføjes som erfaringsbaseret test |
| `+2`, `1e2`, mellemrum, gentagne `count` | Ikke aftalt, hvad der skal ske |
