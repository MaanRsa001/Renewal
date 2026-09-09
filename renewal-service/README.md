# Renewal Service (Spring Boot API)

Backend for the General Insurance Renewal Module. See the top-level
`renewal-module-design.md` for full architecture context.

## Prerequisites
- Java 17+
- Maven 3.9+
- MySQL 8.x

## Configuration

Configuration lives in `src/main/resources/`:

- `application.properties` - base config, every environment-specific value
  is an `${ENV_VAR:default}` placeholder so the same jar runs anywhere just
  by changing environment variables.
- `application-dev.properties` - local dev overrides (verbose SQL logging,
  full health details). Activate with `--spring.profiles.active=dev`.
- `application-prod.properties` - removes the convenience defaults and
  requires `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`,
  `CORS_ALLOWED_ORIGINS` from the real environment - startup fails loudly
  if any are missing, rather than silently using a checked-in default.
  Activate with the `SPRING_PROFILES_ACTIVE=prod` environment variable
  (not just a CLI flag, so it survives however your infra actually launches
  the process).
- `application-test.properties` - H2 in-memory profile for future
  `@SpringBootTest`/`@DataJpaTest`-style tests. **Caveat:** the Flyway
  migrations use MySQL-specific SQL (`JSON_OBJECT()`, a `GENERATED ...
  STORED` column, session variables in the mock-data script) that H2 won't
  fully run as-is - see the comment at the top of that file for the two
  realistic ways to actually get a working test database (Testcontainers
  with real MySQL is the recommended one). The unit tests actually included
  in this project don't need this profile at all - they instantiate POJOs
  directly with no Spring context.
- `.env.example` - reference list of every environment variable the app
  reads, for local `export`/docker-compose/IDE run-config use. Spring Boot
  doesn't read `.env` files natively.

## Setup

1. Create the database and a user:
   ```sql
   CREATE DATABASE renewal_db CHARACTER SET utf8mb4;
   CREATE USER 'renewal_user'@'%' IDENTIFIED BY 'changeme';
   GRANT ALL PRIVILEGES ON renewal_db.* TO 'renewal_user'@'%';
   ```

2. Copy `.env.example` to `.env` and fill in real values, then export them
   into your shell (or set them in your IDE's run configuration / Docker
   Compose `env_file`):
   ```
   export $(cat .env | xargs)
   ```

3. Build and run (dev profile - verbose logging, permissive defaults):
   ```
   mvn clean install
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

   Running with no `-Dspring-boot.run.profiles` flag at all also works -
   `application.properties` alone has sane localhost defaults for every
   value. Use the `prod` profile only once real secrets are wired up via
   environment variables; it will refuse to start without them.

   Flyway runs all three migrations automatically on first startup:
   `V1__init_schema.sql` (schema + one admin user), `V2__lookup_tables.sql`
   (the dynamic lookup/reference-code catalog), and `V3__mock_data.sql`
   (a full demo dataset - see "Mock data" below).

4. API docs: http://localhost:8080/swagger-ui.html

## Default seeded user

`admin` — the seeded password hash is a **placeholder**. Replace it via
a proper user-provisioning script before using this anywhere beyond a
local dev box; never rely on a migration-seeded credential in production.
The `MANAGER` role (seeded for `admin`) can access every endpoint,
including rule-set activation and the referral override-to-normal action.

## Running tests

```
mvn test
```

`RuleEngineServiceImplTest` and `ConditionEvaluatorTest` cover the
eligibility engine end-to-end using the worked examples from the design
doc (clean policy → ELIGIBLE, high claims/loss ratio → REFER while still
logging every rule that ran, cancelled policy → INELIGIBLE, fraud flag →
REFER rather than auto-decline).

## Note on this build

This project was generated in a sandboxed environment without access to
Maven Central, so `mvn clean install` has **not** been run/verified here.
Run it in your own environment as the first sanity check — if the
compiler flags anything, paste the errors back and they can be fixed
directly.

## Project layout

```
security/    JWT auth, users, roles
product/     Rule Designer: product/country/LOB masters, rule sets, rules, conditions
policy/      Policy + claims snapshot (assumed synced from your core policy admin system)
ruleengine/  The eligibility engine itself (PolicySnapshot, ConditionEvaluator, RuleEngineService)
renewal/     Batch orchestration, renewal cases, execution log, offers
referral/    Referral queue, decisioning, and the override-to-normal workflow
dashboard/   Aggregation queries backing the Angular dashboard widgets
common/      PageResponse envelope, exceptions, global error handling
```

## Dynamic lookups (no more hardcoded enums)

Every "enum-like" classification - rule categories, rule outcomes, condition
operators, logical operators, rule-set status, referral status/priority,
batch status, offer status, and roles - now lives in a single generic
`system_code` table (see `V2__lookup_tables.sql`) instead of Java enums.

- `LookupService` / `LookupController` expose it at `GET /api/v1/lookups/all`
  (everything, grouped) and `GET /api/v1/lookups/{codeType}` (one catalog).
- Business entities store the plain string value (e.g. `RenewalRule.category
  = "CLAIMS"`), validated at write time via `LookupService.requireValid(...)`
  rather than by the Java compiler.
- `system_code.style_hint` (GREEN/AMBER/RED/GRAY) drives the Angular status
  badge colors too, so even presentation coloring is data-driven.
- What's still real code, deliberately: the actual comparison logic per
  condition operator (`ConditionEvaluator`) and the AND/OR evaluation order
  (`RuleEngineServiceImpl`). The *catalog* of what a rule author can pick is
  dynamic; the computation those picks trigger is still implemented in Java.
  Adding a genuinely new operator (e.g. `STARTS_WITH`) still needs a small
  code change there, plus a new `system_code` row for it to appear in the UI.

To add a new code (say, a `HOLD_FOR_DOCUMENTS` referral status): insert a row
into `system_code` with `code_type='REFERRAL_STATUS'` - no redeploy needed
for it to appear in the Angular referral-queue filter or status badges. Any
service-layer logic that specifically branches on it (e.g. a new
`assertNotClosed` case) is still a code change, same as it would be with an
enum.

## Mock data

`V3__mock_data.sql` seeds a fully working demo dataset on top of `V1`/`V2`:
4 additional users (one of each role), rule sets + rules + conditions for
all four products (MOTOR/TZ, FIRE/TZ, CAR/UG, MARINE/KE, plus a DRAFT
MOTOR/UG set to show that state in the Rule Designer), 10 policies with
claim summaries spanning ELIGIBLE/REFER/INELIGIBLE outcomes, a completed
batch run, 10 renewal cases with a full 25-row rule-execution audit trail,
5 referral cases (one already overridden-to-normal with history, one
approved-with-loading, two still open/in-review to populate the queue and
aging widget), and 4 renewal offers. Log in as `admin`, `mwangi` (Senior
Underwriter), `jsmith` (Underwriter), or `okoth` (Renewal Admin) to see
different views of the same data.

