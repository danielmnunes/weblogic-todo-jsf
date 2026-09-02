# Repository Guidelines

## Project Structure & Module Organization

This Maven WAR application uses Jakarta Faces 3.0 on WebLogic 15.1.1 and Oracle Database Free. Production Java code is organized under `src/main/java/com/weblogic/todo/`: `domain` contains entities and filters, `repository` handles persistence, `service` contains business rules, and `web` contains CDI/JSF backing beans and messages. JSF pages/configuration are in `src/main/webapp`, styles are under `src/main/webapp/resources/css`, and JPA settings are in `src/main/resources/META-INF`.

Unit tests mirror the production packages in `src/test/java`. Database integration tests and their persistence configuration live in `src/it/java` and `src/it/resources`. Container assets are under `docker/`, with orchestration in `docker-compose.yml`.

## Build, Test, and Development Commands

- `mvn verify` — compile, run unit tests, generate JaCoCo, and enforce 90% line coverage.
- `mvn -DskipTests package` — build `target/weblogic-todo-jsf.war` for deployment.
- `export TESTCONTAINERS_RYUK_DISABLED=true; export DOCKER_HOST="unix://${XDG_RUNTIME_DIR:-/run/user/$(id -u)}/podman/podman.sock"; mvn verify -Pit` — run Oracle-backed integration tests using Podman/Docker.
- `podman compose up --build` — start the local WebLogic and Oracle environment; the app is available at `http://localhost:7001/todo`.
- `podman compose down` — stop the local containers.

## Coding Style & Naming Conventions

Use four-space indentation, UTF-8, and existing Java 21 features (for example, `var`, switch expressions, and `String.formatted`). Keep packages lowercase, classes/enums PascalCase, and methods/fields descriptive camelCase. Follow the Jakarta `jakarta.*` imports, constructor injection, and Portuguese user-facing messages. No formatter or linter is configured; match neighboring files and avoid unrelated reformatting.

## Testing Guidelines

Use JUnit 5 and Mockito for unit tests. Name classes after the subject with a `Test` suffix and methods as behavior-focused descriptions, such as `createNormalizesFieldsAndPersists`. Add tests for validation, missing entities, and persistence behavior when changing those areas. Run `mvn verify` before submitting; integration tests require the `it` profile and a container runtime. Inspect `target/site/jacoco/index.html` if coverage fails.

## Commit & Pull Request Guidelines

Use a short, imperative commit subject describing the change (for example, `Add ...`), with a concise body when context is useful. Pull requests should explain the behavior and deployment impact, include test commands/results, link the relevant issue when one exists, and attach screenshots for JSF/UI changes. Call out any required Oracle, WebLogic, Podman, or configuration changes explicitly.

## Security & Configuration Tips

Local container credentials and datasource details are defined in the documented compose setup. Do not commit real credentials, production connection strings, or generated build output. Keep WebLogic and Oracle configuration changes synchronized across `docker/`, `docker-compose.yml`, and the relevant JPA persistence files.
