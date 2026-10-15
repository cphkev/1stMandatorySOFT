# 1stMandatorySOFT

Java (Spring Boot) application that generates fake information for Danish persons.

## Run

On Windows:

```bash
.\mvnw.cmd spring-boot:run
```

On macOS or Linux:

```bash
bash ./mvnw spring-boot:run
```

## Continuous test pipeline

On Windows, run:

```bash
.\mvnw.cmd clean verify
```

On macOS or Linux, run:

```bash
bash ./mvnw clean verify
```

The Maven Wrapper downloads the configured Maven version the first time it runs. These commands run the Maven test suite, integration tests, and SpotBugs static analysis. The build fails if tests fail or SpotBugs reports findings. GitHub Actions runs the same wrapper command on pushes and pull requests.

Add JUnit tests under `src/test/java`. Maven automatically discovers regular test classes named `*Test.java`; integration test classes named `*IT.java` run in Maven's integration-test phase. No workflow changes are needed for those Java tests.

Standalone API collections and browser end-to-end suites use separate tools, so their commands will need to be added to the workflow when those suites are created. The current project already has a Spring `MockMvc` controller test.

The UI is available at `http://localhost:8080/`.
