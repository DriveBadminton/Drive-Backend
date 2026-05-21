# Backend Agent Guide

This document defines the operating rules for agents and contributors working inside the RallyOn backend repository.

## Project Context

- Stack: Java 21, Spring Boot 4.0.2, Spring Web, Spring Security, OAuth2 Client, OAuth2 Resource Server, Authorization Server, JPA, Flyway, PostgreSQL, springdoc OpenAPI
- Build tool: Gradle
- Main package root: `com.gumraze.rallyon.backend`
- Default verification command: `./gradlew test`
- Stronger packaging verification when runtime boundaries matter: `./gradlew clean test bootJar`

## Source-Of-Truth Documents

Use each document for its intended concern:

- [`ARCHITECTURE.md`](ARCHITECTURE.md): canonical architecture, module boundaries, ubiquitous language
- [`TESTING.md`](TESTING.md): testing strategy, file placement, assertion style, TDD guidance
- [`CONTRIBUTING.md`](CONTRIBUTING.md): issue, branch, commit, PR, and merge workflow
- `AGENTS.md`: repository-local operating guidance for Codex and contributors

If this file appears to conflict with the code, verify the current package structure first and then update the document.

## Current Architecture Shape

The backend is not a pure layered codebase anymore.
It is also not fully normalized into one uniform hexagonal package layout.
Treat it as a modular monolith with mixed maturity by module.

Current top-level modules include:

- `identity`
- `authorization`
- `user`
- `courtManager`
- `region`
- `api`
- `common`
- `config`
- `security`
- `web`

### Complex Modules

These modules mostly follow an `adapter / application / domain` shape:

- `identity`
- `authorization`
- `user`
- `courtManager`

However, they are still in a transitional state.
Some of them also retain supporting packages such as:

- `dto`
- `entity`
- `constants`
- `config`
- `application/support`

Do not assume every complex module has exactly the same package shape.
Match the existing local structure of the module you touch.

### Simpler Modules

`region` is currently closer to a layered structure and uses packages such as:

- `internal/web`
- `internal/service`
- `internal/persistence`
- `dto`

Do not force `region` into ports-and-adapters unless the architecture document and actual code both move that way.

## Working Rules

- Read the target module structure before introducing new packages.
- Prefer fitting into the existing module shape over broad structural cleanup.
- Do not mix a documentation task with repository-wide package refactoring.
- Do not invent new shared abstractions unless the current change clearly needs them.
- Keep security-sensitive logic explicit and easy to audit.
- Keep transactional boundaries in application/service code, not in controllers or low-level adapters.

## Module-Level Guidance

- For `identity`, `authorization`, `user`, and `courtManager`, prefer use-case-oriented changes that respect the existing `adapter / application / domain` direction.
- For these complex modules, do not introduce a second parallel architecture inside the same feature area.
- For `region` and other simpler flows, keep the layered approach simple and avoid speculative ports/adapters.
- `api`, `common`, `config`, `security`, and `web` are supporting boundaries. Do not move business rules there just because they are easier to reach.

## Engineering Principles

### TDD

TDD is the default for business logic changes, bug fixes, and new behavior.

- Start from a failing test when the behavior is clear enough to specify.
- Add regression coverage for bugs before or alongside the fix.
- Keep tests behavior-focused rather than framework-smoke focused.
- Use [`TESTING.md`](TESTING.md) for concrete test layout and style.

### DRY

- Remove real duplication, not incidental similarity.
- Extract shared logic only after the duplication is stable.

### KISS

- Prefer straightforward Spring patterns over clever abstractions.
- Keep use-case flow easy to follow from controller/adapter to application to domain.

### YAGNI

- Do not introduce new layers, generic base classes, or speculative extension points without an immediate need.
- Do not use a documentation update as an excuse to redesign package boundaries.

## Coding Rules

- Prefer constructor injection.
- Keep request validation near the HTTP boundary and domain validation near the business rule.
- Keep controller logic thin.
- Keep persistence details out of domain code.
- Prefer descriptive names over explanatory comments for simple behavior.
- Prefer simple, descriptive class names that rely on package context instead of repeating it.
- Avoid repeating domain or feature terms in class names when the package path already makes that context obvious.
- Concrete adapter names may include the implementation technology only when it materially distinguishes the class.
- Prefer noun-based class names and whole words over compressed abbreviations.
- Use `record` freely for immutable request/response or command/result types when the local module already follows that direction.
- New or modified interfaces under `src/main/java` should normally include JavaDoc before branch push.
- This is especially important for `application/port/in`, `application/port/out`, and `api` interfaces because they define contracts across boundaries.
- Write the first JavaDoc sentence as a short summary and describe the contract or call context rather than implementation details.
- Add method-level JavaDoc when the meaning, input contract, or returned guarantee is not obvious from the signature alone.

## Verification Rules

- Meaningful backend changes require `./gradlew test`.
- Runtime-boundary, packaging, or delivery-sensitive changes should additionally run `./gradlew clean test bootJar`.
- Security, auth, persistence, and API contract changes require targeted tests, not only the full suite.

## Git Workflow

Follow the repository workflow from [`CONTRIBUTING.md`](CONTRIBUTING.md):

1. Create an issue.
2. Create a branch from that issue.
3. Commit with the agreed convention.
4. Open a PR.
5. Merge with squash merge.

## Practical Guidance For Agents

- Prefer facts from the current code over stale mental models.
- If one module is in transition, keep your change locally consistent instead of normalizing the whole repository.
- If a request would require repository-wide structural cleanup, separate that from the feature or bug-fix PR unless the issue explicitly asks for the refactor.
