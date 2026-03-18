# Farmacity SuperApp BFF — API Automation Framework

Java + RestAssured + Cucumber BDD automation framework for the Farmacity SuperApp BFF (66 REST endpoints across 8 domains).

---

## Quick Start

```bash
# Run smoke tests against QA
mvn test -Denv=qa -Dtest=SmokeTestRunner

# Run full regression against QA
mvn test -Denv=qa -Dtest=ApiTestRunner

# Run regression against UAT
mvn test -Denv=uat -Dtest=ApiTestRunner

# Run only auth scenarios
mvn test -Denv=qa "-Dcucumber.filter.tags=@auth"

# Run only customers scenarios
mvn test -Denv=qa "-Dcucumber.filter.tags=@customers"
```

---

## Project Structure

```
src/
├── main/java/com/farmacity/bff/automation/
│   ├── api/            ApiEndpoints.java (all 66 paths), PathBuilder.java
│   ├── auth/           AuthScheme, TokenManager, AuthHeaderProvider
│   ├── client/         ApiClient (single HTTP engine), RequestSpec, HttpMethod
│   ├── config/         Environment, ConfigLoader (reads *.properties by -Denv)
│   ├── context/        ScenarioContext, CtxKeys, CleanupRegistry
│   ├── models/         Request/Response POJOs
│   └── utils/          JsonUtils, DataFaker, AssertionUtils
│
└── test/
    ├── java/com/farmacity/bff/automation/
    │   ├── hooks/      CucumberHooks (@Before/@After + cleanup execution)
    │   ├── runners/    ApiTestRunner (regression), SmokeTestRunner (@smoke only)
    │   └── steps/      GenericApiSteps, AuthSteps, CustomerSteps, ShoppingCartSteps
    │
    └── resources/
        ├── config/     dev|qa|uat|prod.properties
        ├── features/   Gherkin scenarios organized by domain
        ├── schemas/    JSON Schema files for response validation
        ├── cucumber.properties
        └── logback-test.xml
```

---

## Environments

| Env   | Base URL                          | Run with     |
|-------|-----------------------------------|--------------|
| dev   | https://bff-dev.farmacity.com.ar  | `-Denv=dev`  |
| qa    | https://bff-qa.farmacity.com.ar   | `-Denv=qa`   |
| uat   | https://bff-uat.farmacity.com.ar  | `-Denv=uat`  |
| prod  | https://bff.farmacity.com.ar      | `-Denv=prod` |

---

## Authentication

| Scheme       | Header                              | Used for                 |
|--------------|-------------------------------------|--------------------------|
| VTEX_TOKEN   | `Authorization: VtexIdToken <t>`    | ~56 standard endpoints   |
| SSO_TOKEN    | `Authorization: VtexSsoToken <t>`   | SSO login flow           |
| API_KEY      | `X-Api-Key: <key>`                  | `/admin/*` only          |
| ANONYMOUS    | *(none)*                            | Login + registration     |

---

## Scenario Cleanup Strategy

Any step that **creates** a resource must register its **delete action** in `CleanupRegistry` immediately:

```java
cleanupRegistry.register(
    "Delete address [" + addressId + "] for customer [" + dni + "]",
    () -> deleteAddress(dni, addressId)
);
```

`CucumberHooks @After` runs `cleanupRegistry.executeAll()` which pops and executes
cleanups in **LIFO order** — regardless of scenario pass/fail. This prevents test
pollution across runs.

---

## Tag Taxonomy

| Tag          | Meaning                                              |
|--------------|------------------------------------------------------|
| `@smoke`     | Critical path, fast (~10 scenarios)                  |
| `@regression`| Full suite                                           |
| `@auth`      | Authentication domain                                |
| `@customers` | Customer profile, addresses, favorites               |
| `@shopping`  | Carts, checkout, orders                              |
| `@catalog`   | Search, inventory, categories                        |
| `@admin`     | Admin endpoints (requires X-Api-Key)                 |
| `@flow`      | Multi-step stateful scenarios                        |
| `@negative`  | Error/validation scenarios                           |
| `@v2`        | v2-versioned endpoints                               |
| `@wip`       | Work in progress — excluded from all runners         |

---

## Adding a New Scenario

1. Identify the endpoint domain and tag (`@auth`, `@shopping`, etc.)
2. Write the Gherkin in the corresponding `features/{domain}/` file
3. If a new step is needed, add it to the relevant `*Steps.java` class
4. If the step creates a resource, register a cleanup action immediately
5. Tag with `@wip` until you confirm it passes, then remove `@wip`

---

## Allure Report

```bash
mvn allure:serve
```
