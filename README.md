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

Before the first run that includes the browser end-to-end tests, install Playwright's Chromium browser once:

On Windows:

```powershell
.\mvnw.cmd "-Dexec.mainClass=com.microsoft.playwright.CLI" "-Dexec.args=install chromium" exec:java
```

On macOS or Linux:

```bash
bash ./mvnw -Dexec.mainClass=com.microsoft.playwright.CLI "-Dexec.args=install chromium" exec:java
```

The Maven Wrapper downloads the configured Maven version the first time it runs. These commands run the Maven test suite, integration tests, generate a JaCoCo coverage report, and run SpotBugs static analysis. The build fails if tests fail or SpotBugs reports findings. GitHub Actions runs the same wrapper command on pushes and pull requests.

After a successful `verify`, open `target/site/jacoco/index.html` to view line and branch coverage. JaCoCo currently reports coverage without enforcing a minimum percentage; a coverage threshold can be added once the test suite is more complete.

Add JUnit tests under `src/test/java`. Maven automatically discovers regular test classes named `*Test.java`; integration test classes named `*IT.java` run in Maven's integration-test phase. `FakeInfoUiIT` uses Playwright to exercise the browser UI. GitHub Actions installs Chromium and its Linux dependencies before running the tests.

The Playwright browser end-to-end tests run as part of `verify`. If you later add an API test collection in Postman, Insomnia, or another tool, add its runner to the workflow as a separate step. The project also has a Spring `MockMvc` controller test.

The UI is available at `http://localhost:8080/`.
