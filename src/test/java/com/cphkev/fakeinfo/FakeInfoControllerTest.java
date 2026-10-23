package com.cphkev.fakeinfo;

import com.cphkev.fakeinfo.model.PostalCode;
import com.cphkev.fakeinfo.repository.PostalCodeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Import(MySqlTestConfiguration.class)
class FakeInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostalCodeRepository postalCodeRepository;

    private List<PostalCode> postalCodes;

    @BeforeEach
    void setUp() {
        postalCodes = postalCodeRepository.saveAll(Populator.createPostalCodes());
    }

    @AfterEach
    void tearDown() {
        postalCodeRepository.deleteAll();
    }

    @Test
    void testThatGetCprReturnsValidCpr() throws Exception {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
                .appendPattern("ddMM")
                .appendValueReduced(ChronoField.YEAR, 2, 2, 1940)
                .toFormatter()
                .withResolverStyle(ResolverStyle.STRICT);

        String response = mockMvc.perform(get("/api/cpr"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        String gender = json.get("gender").asString();
        String cpr = json.get("cpr").asString();

        String dateOfBirth = cpr.substring(0, 6);
        LocalDate dob = LocalDate.parse(dateOfBirth, dateFormatter);

        assertThat(gender).isIn("male", "female");

        assertThat(cpr).matches("\\d{10}");
        assertThat(dob.format(dateFormatter)).isEqualTo(dateOfBirth);

        int lastDigit = Character.getNumericValue(cpr.charAt(9));

        if ("female".equals(gender)) {
            assertThat(lastDigit % 2).isEqualTo(0);
        } else {
            assertThat(lastDigit % 2).isEqualTo(1);
        }
    }

    @Test
    void testThatGetNameGenderReturnsValidNameAndGender() throws Exception {
        String response = mockMvc.perform(get("/api/name-gender"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        String firstName = json.get("first_name").asString();
        String lastName = json.get("last_name").asString();
        String gender = json.get("gender").asString();

        assertThat(firstName).isNotBlank();
        assertThat(lastName).isNotBlank();
        assertThat(gender).isIn("male", "female");
    }

    @Test
    void testThatGetNameGenderDobReturnsValidNameGenderAndDateOfBirth() throws Exception {
        DateTimeFormatter dateFormatter = DateTimeFormatter
                .ofPattern("uuuu-MM-dd")
                .withResolverStyle(ResolverStyle.STRICT);

        String response = mockMvc.perform(get("/api/name-gender-dob"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        String firstName = json.get("first_name").asString();
        String lastName = json.get("last_name").asString();
        String gender = json.get("gender").asString();
        String dateOfBirth = json.get("date_of_birth").asString();

        LocalDate dob = LocalDate.parse(dateOfBirth, dateFormatter);

        assertThat(firstName).isNotBlank();
        assertThat(lastName).isNotBlank();
        assertThat(gender).isIn("male", "female");

        assertThat(dob.format(dateFormatter)).isEqualTo(dateOfBirth);
    }

    @Test
    void testThatGetCprNameGenderReturnsValidCprNameAndGender() throws Exception {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
                .appendPattern("ddMM")
                .appendValueReduced(ChronoField.YEAR, 2, 2, 1940)
                .toFormatter()
                .withResolverStyle(ResolverStyle.STRICT);

        String response = mockMvc.perform(get("/api/cpr-name-gender"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        String firstName = json.get("first_name").asString();
        String lastName = json.get("last_name").asString();
        String gender = json.get("gender").asString();
        String cpr = json.get("cpr").asString();

        String dateOfBirth = cpr.substring(0, 6);
        LocalDate dob = LocalDate.parse(dateOfBirth, dateFormatter);

        assertThat(firstName).isNotBlank();
        assertThat(lastName).isNotBlank();
        assertThat(gender).isIn("male", "female");

        assertThat(cpr).matches("\\d{10}");
        assertThat(dob.format(dateFormatter)).isEqualTo(dateOfBirth);

        int lastDigit = Character.getNumericValue(cpr.charAt(9));

        if ("female".equals(gender)) {
            assertThat(lastDigit % 2).isEqualTo(0);
        } else {
            assertThat(lastDigit % 2).isEqualTo(1);
        }
    }

    @Test
    void testThatGetCprNameGenderDobReturnsValidCprNameGenderAndDateOfBirth() throws Exception {
        DateTimeFormatter cprDateFormatter = new DateTimeFormatterBuilder()
                .appendPattern("ddMM")
                .appendValueReduced(ChronoField.YEAR, 2, 2, 1940)
                .toFormatter()
                .withResolverStyle(ResolverStyle.STRICT);

        DateTimeFormatter dateFormatter = DateTimeFormatter
                .ofPattern("uuuu-MM-dd")
                .withResolverStyle(ResolverStyle.STRICT);

        String response = mockMvc.perform(get("/api/cpr-name-gender-dob"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        String firstName = json.get("first_name").asString();
        String lastName = json.get("last_name").asString();
        String gender = json.get("gender").asString();
        String cpr = json.get("cpr").asString();
        String dateOfBirth = json.get("date_of_birth").asString();

        LocalDate cprDob = LocalDate.parse(cpr.substring(0, 6), cprDateFormatter);
        LocalDate dob = LocalDate.parse(dateOfBirth, dateFormatter);

        assertThat(firstName).isNotBlank();
        assertThat(lastName).isNotBlank();
        assertThat(gender).isIn("male", "female");

        assertThat(cpr).matches("\\d{10}");
        assertThat(dob.format(dateFormatter)).isEqualTo(dateOfBirth);
        assertThat(cprDob).isEqualTo(dob);

        int lastDigit = Character.getNumericValue(cpr.charAt(9));

        if ("female".equals(gender)) {
            assertThat(lastDigit % 2).isEqualTo(0);
        } else {
            assertThat(lastDigit % 2).isEqualTo(1);
        }
    }

    @Test
    void testThatGetAddressReturnsValidAddress() throws Exception {
        String response = mockMvc.perform(get("/api/address"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        String street = json.get("street").asString();
        String number = json.get("number").asString();
        String floor = json.get("floor").asString();
        String door = json.get("door").asString();
        String postalCode = json.get("postal_code").asString();
        String town = json.get("town").asString();

        assertThat(street).matches("[A-Za-z]+");
        assertThat(number).matches("[1-9]\\d{0,2}[A-Z]?");
        assertThat(floor).matches("st|([1-9]|[1-9]\\d)");
        assertThat(door).matches("th|mf|tv|([1-9]|[1-4]\\d|50)|[a-z]-?\\d{1,3}");

        assertThat(postalCodes)
                .anyMatch(postal ->
                        postal.getPostalCode().equals(postalCode)
                                && postal.getTownName().equals(town)
                );
    }

    @Test
    void testThatGetMobileReturnsValidMobilePhoneNumber() throws Exception {
        String response = mockMvc.perform(get("/api/mobile"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        String mobile = json.get("mobile_phone_number").asString();

        assertThat(mobile).matches("\\d{8}");
        assertThat(FakeDanishPersonService.buildPhonePrefixes())
                .anyMatch(mobile::startsWith);
    }

    @Test
    void testThatGetPersonReturnsValidPerson() throws Exception {
        DateTimeFormatter cprDateFormatter = new DateTimeFormatterBuilder()
                .appendPattern("ddMM")
                .appendValueReduced(ChronoField.YEAR, 2, 2, 1940)
                .toFormatter()
                .withResolverStyle(ResolverStyle.STRICT);

        DateTimeFormatter dateFormatter = DateTimeFormatter
                .ofPattern("uuuu-MM-dd")
                .withResolverStyle(ResolverStyle.STRICT);

        String response = mockMvc.perform(get("/api/person"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        String firstName = json.get("first_name").asString();
        String lastName = json.get("last_name").asString();
        String gender = json.get("gender").asString();
        String cpr = json.get("cpr").asString();
        String dateOfBirth = json.get("date_of_birth").asString();
        String mobile = json.get("mobile_phone_number").asString();

        JsonNode address = json.get("address");

        String street = address.get("street").asString();
        String number = address.get("number").asString();
        String floor = address.get("floor").asString();
        String door = address.get("door").asString();
        String postalCode = address.get("postal_code").asString();
        String town = address.get("town").asString();

        LocalDate cprDob = LocalDate.parse(cpr.substring(0, 6), cprDateFormatter);
        LocalDate dob = LocalDate.parse(dateOfBirth, dateFormatter);

        assertThat(firstName).isNotBlank();
        assertThat(lastName).isNotBlank();
        assertThat(gender).isIn("male", "female");

        assertThat(cpr).matches("\\d{10}");
        assertThat(dob.format(dateFormatter)).isEqualTo(dateOfBirth);
        assertThat(cprDob).isEqualTo(dob);

        int lastDigit = Character.getNumericValue(cpr.charAt(9));

        if ("female".equals(gender)) {
            assertThat(lastDigit % 2).isEqualTo(0);
        } else {
            assertThat(lastDigit % 2).isEqualTo(1);
        }

        assertThat(mobile).matches("\\d{8}");
        assertThat(FakeDanishPersonService.buildPhonePrefixes())
                .anyMatch(mobile::startsWith);

        assertThat(street).matches("[A-Za-z]+");
        assertThat(number).matches("[1-9]\\d{0,2}[A-Z]?");
        assertThat(floor).matches("st|([1-9]|[1-9]\\d)");
        assertThat(door).matches("th|mf|tv|([1-9]|[1-4]\\d|50)|[a-z]-?\\d{1,3}");

        assertThat(postalCodes)
                .anyMatch(postal ->
                        postal.getPostalCode().equals(postalCode)
                                && postal.getTownName().equals(town)
                );
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 52, 99, 100})
    void testThatGetPersonsReturnsRequestedNumberOfValidPersons(int count) throws Exception {
        DateTimeFormatter cprDateFormatter = new DateTimeFormatterBuilder()
                .appendPattern("ddMM")
                .appendValueReduced(ChronoField.YEAR, 2, 2, 1940)
                .toFormatter()
                .withResolverStyle(ResolverStyle.STRICT);

        DateTimeFormatter dateFormatter = DateTimeFormatter
                .ofPattern("uuuu-MM-dd")
                .withResolverStyle(ResolverStyle.STRICT);

        String response = mockMvc.perform(get("/api/persons")
                        .param("count", String.valueOf(count)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode persons = objectMapper.readTree(response);

        assertThat(persons.isArray()).isTrue();
        assertThat(persons.size()).isEqualTo(count);

        for (JsonNode person : persons) {
            String firstName = person.get("first_name").asString();
            String lastName = person.get("last_name").asString();
            String gender = person.get("gender").asString();
            String cpr = person.get("cpr").asString();
            String dateOfBirth = person.get("date_of_birth").asString();
            String mobile = person.get("mobile_phone_number").asString();

            JsonNode address = person.get("address");

            String street = address.get("street").asString();
            String number = address.get("number").asString();
            String floor = address.get("floor").asString();
            String door = address.get("door").asString();
            String postalCode = address.get("postal_code").asString();
            String town = address.get("town").asString();

            LocalDate cprDob = LocalDate.parse(cpr.substring(0, 6), cprDateFormatter);
            LocalDate dob = LocalDate.parse(dateOfBirth, dateFormatter);

            assertThat(firstName).isNotBlank();
            assertThat(lastName).isNotBlank();
            assertThat(gender).isIn("male", "female");

            assertThat(cpr).matches("\\d{10}");
            assertThat(dob.format(dateFormatter)).isEqualTo(dateOfBirth);
            assertThat(cprDob).isEqualTo(dob);

            int lastDigit = Character.getNumericValue(cpr.charAt(9));

            if ("female".equals(gender)) {
                assertThat(lastDigit % 2).isEqualTo(0);
            } else {
                assertThat(lastDigit % 2).isEqualTo(1);
            }

            assertThat(mobile).matches("\\d{8}");
            assertThat(FakeDanishPersonService.buildPhonePrefixes())
                    .anyMatch(mobile::startsWith);

            assertThat(street).matches("[A-Za-z]+");
            assertThat(number).matches("[1-9]\\d{0,2}[A-Z]?");
            assertThat(floor).matches("st|([1-9]|[1-9]\\d)");
            assertThat(door).matches("th|mf|tv|([1-9]|[1-4]\\d|50)|[a-z]-?\\d{1,3}");

            assertThat(postalCodes)
                    .anyMatch(postal ->
                            postal.getPostalCode().equals(postalCode)
                                    && postal.getTownName().equals(town)
                    );
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"-2", "-1", "0", "1", "101", "102", "abc"})
    void testThatGetPersonsRejectsInvalidCount(String count) throws Exception {
        mockMvc.perform(get("/api/persons").param("count", count))
                .andExpect(status().isBadRequest());
    }
}
