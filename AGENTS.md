# AGENTS.md

## Project quick context
- Tech: Java 21, Spring Boot 3.5, Gradle, JUnit 5, Flyway, PostgreSQL
- Goal: Drive 백엔드 도메인 서비스(auth, user, region, courtManager 등)

## Setup commands
- Requirements:
  - Java: 21
  - Database: PostgreSQL (로컬은 Docker 사용 가능)
- Install & build:
  - `./gradlew build`
- Run locally:
  - `SPRING_PROFILES_ACTIVE=local ./gradlew bootRun`
- Run with Docker:
  - `docker compose up db` (로컬 Postgres만 실행)
  - `docker compose up` (백엔드 + DB)

## Test & verification (MUST)
- Service/Controller tests only (no integration tests for now):
  - `./gradlew test`
- Static analysis / formatting:
  - `./gradlew check`
- Before opening a pull request:
  - 반드시 위 명령을 모두 통과시킬 것

## Repo structure
- `src/main/java/com/gumraze/drive/drive_backend`: 도메인별 패키지(`auth`, `user`, `region`, `courtManager`, `web`, `config`, `common`)
- `src/main/resources`: 설정(`application.yml`, `application-local.yml`, `application-prod.yml`)과 SQL 리소스
- `src/main/resources/db/migration`: Flyway 마이그레이션(`V1__init_schema.sql`, `V2__seed_region.sql`)
- `src/test/java`: 메인 패키지 구조를 따르는 테스트 코드
- `src/test/resources`: 테스트 리소스

## Code conventions (project-specific only)
- API style:
  - RESTful 원칙 기반(리소스 중심, 상태 코드는 HTTP Status Code 사용)
- Testing:
  - 테스트는 항상 given/when/then 구조로 작성
  - 현재는 service/controller 테스트만 작성(Integration test는 작성하지 않음)
- Java style:
  - 들여쓰기 4칸, 기존 클래스의 브레이스 스타일 유지
  - 패키지는 소문자, 클래스/인터페이스는 PascalCase, 필드/메서드는 camelCase
- Database:
  - Flyway 파일명 규칙: `V<version>__<description>.sql`

## Safe change rules
- Do not:
  - 운영 환경 관련 설정 파일을 임의 변경하지 말 것
  - 마이그레이션 파일을 리라이트하지 말 것(추가만)
- Do:
  - 공개 API 변경 시 문서/테스트 동반 업데이트
  - 보안 민감 코드 변경 시 추가 검증(테스트/리뷰) 요청

## Pull request expectations
- Include:
  - What changed / why
  - How verified (commands + 결과)
  - Risk & rollback notes (if relevant)
