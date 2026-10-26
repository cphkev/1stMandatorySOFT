package com.cphkev.fakeinfo.configuration;

import com.cphkev.fakeinfo.model.PersonName;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Configuration
public class PersonNameConfiguration {

    @Bean
    public List<PersonName> personNames(ObjectMapper objectMapper) {
        try (InputStream input = new ClassPathResource("person-names.json").getInputStream()) {
            JsonNode root = objectMapper.readTree(input);
            JsonNode personsNode = root != null ? root.get("persons") : null;

            if (personsNode == null || !personsNode.isArray() || personsNode.isEmpty()) {
                throw new IllegalStateException("person-names.json must contain a non-empty persons array");
            }

            return objectMapper.readerForListOf(PersonName.class)
                    .readValue(personsNode);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load person-names.json", e);
        }
    }
}