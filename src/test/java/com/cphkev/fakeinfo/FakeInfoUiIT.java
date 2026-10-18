package com.cphkev.fakeinfo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FakeInfoUiIT {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final DateTimeFormatter CPR_DATE = DateTimeFormatter.ofPattern("ddMMyy");

    @LocalServerPort
    private int port;

    @Test
    void personButtonDisplaysAConsistentGeneratedPerson() throws Exception {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            try {
                Page page = browser.newPage();
                page.navigate(baseUrl());

                page.locator("button[data-endpoint='/api/person']").click();
                page.waitForFunction("() => document.querySelector('#output').textContent.trim().startsWith('{')");

                JsonNode person = OBJECT_MAPPER.readTree(page.locator("#output").textContent());
                String cpr = person.path("cpr").asText();
                String gender = person.path("gender").asText();
                LocalDate dateOfBirth = LocalDate.parse(person.path("date_of_birth").asText());
                int finalCprDigit = Character.getNumericValue(cpr.charAt(9));

                assertThat(person.path("first_name").asText()).isNotBlank();
                assertThat(person.path("last_name").asText()).isNotBlank();
                assertThat(gender).isIn("female", "male");
                assertThat(cpr).matches("\\d{10}");
                assertThat(cpr.substring(0, 6)).isEqualTo(dateOfBirth.format(CPR_DATE));
                assertThat(finalCprDigit % 2).isEqualTo("female".equals(gender) ? 0 : 1);
                assertThat(person.path("address").isObject()).isTrue();
                assertThat(person.path("mobile_phone_number").asText()).matches("\\d{8}");
            } finally {
                browser.close();
            }
        }
    }

    @Test
    void bulkButtonDisplaysFivePeople() throws Exception {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            try {
                Page page = browser.newPage();
                page.navigate(baseUrl());

                page.locator("button[data-endpoint='/api/persons?count=5']").click();
                page.waitForFunction("() => document.querySelector('#output').textContent.trim().startsWith('[')");

                JsonNode people = OBJECT_MAPPER.readTree(page.locator("#output").textContent());
                assertThat(people.isArray()).isTrue();
                assertThat(people.size()).isEqualTo(5);
                for (JsonNode person : people) {
                    assertThat(person.path("first_name").asText()).isNotBlank();
                    assertThat(person.path("cpr").asText()).matches("\\d{10}");
                    assertThat(person.path("address").isObject()).isTrue();
                    assertThat(person.path("mobile_phone_number").asText()).matches("\\d{8}");
                }
            } finally {
                browser.close();
            }
        }
    }

    private String baseUrl() {
        return "http://127.0.0.1:" + port + "/";
    }
}
