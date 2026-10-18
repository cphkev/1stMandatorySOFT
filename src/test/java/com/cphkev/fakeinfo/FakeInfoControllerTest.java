package com.cphkev.fakeinfo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FakeInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void requiredEndpointsExist() throws Exception {
        List<String> endpoints = List.of(
            "/api/cpr",
            "/api/name-gender",
            "/api/name-gender-dob",
            "/api/cpr-name-gender",
            "/api/cpr-name-gender-dob",
            "/api/address",
            "/api/mobile",
            "/api/person",
            "/api/persons?count=2"
        );

        for (String endpoint : endpoints) {
            mockMvc.perform(get(endpoint)).andExpect(status().isOk());
        }
    }

    @Test
    void bulkBoundsValidated() throws Exception {
        mockMvc.perform(get("/api/persons?count=1")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/persons?count=101")).andExpect(status().isBadRequest());
    }

    @Test
    void personConsistency() throws Exception {
        for (int index = 0; index < 20; index++) {
            String json = mockMvc.perform(get("/api/person"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

            JsonNode person = objectMapper.readTree(json);
            String cpr = person.get("cpr").asString();
            String gender = person.get("gender").asString();
            LocalDate dob = LocalDate.parse(person.get("date_of_birth").asString());

            assertThat(cpr).matches("\\d{10}");
            assertThat(cpr.substring(0, 6)).isEqualTo(dob.format(DateTimeFormatter.ofPattern("ddMMyy")));
            int lastDigit = Character.getNumericValue(cpr.charAt(9));
            if ("female".equals(gender)) {
                assertThat(lastDigit % 2).isEqualTo(0);
            } else {
                assertThat(lastDigit % 2).isEqualTo(1);
            }
        }
    }

    @Test
    void mobilePrefixIsAllowed() throws Exception {
        List<String> allowed = FakeDanishPersonService.buildPhonePrefixes();
        for (int index = 0; index < 50; index++) {
            String json = mockMvc.perform(get("/api/mobile"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
            String mobile = objectMapper.readTree(json).get("mobile_phone_number").asString();
            assertThat(mobile).matches("\\d{8}");
            assertThat(allowed.stream().anyMatch(mobile::startsWith)).isTrue();
        }
    }
}
