package com.cphkev.fakeinfo.provider;

import com.cphkev.fakeinfo.model.PersonName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PersonNameProviderTest {

    private PersonNameProvider personNameProvider;

    @BeforeEach
    void setUp() {
        personNameProvider = new PersonNameProvider();
    }

    @Test
    void shouldLoadPersonNamesFromJsonFile() {
        List<PersonName> persons = personNameProvider.getAll();

        assertNotNull(persons);
        assertFalse(persons.isEmpty());
    }

    @Test
    void shouldLoadNamesWithAllRequiredFields() {
        List<PersonName> persons = personNameProvider.getAll();

        for (PersonName person : persons) {
            assertNotNull(person.name(), "Name must not be null");
            assertFalse(person.name().isBlank());

            assertNotNull(person.surname(), "Surname must not be null");
            assertFalse(person.surname().isBlank());

            assertNotNull(person.gender(), "Gender must not be null");
            assertFalse(person.gender().isBlank());
        }
    }

    @Test
    void shouldOnlyContainValidGenders() {
        List<PersonName> persons = personNameProvider.getAll();
        Set<String> validGenders = Set.of("female", "male");

        for (PersonName person : persons) {
            assertTrue(
                    validGenders.contains(person.gender()),
                    () -> "Invalid gender: " + person.gender()
            );
        }
    }

    @Test
    void shouldReturnSameLoadedDataOnSubsequentCalls() {
        List<PersonName> firstCall = personNameProvider.getAll();
        List<PersonName> secondCall = personNameProvider.getAll();

        assertSame(firstCall, secondCall);
    }
}
