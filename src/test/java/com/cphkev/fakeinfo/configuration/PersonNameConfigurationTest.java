package com.cphkev.fakeinfo.configuration;

import com.cphkev.fakeinfo.model.PersonName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PersonNameConfigurationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final PersonNameConfiguration personNameConfiguration = new PersonNameConfiguration();

    @Test
    void testThatPersonNamesAreLoadedFromJsonFile() {
        List<PersonName> persons = personNameConfiguration.personNames(objectMapper);

        assertThat(persons)
                .isNotNull()
                .isNotEmpty();
    }

    @Test
    void testThatPersonNamesContainValidData() {
        List<PersonName> persons = personNameConfiguration.personNames(objectMapper);

        assertThat(persons).isNotEmpty();

        for (PersonName person : persons) {
            assertThat(person.name())
                    .isNotNull()
                    .isNotBlank();

            assertThat(person.surname())
                    .isNotNull()
                    .isNotBlank();

            assertThat(person.gender())
                    .isIn("male", "female");
        }
    }
}
