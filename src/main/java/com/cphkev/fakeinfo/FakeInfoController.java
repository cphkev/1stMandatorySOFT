package com.cphkev.fakeinfo;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
public class FakeInfoController {
    private final FakeDanishPersonService service;

    public FakeInfoController(FakeDanishPersonService service) {
        this.service = service;
    }

    @GetMapping("/api/cpr")
    public Map<String, String> cpr() {
        return service.fakeCpr();
    }

    @GetMapping("/api/name-gender")
    public Map<String, String> nameGender() {
        return service.fakeNameGender();
    }

    @GetMapping("/api/name-gender-dob")
    public Map<String, String> nameGenderDob() {
        return service.fakeNameGenderDob();
    }

    @GetMapping("/api/cpr-name-gender")
    public Map<String, String> cprNameGender() {
        return service.fakeCprNameGender();
    }

    @GetMapping("/api/cpr-name-gender-dob")
    public Map<String, String> cprNameGenderDob() {
        return service.fakeCprNameGenderDob();
    }

    @GetMapping("/api/address")
    public Map<String, String> address() {
        return service.fakeAddress();
    }

    @GetMapping("/api/mobile")
    public Map<String, String> mobile() {
        return service.fakeMobilePhoneNumber();
    }

    @GetMapping("/api/person")
    public Map<String, Object> person() {
        return service.fakePerson();
    }

    @GetMapping("/api/persons")
    public List<Map<String, Object>> persons(@RequestParam(defaultValue = "2") int count) {
        try {
            return service.fakePersonsBulk(count);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "count must be between 2 and 100");
        }
    }

    @GetMapping("/health")
    @ResponseStatus(HttpStatus.OK)
    public void health() {
    }
}
