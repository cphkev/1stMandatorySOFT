package com.cphkev.fakeinfo.provider;

import com.cphkev.fakeinfo.model.PersonName;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class PersonNameProvider {

    private final List<PersonName> persons;

    public PersonNameProvider() {
        ObjectMapper objectMapper = new ObjectMapper();

        try (InputStream input = new ClassPathResource("person-names.json").getInputStream()) {
            JsonNode root = objectMapper.readTree(input);
            JsonNode personsNode = root.get("persons");

            if (personsNode == null || !personsNode.isArray() || personsNode.isEmpty()) {
                throw new IllegalStateException("person-names.json must contain a non-empty persons array");
            }

            this.persons = objectMapper.readerForListOf(PersonName.class)
                    .readValue(personsNode);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load person-names.json", e);
        }
    }

    public List<PersonName> getAll() {
        return persons;
    }
}