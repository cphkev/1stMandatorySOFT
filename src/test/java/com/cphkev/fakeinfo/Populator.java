package com.cphkev.fakeinfo;

import com.cphkev.fakeinfo.model.PostalCode;

import java.util.List;

public final class Populator {
    public static List<PostalCode> createPostalCodes() {
        return List.of(
                PostalCode.builder()
                        .postalCode("8000")
                        .townName("Aarhus C")
                        .build(),
                PostalCode.builder()
                        .postalCode("5000")
                        .townName("Odense C")
                        .build(),
                PostalCode.builder()
                        .postalCode("9000")
                        .townName("Aalborg")
                        .build()
        );
    }
}
