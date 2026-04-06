# Backend Testing Guide

This document is the source of truth for backend testing conventions in this repository.
Use it together with [`AGENTS.md`](AGENTS.md): `AGENTS.md` defines the operating policy, and this file defines how tests should be written and organized.

## Purpose

- Protect business behavior, security rules, and persistence contracts from regression.
- Treat tests as executable use-case specifications, not framework smoke checks.
- Prefer focused tests that explain one business rule or one HTTP contract at a time.
- Avoid shallow tests that only prove Spring wiring unless the wiring itself is the change.

## Test Stack

Current resolved test stack:

- JUnit Jupiter `6.0.2`: test runner, structure, lifecycle, `@Test`, `@DisplayName`
- AssertJ `3.27.6`: default assertion style for new tests
- Mockito `5.20.0`: mocks, stubs, captors, interaction verification
- MockMvc: controller-level HTTP contract testing

Role split:

- Use JUnit for test structure and execution.
- Use AssertJ for assertions in new tests.
- Use Mockito for mocking and verification.
- Use MockMvc for controller status/validation/auth/JSON contract checks.

## Default Conventions

- Follow the default TDD loop: `red -> green -> refactor`.
- Start from the smallest failing test that expresses one concrete behavior.
- Make the test fail for the intended reason before changing production code.
- Do the minimum work required to return to green.
- Refactor only after the test is green again.
- Prefer `given / when / then` comments when they improve scanability.
- Name tests by behavior, not implementation detail.
- Name test methods with `method_underCondition_expectedResult`.
- Use English camelCase for test method names.
- Keep `@DisplayName` for human-readable scenario text when it improves clarity.
- Prefer one test for one reason to fail.
- Prefer AssertJ for assertions in new or touched tests.
- Avoid style-only rewrites in untouched stable tests.

## Test Boundaries

### Controller Tests

Use controller tests for:

- status code
- request validation
- authentication and authorization behavior
- response JSON shape and important fields

Controller tests should:

- use `@WebMvcTest`
- mock the service layer
- avoid asserting service internal logic

Recommended naming:

- `XxxControllerTest`
- `XxxControllerAuthTest`
- `XxxControllerValidationTest`

### Service / Use-Case Tests

Use service or use-case tests for:

- business rules
- branching and defaults
- exception flow
- persistence interaction and saved values
- mapping decisions that happen in service logic

Service tests should:

- use JUnit + Mockito
- verify repository interactions only where they are part of the behavior
- use `ArgumentCaptor` when saved entity state matters

Preferred naming for new files:

- `CreateFreeGameUseCaseTest`
- `GetFreeGameDetailUseCaseTest`
- `UpdateFreeGameInfoUseCaseTest`

Avoid creating new oversized `*ServiceImplTest` files. Existing large files may be split gradually when touched for real work.

### Repository / Integration Tests

Use repository or integration-style tests only when:

- JPA mapping is the change
- Flyway/schema behavior is the change
- database-specific behavior must be verified

Use `@SpringBootTest` only when a full application context or integrated flow is the subject of the change.

## File Naming and Placement

- Put tests under the same domain package as the production code they protect.
- Keep controller tests under `src/test/java/.../<domain>/controller`.
- Keep use-case/service tests under `src/test/java/.../<domain>/service`.
- Keep test support code in the same domain package under `support` or `fixtures` when reuse becomes real.

Examples:

- `courtManager/controller/CourtManagerControllerValidationTest`
- `courtManager/service/CreateFreeGameUseCaseTest`
- `courtManager/service/support/FreeGameFixtures`

## Assertions and Verification

Default for new tests:

- use `assertThat(...)`
- use `assertThatThrownBy(...)` for exception assertions
- use `jsonPath(...)` in MockMvc tests

Examples:

```java
assertThat(response.getLocation()).isEqualTo("잠실 배드민턴장");
```

```java
assertThatThrownBy(() -> freeGameService.getFreeGameDetail(accountId, gameId))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("존재하지 않는 게임");
```

```java
mockMvc.perform(get("/free-games/{gameId}", gameId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.location").value("잠실 배드민턴장"));
```

Mockito defaults:

