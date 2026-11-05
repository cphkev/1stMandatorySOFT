package com.cphkev.fakeinfo;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;


@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MySqlTestConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FakeInfoUITest {
        @LocalServerPort
        private int port;

        private static final String BASE_URL = "http://localhost:";

        private Playwright playwright;
        private Browser browser;
        private Page page;

        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {

                playwright = Playwright.create();

                browser = playwright.chromium().launch(
                                new BrowserType.LaunchOptions().setHeadless(false));

                page = browser.newPage();

                page.navigate(BASE_URL + port + "/");
        }

        @AfterEach
        void tearDown() {
                browser.close();
                playwright.close();
        }

        @Test
        void canButtonDisplayValidCpr() {
                page.pause();
                // click the CPR button
                page.locator("button[data-endpoint='/api/cpr']").click();
                page.pause();
                // wait for the JSON output
                page.waitForFunction(
                                "() => document.querySelector('#output').textContent.trim().startsWith('{')");
                // parse JSON

                JsonNode result = objectMapper.readTree(
                                page.locator("#output").textContent());

                // verify the data
                String cpr = result.path("cpr").asString();
                String gender = result.path("gender").asString();

                assertThat(cpr).matches("\\d{10}");
                assertThat(gender).isIn("male", "female");

                int lastDigit = Character.getNumericValue(cpr.charAt(9));

                assertThat(lastDigit % 2)
                                .isEqualTo("female".equals(gender) ? 0 : 1);

        }

        @Test
        void canButtonDisplayValidNameAndGender() {
                // click name + gender button
                page.pause();
                page.getByRole(
                                AriaRole.BUTTON,
                                new Page.GetByRoleOptions()
                                                .setName("Name + Gender")
                                                .setExact(true))
                                .click();
                page.pause();
                // wait for the json outout
                page.waitForFunction(
                                "() => document.querySelector('#output').textContent.trim().startsWith('{')");
                // parse json
                JsonNode result = objectMapper.readTree(
                                page.locator("#output").textContent());

                // Verify the generated data
                String firstName = result.path("first_name").asString();
                String lastName = result.path("last_name").asString();
                String gender = result.path("gender").asString();

                // assert that its not blank and gender is either male or female
                assertThat(firstName).isNotBlank();
                assertThat(lastName).isNotBlank();
                assertThat(gender).isIn("male", "female");

        }

        @Test
        void canBulk5PersonsShowValidData() {

                page.getByRole(
                                AriaRole.BUTTON,
                                new Page.GetByRoleOptions()
                                                .setName("Bulk 5 Persons")
                                                .setExact(true))
                                .click();

                page.waitForFunction(
                                "() => document.querySelector('#output').textContent.trim().startsWith('[')");
                String output = page.locator("#output").textContent();

                JsonNode people = objectMapper.readTree(output);

                // verify that there is a array, and that the array size is 5

                assertThat(people.isArray()).isTrue();
                assertThat(people.size()).isEqualTo(5);

                // verify each person
                for (JsonNode person : people) {

                        assertThat(person.path("first_name").asString()).isNotBlank();
                        assertThat(person.path("last_name").asString()).isNotBlank();

                        String gender = person.path("gender").asString();
                        String cpr = person.path("cpr").asString();

                        assertThat(gender).isIn("male", "female");
                        assertThat(cpr).matches("\\d{10}");

                        assertThat(person.path("date_of_birth").asString())
                                        .matches("\\d{4}-\\d{2}-\\d{2}");

                        assertThat(person.path("address").isObject()).isTrue();

                        assertThat(person.path("mobile_phone_number").asString())
                                        .matches("\\d{8}");

                }

        }
}
