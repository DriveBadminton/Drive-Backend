# Drive-Badminton

Spring Boot 3.5 기반 배드민턴 서비스 백엔드입니다. OAuth 로그인, 사용자 프로필, 지역 조회, 자유게임(코트 매니저) 기능을 제공합니다.

## 주요 기능
- **인증**: Kakao/Google OAuth(로컬에서는 Dummy provider 추가) → JWT Access Token(Bearer), Refresh Token은 `refresh_token` httpOnly/secure 쿠키로 발급. `/auth/login`, `/auth/refresh`, `/auth/logout` 제공.
- **사용자/프로필**: `/users/me` 조회, 프로필 등록·수정, 닉네임+태그 변경(90일 제한), 지역/성별/급수/생년월일 관리, 닉네임/태그 검색.
- **지역**: Flyway로 시드된 시·군·구 목록 제공(`/regions/provinces`, `/regions/{provinceId}/districts`).
- **코트 매니저(자유게임)**: `/free-games` 생성(라운드 수/코트 수/급수/매치 기록 방식 설정), 상세 조회·기본 정보 수정, 라운드/매치 편성 조회 및 일괄 수정(주최자만, 참가자/코트 검증 포함).
- **공통**: 일관된 `ApiResponse` 포맷, Bot 차단/요청 로깅 필터, CORS 허용 도메인 사전 설정, `/actuator/health` 헬스 체크, 로컬 프로필에서 Swagger UI 활성화.

## 기술 스택
- Java 21, Spring Boot 3.5, Spring MVC, Spring Security, Spring Data JPA
- PostgreSQL, Flyway (마이그레이션은 `src/main/resources/db/migration`)
- JWT(JJJWT), OAuth2 Client, Springdoc OpenAPI, H2(테스트)
- Gradle, Docker/Docker Compose

## 환경 변수
`src/main/resources/application.yml` 및 `application-*.yml`에서 다음 값을 읽습니다.
- 데이터베이스: `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB`
- JWT: `JWT_ACCESS_TOKEN_SECRET`, `JWT_ACCESS_TOKEN_EXPIRATION_MS`, `JWT_REFRESH_TOKEN_EXPIRATION_HOURS`
- OAuth: `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
- 실행 프로필: `SPRING_PROFILES_ACTIVE=local|prod` (로컬은 Dummy provider 허용, HTTPS/자가 서명 키스토어 `src/main/resources/certs/local-ssl.p12` 사용)
- Docker Compose는 `.env`를 자동 참조합니다.

## 로컬 실행
1) DB만 필요할 때: `docker compose up db`  
2) 앱 실행(로컬 프로필): `SPRING_PROFILES_ACTIVE=local ./gradlew bootRun`  
   - HTTPS(포트 8080)로 기동되며 `/` → `/swagger-ui/index.html` 로 리다이렉트. 브라우저에서 자체 서명 인증서 신뢰 필요.  
3) 전체 빌드: `./gradlew build` (테스트 포함)  
4) 테스트만: `./gradlew test` (H2 사용, JUnit5)

## Docker 배포 실행
- 애플리케이션과 DB를 함께 올릴 때: `docker compose up`  
  - 앱 컨테이너는 `SPRING_PROFILES_ACTIVE=prod`로 기동하며 Swagger UI는 비활성화됩니다. 리소스 사용 제한은 Compose에 정의돼 있습니다.

## 데이터베이스 & 마이그레이션
- Flyway가 앱 기동 시 자동 실행(`baseline-on-migrate` 활성). 주요 스크립트: 초기 스키마(`V1__init_schema.sql`), 지역 데이터 시드(`V2__seed_region.sql`), 사용자/자유게임 관련 확장(V3~V10).
- 테스트는 H2 인메모리 DB를 사용하며 Flyway와 동일한 스키마로 검증합니다.

## API 빠른 참고
- Auth: `/auth/login`, `/auth/refresh`, `/auth/logout`
- Users: `/users?nickname=...&tag=...`, `/users/me`, `/users/me/profile`(POST/GET/PATCH), `/users/me/profile/prefill`, `/users/me/profile/identity`
- Regions: `/regions/provinces`, `/regions/{provinceId}/districts`
- Court Manager: `/free-games`(POST), `/free-games/{gameId}`(GET/PATCH), `/free-games/{gameId}/rounds-and-matches`(GET/PATCH)
