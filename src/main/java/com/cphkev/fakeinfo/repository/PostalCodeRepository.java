package com.cphkev.fakeinfo.repository;

import com.cphkev.fakeinfo.model.PostalCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PostalCodeRepository extends JpaRepository<PostalCode, String> {

    @Query(value = """
            SELECT *
            FROM postal_code
            ORDER BY RAND()
            LIMIT 1
            """, nativeQuery = true)
    Optional<PostalCode> findRandomPostalCode();
}
