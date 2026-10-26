package com.cphkev.fakeinfo;

import com.cphkev.fakeinfo.model.PersonName;
import com.cphkev.fakeinfo.model.PostalCode;
import com.cphkev.fakeinfo.repository.PostalCodeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public final class FakeDanishPersonService {
    private static final DateTimeFormatter CPR_DATE = DateTimeFormatter.ofPattern("ddMMyy", Locale.ROOT);
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private final List<PersonName> personNames;
    private final PostalCodeRepository postalCodeRepository;

    private final Random random = new Random();
    private final List<String> phonePrefixes;

    public FakeDanishPersonService(List<PersonName> personNames, PostalCodeRepository postalCodeRepository) {
        this.personNames = personNames;
        this.postalCodeRepository = postalCodeRepository;

        this.phonePrefixes = buildPhonePrefixes();
    }

    public static List<String> buildPhonePrefixes() {
        List<String> prefixes = new ArrayList<>(List.of("2", "30", "31", "40", "41", "42", "50", "51", "52", "53", "60", "61", "71", "81", "91", "92", "93", "342", "359", "362", "389", "398", "431", "441", "462", "466", "468", "472", "474", "476", "478", "545", "556", "577", "579", "584", "589", "627", "629", "641", "649", "658", "667", "697", "829"));

        addRange(prefixes, 344, 349);
        addRange(prefixes, 356, 357);
        addRange(prefixes, 365, 366);
        addRange(prefixes, 485, 486);
        addRange(prefixes, 488, 489);
        addRange(prefixes, 493, 496);
        addRange(prefixes, 498, 499);
        addRange(prefixes, 542, 543);
        addRange(prefixes, 551, 552);
        addRange(prefixes, 571, 574);
        addRange(prefixes, 586, 587);
        addRange(prefixes, 597, 598);
        addRange(prefixes, 662, 665);
        addRange(prefixes, 692, 694);
        addRange(prefixes, 771, 772);
        addRange(prefixes, 782, 783);
        addRange(prefixes, 785, 786);
        addRange(prefixes, 788, 789);
        addRange(prefixes, 826, 827);
        return prefixes;
    }

    private static void addRange(List<String> prefixes, int start, int end) {
        for (int value = start; value <= end; value++) {
            prefixes.add(Integer.toString(value));
        }
    }

    public Map<String, String> fakeNameGender() {
        PersonName person = personNames.get(random.nextInt(personNames.size()));

        return Map.of(
                "first_name", person.name(),
                "last_name", person.surname(),
                "gender", person.gender()
        );
    }

    public Map<String, String> fakeCpr() {
        String gender = random.nextBoolean() ? "female" : "male";
        LocalDate dob = randomBirthDate();
        return Map.of("cpr", cprFor(gender, dob), "gender", gender);
    }

    public Map<String, String> fakeNameGenderDob() {
        Map<String, String> person = new HashMap<>(fakeNameGender());
        person.put("date_of_birth", randomBirthDate().format(ISO_DATE));
        return person;
    }

    public Map<String, String> fakeCprNameGender() {
        Map<String, String> person = new HashMap<>(fakeNameGender());
        person.put("cpr", cprFor(person.get("gender"), randomBirthDate()));
        return person;
    }

    public Map<String, String> fakeCprNameGenderDob() {
        Map<String, String> person = new HashMap<>(fakeNameGender());
        LocalDate dob = randomBirthDate();
        person.put("date_of_birth", dob.format(ISO_DATE));
        person.put("cpr", cprFor(person.get("gender"), dob));
        return person;
    }

    public Map<String, String> fakeAddress() {
        Optional<PostalCode> postalCode = postalCodeRepository.findRandomPostalCode();

        if (postalCode.isEmpty()) {
            throw new IllegalStateException("No postal code found in the system");
        }

        String street = randomLetters(5 + random.nextInt(8));

        String number = Integer.toString(1 + random.nextInt(999));
        if (random.nextDouble() < 0.3) {
            number += (char) ('A' + random.nextInt(26));
        }

        String floor = random.nextDouble() < 0.2 ? "st" : Integer.toString(1 + random.nextInt(99));

        String door;
        int mode = random.nextInt(3);
        if (mode == 0) {
            String[] named = {"th", "mf", "tv"};
            door = named[random.nextInt(named.length)];
        } else if (mode == 1) {
            door = Integer.toString(1 + random.nextInt(50));
        } else {
            String letter = Character.toString((char) ('a' + random.nextInt(26)));
            String separator = random.nextBoolean() ? "-" : "";
            String digits = Integer.toString(1 + random.nextInt(999));
            door = letter + separator + digits;
        }

        Map<String, String> result = new HashMap<>();
        result.put("street", street);
        result.put("number", number);
        result.put("floor", floor);
        result.put("door", door);
        result.put("postal_code", postalCode.get().getPostalCode());
        result.put("town", postalCode.get().getTownName());
        return result;
    }

    public Map<String, String> fakeMobilePhoneNumber() {
        String prefix = phonePrefixes.get(random.nextInt(phonePrefixes.size()));
        StringBuilder number = new StringBuilder(prefix);
        while (number.length() < 8) {
            number.append(random.nextInt(10));
        }
        return Map.of("mobile_phone_number", number.toString());
    }

    public Map<String, Object> fakePerson() {
        Map<String, String> base = fakeCprNameGenderDob();
        Map<String, Object> result = new HashMap<>(base);
        result.put("address", fakeAddress());
        result.putAll(fakeMobilePhoneNumber());
        return result;
    }

    public List<Map<String, Object>> fakePersonsBulk(int count) {
        if (count < 2 || count > 100) {
            throw new IllegalArgumentException("count must be between 2 and 100");
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            result.add(fakePerson());
        }
        return result;
    }

    private LocalDate randomBirthDate() {
        LocalDate start = LocalDate.of(1940, 1, 1);
        LocalDate end = LocalDate.of(2025, 12, 31);
        long range = end.toEpochDay() - start.toEpochDay();
        return start.plusDays(random.nextInt((int) range + 1));
    }

    private String cprFor(String gender, LocalDate dob) {
        String ddmmyy = dob.format(CPR_DATE);
        String middle = String.format(Locale.ROOT, "%03d", random.nextInt(1000));
        int[] parity = "female".equals(gender) ? new int[]{0, 2, 4, 6, 8} : new int[]{1, 3, 5, 7, 9};
        int last = parity[random.nextInt(parity.length)];
        return ddmmyy + middle + last;
    }

    private String randomLetters(int length) {
        StringBuilder output = new StringBuilder(length);
        output.append((char) ('A' + random.nextInt(26)));
        for (int index = 1; index < length; index++) {
            output.append((char) ('a' + random.nextInt(26)));
        }
        return output.toString();
    }
}