- use `when(...)` for stubbing
- use `verify(...)` for meaningful interaction checks
- use `ArgumentCaptor` when saved state is the assertion target
- avoid `verifyNoMoreInteractions()` unless the interaction boundary itself is the behavior being protected

JUnit assertions such as `assertEquals` are allowed in legacy tests or very small cases, but new tests should prefer AssertJ.

## Fixtures and Test Data

- Extract common fixtures only after duplication becomes stable.
- Fixture helpers should provide domain-valid defaults and let tests override only what matters.
- Prefer small fixture builders over giant shared object graphs.
- Keep fixture code in the same domain package unless it is clearly cross-domain.

Example direction:

```java
public final class FreeGameFixtures {
    public static FreeGame freeGame(Long gameId, User organizer) { ... }
    public static FreeGameSetting setting(FreeGame freeGame, int courtCount, int roundCount) { ... }
}
```

## When to Extract Repetition

- Prefer `@BeforeEach` for repeated setup inside one test class.
- Prefer `support` or `fixtures` classes for repeated domain objects shared across files.
- Prefer JUnit test interfaces with default methods for repeated helper behavior shared across sibling test classes.
- Prefer AssertJ custom assertions or a small helper when the same assertion pattern repeats.
- Avoid large abstract base test classes as the default reuse mechanism.
- Avoid extracting helpers that hide the test intent or are only used once.

## Execution

Run the full suite for meaningful backend changes:

```bash
./gradlew test
```

Run a single test class while iterating:

```bash
./gradlew test --tests "*CreateFreeGameUseCaseTest"
```

Suggested flow:

1. Run the smallest related test while in the TDD loop.
2. Run the touched package or domain tests when green.
3. Run `./gradlew test` before considering the work complete.

## Examples

### Service / Use-Case Example

```java
@Test
@DisplayName("자유게임 생성 시 location이 있으면 저장한다")
void createFreeGame_withLocation_savesLocation() {
    // given: location이 포함된 자유게임 생성 요청을 준비한다.
    CreateFreeGameRequest request = CreateFreeGameRequest.builder()
            .title("주말 자유게임")
            .location("잠실 배드민턴장")
            .gradeType(GradeType.NATIONAL)
            .courtCount(2)
            .roundCount(3)
            .build();

    when(gameRepository.save(any(FreeGame.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    // when: 자유게임 생성을 수행한다.
    freeGameService.createFreeGame(1L, request);

    // then: 저장되는 엔티티에 location이 반영되어야 한다.
    ArgumentCaptor<FreeGame> captor = ArgumentCaptor.forClass(FreeGame.class);
    verify(gameRepository).save(captor.capture());
    assertThat(captor.getValue().getLocation()).isEqualTo("잠실 배드민턴장");
}
```

### Controller Validation Example

```java
@Test
@DisplayName("자유게임 생성 시 title 누락이면 400을 반환한다")
void createFreeGame_withoutTitle_returnsBadRequest() throws Exception {
    // given: title이 없는 생성 요청을 준비한다.
    CreateFreeGameRequest request = CreateFreeGameRequest.builder()
            .courtCount(2)
            .roundCount(3)
            .build();

    // when & then: validation 실패로 400을 반환해야 한다.
    mockMvc.perform(post("/free-games")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(authenticatedUser(1L))
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
}
```

### Where to Put a Field Test

When a field such as `location` is added:

- creation behavior goes into `CreateFreeGameUseCaseTest`
- detail mapping goes into `GetFreeGameDetailUseCaseTest`
- update behavior goes into `UpdateFreeGameInfoUseCaseTest`

Do not create a field-specific test file such as `LocationTest`.

## References

- Spring Boot test dependencies: [Spring Boot Test Scope Dependencies](https://docs.spring.io/spring-boot/reference/testing/test-scope-dependencies.html)
- JUnit display names: [JUnit Display Names](https://docs.junit.org/6.0.2/writing-tests/display-names.html)
- Mockito reference: [Mockito Javadoc](https://site.mockito.org/javadoc/current/org/mockito/Mockito.html)
- AssertJ overview: [AssertJ](https://github.com/assertj/assertj)
