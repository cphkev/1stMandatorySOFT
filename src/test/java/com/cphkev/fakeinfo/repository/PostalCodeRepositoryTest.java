package com.cphkev.fakeinfo.repository;

import com.cphkev.fakeinfo.MySqlTestConfiguration;
import com.cphkev.fakeinfo.Populator;
import com.cphkev.fakeinfo.model.PostalCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Import(MySqlTestConfiguration.class)
class PostalCodeRepositoryTest {

    @Autowired
    private PostalCodeRepository postalCodeRepository;

    @BeforeEach
    void setUp() {
        postalCodeRepository.saveAll(Populator.createPostalCodes());
    }

    @Test
    void testFindRandomPostalCodeReturnsAPostalCode() {
        PostalCode result = postalCodeRepository.findRandomPostalCode()
                .orElseThrow();

        assertThat(result.getPostalCode())
                .isIn("8000", "5000", "9000");

        assertThat(result.getTownName()).isNotBlank();
    }
}