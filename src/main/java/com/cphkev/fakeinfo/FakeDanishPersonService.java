package com.cphkev.fakeinfo;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public final class FakeDanishPersonService {
    private static final DateTimeFormatter CPR_DATE = DateTimeFormatter.ofPattern("ddMMyy", Locale.ROOT);
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private final Random random = new Random();
    private final List<NameRecord> firstNames;
    private final List<String> lastNames;
    private final List<PostalRecord> postals;
    private final List<String> phonePrefixes;

    public FakeDanishPersonService() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new ClassPathResource("person-names.json").getInputStream());

        this.firstNames = new ArrayList<>();
        this.lastNames = new ArrayList<>();

        JsonNode persons = root.path("persons");
        if (persons.isArray()) {
            for (JsonNode node : persons) {
                JsonNode name = node.get("name");
                JsonNode surname = node.get("surname");
                JsonNode gender = node.get("gender");
                if (name != null && surname != null && gender != null) {
                    this.firstNames.add(new NameRecord(name.asString(), gender.asString()));
                    if (!this.lastNames.contains(surname.asString())) {
                        this.lastNames.add(surname.asString());
                    }
                }
            }
        } else {
            JsonNode firstNameNodes = root.path("first_names");
            if (firstNameNodes.isArray()) {
                for (JsonNode node : firstNameNodes) {
                    this.firstNames.add(new NameRecord(node.path("name").asString(), node.path("gender").asString()));
                }
            }

            JsonNode lastNameNodes = root.path("last_names");
            if (lastNameNodes.isArray()) {
                for (JsonNode node : lastNameNodes) {
                    this.lastNames.add(node.asString());
                }
            }
        }

        if (this.firstNames.isEmpty() || this.lastNames.isEmpty()) {
            throw new IllegalStateException("person-names.json must contain usable person names");
        }

        this.postals = loadPostals();
        this.phonePrefixes = buildPhonePrefixes();
    }

    private List<PostalRecord> loadPostals() throws IOException {
        Pattern pattern = Pattern.compile("\\('([^']+)'\\s*,\\s*'([^']+)'\\)");
        List<PostalRecord> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("addresses.sql").getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) {
                    records.add(new PostalRecord(matcher.group(1), matcher.group(2)));
                }
            }
        }
        if (records.isEmpty()) {
            throw new IllegalStateException("No postal data found in addresses.sql");
        }
        return records;
    }

    public static List<String> buildPhonePrefixes() {
        List<String> prefixes = new ArrayList<>(List.of(
                "2", "30", "31", "40", "41", "42", "50", "51", "52", "53", "60", "61", "71", "81", "91", "92", "93",
                "342", "359", "362", "389", "398", "431", "441", "462", "466", "468", "472", "474", "476", "478",
                "545", "556", "577", "579", "584", "627", "629", "641", "649", "658", "667", "697", "829"
        ));
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
        NameRecord first = firstNames.get(random.nextInt(firstNames.size()));
        Map<String, String> result = new HashMap<>();
        result.put("first_name", first.name());
        result.put("last_name", lastNames.get(random.nextInt(lastNames.size())));
        result.put("gender", first.gender());
        return result;
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
        PostalRecord postal = postals.get(random.nextInt(postals.size()));
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
        result.put("postal_code", postal.postalCode());
        result.put("town", postal.town());
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
        Map<String, Object> result = new HashMap<>();
        result.putAll(base);
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

    private record NameRecord(String name, String gender) {
    }

    private record PostalRecord(String postalCode, String town) {
    }
}
