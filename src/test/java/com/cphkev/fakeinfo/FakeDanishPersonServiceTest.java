package com.cphkev.fakeinfo;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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
}
