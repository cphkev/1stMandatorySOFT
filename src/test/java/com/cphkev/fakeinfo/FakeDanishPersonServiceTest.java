package com.cphkev.fakeinfo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FakeDanishPersonServiceTest {

    @Test // tester at cpr har præcis 10 cifre og kun består af tal
    void generatedCprContainsExactlyTenDigits() throws IOException {
        FakeDanishPersonService service = new FakeDanishPersonService();

        String cpr = service.fakeCpr().get("cpr");

        assertThat(cpr).hasSize(10).matches("\\d{10}");
    }

    @Test // tester at cpr matcher fødselsdatoen
    void generatedCprMatchesDateOfBirth() throws IOException {
        FakeDanishPersonService service = new FakeDanishPersonService();

        Map<String, String> person = service.fakeCprNameGenderDob();
        String cpr = person.get("cpr");
        LocalDate dateOfBirth = LocalDate.parse(person.get("date_of_birth"));

        String cprDate = cpr.substring(0, 6);
        String expectedDate = dateOfBirth.format(DateTimeFormatter.ofPattern("ddMMyy"));

        assertThat(cprDate).isEqualTo(expectedDate);
    }

    @Test // tester at cpr's sidste nummer matcher kønnet
    void cprLastDigitMatchesGender() throws IOException {
        FakeDanishPersonService service = new FakeDanishPersonService();

        for (int index = 0; index < 100; index++) {
            Map<String, String> person = service.fakeCprNameGenderDob();
            String cpr = person.get("cpr");
            String gender = person.get("gender");
            int lastDigit = Character.getNumericValue(cpr.charAt(9));

            if ("female".equals(gender)) {
                assertThat(lastDigit % 2).isEqualTo(0);
            } else {
                assertThat(gender).isEqualTo("male");
                assertThat(lastDigit % 2).isEqualTo(1);
            }
        }
    }

    @Test // tester at mobilnummer har præcis 8 cifre og starter med et af de tilladte præfikser
    void generatedMobileNumberHasEightDigitsAndAllowedPrefix() throws IOException {
        FakeDanishPersonService service = new FakeDanishPersonService();
        List<String> allowedPrefixes = FakeDanishPersonService.buildPhonePrefixes();

        for (int index = 0; index < 100; index++) {
            String mobileNumber = service.fakeMobilePhoneNumber().get("mobile_phone_number");

            assertThat(mobileNumber).matches("\\d{8}");
            assertThat(allowedPrefixes.stream().anyMatch(mobileNumber::startsWith)).isTrue();
        }
    }

    @Test // tester at adressefelterne har gyldige formater
    void generatedAddressFieldsHaveValidFormats() throws IOException {
        FakeDanishPersonService service = new FakeDanishPersonService();
        List<String> namedDoors = List.of("th", "mf", "tv");

        for (int index = 0; index < 100; index++) {
            Map<String, String> address = service.fakeAddress();
            String number = address.get("number");
            String floor = address.get("floor");
            String door = address.get("door");

            assertThat(address.get("street")).matches("[A-Za-z]{5,12}");
            assertThat(number).matches("\\d{1,3}[A-Z]?");
            // tjekker at nummeret er mellem 1 og 999 ved at fjerne bogstaver først
            assertThat(Integer.parseInt(number.replaceAll("[A-Z]", ""))).isBetween(1, 999);
            if ("st".equals(floor)) {
                assertThat(floor).isEqualTo("st");
            } else {
                assertThat(floor).matches("\\d{1,2}");
                assertThat(Integer.parseInt(floor)).isBetween(1, 99);
            }
            if (namedDoors.contains(door)) {
                assertThat(door).isIn("th", "mf", "tv");
            } else if (door.matches("\\d{1,2}")) {
                assertThat(Integer.parseInt(door)).isBetween(1, 50);
            } else {
                assertThat(door).matches("[a-z]-?\\d{1,3}");
                String digits = door.replaceAll("[a-z-]", "");
                assertThat(Integer.parseInt(digits)).isBetween(1, 999);
            }
            assertThat(address.get("postal_code")).isNotBlank();
            assertThat(address.get("town")).isNotBlank();
        }
    }

    @Test // tester at bulk-generering accepterer counts mellem 2 og 100
    void bulkGenerationAcceptsCountsFromTwoToOneHundred() throws IOException {
        FakeDanishPersonService service = new FakeDanishPersonService();

        assertThat(service.fakePersonsBulk(2)).hasSize(2);
        assertThat(service.fakePersonsBulk(100)).hasSize(100);
    }

    @Test // tester at bulk-generering afviser counts uden 2-100 intervallet
    void bulkGenerationRejectsCountsOutsideAllowedRange() throws IOException {
        FakeDanishPersonService service = new FakeDanishPersonService();

        assertThatThrownBy(() -> service.fakePersonsBulk(1))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.fakePersonsBulk(101))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test // tester at genererede navne og køn har gyldige værdier/ikke er tomme
    void generatedNameGenderContainsValidValues() throws IOException {
        FakeDanishPersonService service = new FakeDanishPersonService();

        for (int index = 0; index < 100; index++) {
            Map<String, String> person = service.fakeNameGender();

            assertThat(person.get("first_name")).isNotBlank();
            assertThat(person.get("last_name")).isNotBlank();
            assertThat(person.get("gender")).isIn("female", "male");
        }
    }
/* 
    @Test // tester at navne hentes fra person-names.json
    void generatedNamesComeFromNamesFile() throws IOException {
        FakeDanishPersonService service = new FakeDanishPersonService();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(
            new ClassPathResource("person-names.json").getInputStream()
        );
        Set<String> validFirstNames = new HashSet<>();
        Set<String> validLastNames = new HashSet<>();
        for (JsonNode person : root.path("persons")) {
            validFirstNames.add(person.path("name").asText());
            validLastNames.add(person.path("surname").asText());
        }

        for (int index = 0; index < 100; index++) {
            Map<String, String> person = service.fakeNameGender();

            assertThat(validFirstNames).contains(person.get("first_name"));
            assertThat(validLastNames).contains(person.get("last_name"));
        }
    }
*/
    @Test // tester at name, gender og date_of_birth returneres korrekt
    void generatedNameGenderDobContainsValidInformation() throws IOException {
        FakeDanishPersonService service = new FakeDanishPersonService();

        Map<String, String> person = service.fakeNameGenderDob();

        assertThat(person).containsKeys(
            "first_name",
            "last_name",
            "gender",
            "date_of_birth"
        );
        assertThat(person.get("first_name")).isNotBlank();
        assertThat(person.get("last_name")).isNotBlank();
        assertThat(person.get("gender")).isIn("female", "male");
        assertThat(LocalDate.parse(person.get("date_of_birth"))).isNotNull();
    }

    @Test // tester at CPR, navn og køn returneres korrekt
    void generatedCprNameGenderContainsValidInformation() throws IOException {
        FakeDanishPersonService service = new FakeDanishPersonService();

        Map<String, String> person = service.fakeCprNameGender();
        String cpr = person.get("cpr");
        String gender = person.get("gender");
        int lastDigit = Character.getNumericValue(cpr.charAt(9));

        assertThat(person).containsKeys("cpr", "first_name", "last_name", "gender");
        assertThat(cpr).matches("\\d{10}");
        assertThat(person.get("first_name")).isNotBlank();
        assertThat(person.get("last_name")).isNotBlank();
        assertThat(gender).isIn("female", "male");
        if ("female".equals(gender)) {
            assertThat(lastDigit % 2).isEqualTo(0);
        } else {
            assertThat(lastDigit % 2).isEqualTo(1);
        }
    }

    @Test // tester at CPR, navn, køn og fødselsdato hænger sammen
    void generatedCprNameGenderDobContainsValidInformation() throws IOException {
        FakeDanishPersonService service = new FakeDanishPersonService();

        Map<String, String> person = service.fakeCprNameGenderDob();
        String cpr = person.get("cpr");
        String gender = person.get("gender");
        LocalDate dateOfBirth = LocalDate.parse(person.get("date_of_birth"));
        int lastDigit = Character.getNumericValue(cpr.charAt(9));

        assertThat(person).containsKeys(
            "cpr",
            "first_name",
            "last_name",
            "gender",
            "date_of_birth"
        );
        assertThat(cpr).matches("\\d{10}");
        assertThat(person.get("first_name")).isNotBlank();
        assertThat(person.get("last_name")).isNotBlank();
        assertThat(gender).isIn("female", "male");
        assertThat(cpr.substring(0, 6))
            .isEqualTo(dateOfBirth.format(DateTimeFormatter.ofPattern("ddMMyy")));
        if ("female".equals(gender)) {
            assertThat(lastDigit % 2).isEqualTo(0);
        } else {
            assertThat(lastDigit % 2).isEqualTo(1);
        }
    }

    @Test // tester at genererede personer indeholder alle nødvendige informationer
    void generatedPersonContainsAllRequiredInformation() throws IOException {
        FakeDanishPersonService service = new FakeDanishPersonService();

        Map<String, Object> person = service.fakePerson();

        assertThat(person).containsKeys(
            "cpr",
            "first_name",
            "last_name",
            "gender",
            "date_of_birth",
            "address",
            "mobile_phone_number"
        );
        assertThat(person.get("address")).isInstanceOf(Map.class);
    }
}
