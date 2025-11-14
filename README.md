# 축구 팬을 위한 커뮤니티 공간: KickBoard
⚽football-community-project

## 📝 소개
축구 팬들이 자유롭게 소통하고 정보를 공유하며, 용병 경기를 모집할 수 있는 커뮤니티 플랫폼입니다. 회원 가입 및 로그인, 게시판 기능, 관리자 페이지, 그리고 축구 경기 일정 등을 제공합니다.

## 📦 주요 기능
### V1.0: 핵심 커뮤니티 기능 (Completed)
**1. 회원 (Member) & 인증/인가 (Auth)**
* **Spring Security & JWT:** `JwtAuthenticationFilter`, `EntryPoint`, `Handler`를 커스텀하여 JWT(Access/Refresh Token) 기반의 인증/인가 시스템을 구축했습니다.
* **보안:** `BCryptPasswordEncoder`를 사용해 비밀번호를 단방향 암호화하여 저장합니다.
* **핵심 로직:** 회원가입, 로그인, 회원 탈퇴(`Soft Delete` - `active` 플래그 관리) 기능을 구현했습니다.
* **권한 관리:** `@PreAuthorize` 및 `Role(ADMIN, USER)`을 통해 관리자 API와 사용자 API의 접근 권한을 명확히 분리했습니다.

**2. 커뮤니티 (Post & Comment)**
* **동적 쿼리 (QueryDSL):** `QueryDSL`과 `PostRepositoryCustom`을 구현하여, 게시판 타입(`GENERAL`/`MERCENARY`), 키워드(제목/내용)에 따른 **동적 검색 및 페이징** 기능을 구현했습니다.
* **계층형 댓글:** `self-join`을 활용하여 '댓글'과 '대댓글'이 모두 가능한 계층형 구조를 구현했습니다.
* **유효성 검증:** `@Valid`와 **Validation Groups** (`@GroupSequence`)를 활용하여, '일반 게시글'과 '용병 모집글'(`location`, `matchTime` 등)의 **DTO 유효성 검사 규칙을 동적으로 분리**하여 처리했습니다.

**3. 좋아요 (Like) & 동시성 제어**
* 게시글과 댓글에 대한 '좋아요' 토글(Toggle) 기능을 구현했습니다.
* **[동시성 제어]** '좋아요' 버튼 연타 시 발생하는 **Race Condition(경쟁 상태)** 문제를 해결하기 위해, `Like` 엔티티의 `@Table`에 **`@UniqueConstraint`** (복합 유니크 키: `member_id`, `post_id`)를 적용했습니다.
    * (서비스 로직(1차 방어)이 뚫리더라도, **DB 레벨(2차 방어)**에서 `DataIntegrityViolationException`을 발생시켜 중복 저장을 원천 차단하고 데이터 무결성을 보장합니다.)

### V2.0: 성능 고도화 (Completed)

**1. 경기 일정 (Match) API: 다계층 캐시 아키텍처**
* **문제점:** 외부 API(`FootballDataClient`)에 실시간으로 의존하여, API 장애 시 서비스가 중단되고 응답 속도가 느린 문제를 발견했습니다.
* **L1 캐시 (DB):**
    * **`@Scheduled` (스케줄링):** 매일 새벽 4시, `MatchUpdateScheduler`가 외부 API를 자동으로 호출하여 최신 경기 일정을 `soccer_match` DB 테이블에 **"미리 저장(적재)"**합니다.
    * **DB Pruning:** 매일 새벽 5시, 7일이 지난 **오래된 경기 데이터를 자동으로 `DELETE`**하여, "DB 저장 비용" 문제를 해결했습니다.
* **L2 캐시 (Redis):**
    * **Cache-Aside 패턴:** `MatchService`는 `Redis`를 우선 조회합니다.
    * **(Cache Hit)** `Redis`에 데이터가 있으면 DB 조회 없이 즉시 반환합니다. (빠른 속도)
    * **(Cache Miss)** `Redis`에 데이터가 없으면 `soccer_match` DB(L1 캐시)를 조회하고, 그 결과를 `Redis`에 **1시간 TTL**로 저장한 뒤 반환합니다.

**[🗺️ 향후 로드맵 (Future Roadmap)]**
* **스포츠 이슈 크롤링:** `Jsoup`을 활용하여 실시간 스포츠 뉴스를 크롤링하여 제공.
* **실시간 알림:** `SSE (Server-Sent Events)`를 도입하여, 내 게시글/댓글에 '좋아요'나 '새 댓글'이 달릴 시 실시간 알림 기능 구현.
* **소셜 로그인:** `OAuth2` (Google, Kakao)를 이용한 간편 로그인 기능 추가.


## 🔧 기술 스택
**Backend**: Java 17
- **Spring Boot 3.5.4**: Spring Web, Spring Data JPA, AOP
- **Spring Security**: JWT (Access/Refresh Token) 기반 인증/인가
- **QueryDSL 5.0**: `PostRepositoryCustom` 구현 (동적 검색 및 페이징)
**데이터베이스**: MySQL (AWS RDS) | 운영 DB
- **Redis** | **(L2 캐시)**: `Match` API 조회 성능 향상 (Cache-Aside)
- **Cache**: Redis
- **H2 Database**: 테스트 환경 격리 (DB 완전 분리)
**Infra & DevOps**: AWS EC2, Nginx | (배포 예정) 리버스 프록시 및 무중단 배포
- **@Scheduled**: 매일 새벽 `Match` API 호출 및 DB 저장/삭제 (L1 캐시)
**API & Docs**: REST API
- **Springdoc (OpenAPI 3.0)**: springdoc-openapi-starter-webmvc-ui:2.5.0 사용 |
`FootballDataClient` | `RestTemplate` 을 이용한 외부 API 연동
**Test** 단위 테스트(JUnit 5 & Mockito), 통합 테스트(@SpringBootTest)
- **빌드 툴**: Gradle

## ⚙️ 개발 환경 (Development Environment)
- **IDE**: IntelliJ IDEA
- **JDK**: OpenJDK 17
- **Database Tool**: MySQL Workbench, H2 Console(테스트 DB)
- **API Test Tool**: Swagger UI, Postman

## ERD
![img.png](img.png)



## Trouble Shooting
### (1) [가장 중요] 테스트 환경 격리 실패 (MySQL DB 오염 문제)
* **문제:** `@SpringBootTest` (통합 테스트) 실행 시, `src/main`의 **`MySQL`** 설정을 그대로 읽어와 테스트가 실패했습니다.
  1.  `@Transactional`을 사용해도, `@BeforeEach`와 `@Test`의 트랜잭션이 분리되어 `Post`에 `Comment`가 조회되지 않는(`comments[0]` 없음) 문제가 발생했습니다.
  2.  `@Transactional`을 제거하자, 롤백이 안 되어 `Duplicate entry 'testUser'` (중복 키) 에러가 모든 테스트에서 발생하며, **운영 DB가 테스트 데이터로 오염**되는 심각한 문제가 발생했습니다.
* **해결:**
  1.  `src/test/resources`에 `application.properties`를 추가했으나, Gradle 빌드 캐시 문제로 `MySQL`이 계속 로드되었습니다.
  2.  **`@TestPropertySource`** 어노테이션을 각 테스트 클래스에 직접 명시하여, 테스트 DB를 **`H2` 인메모리 DB**로 강제 주입했습니다.
  3.  `spring.jpa.hibernate.ddl-auto=create-drop`을 적용하여, 매 테스트 실행 시 DB를 **완전히 초기화**함으로써 `Duplicate entry`와 `comments[0]` 문제를 **모두 해결**하고 테스트 환경을 완벽히 격리했습니다.

### (2) [보안] `@PreAuthorize` 권한 테스트 시 500 에러 발생
* **문제:** "ADMIN" 권한이 필요한 API(`/members/admin`)에 "USER"로 접근 시, `Expected: 403 (Forbidden)`이 아닌 `Actual: 500 (Internal Server Error)`이 발생.
* **원인 분석:**
  1.  `JwtAccessDeniedHandler` (403 담당)는 **"보안 필터"** 레벨의 `AccessDeniedException`을 처리합니다.
  2.  하지만 `@PreAuthorize` (메서드 보안)는 `AuthorizationDeniedException`이라는 **"다른 종류"**의 예외를 발생시키며, 이 예외는 "컨트롤러" 영역으로 전달됩니다.
  3.  `GlobalExceptionHandler`에 이 예외 핸들러가 없어, 최종 `@ExceptionHandler(Exception.class)`가 `500`을 반환했습니다.
* **해결:** `GlobalExceptionHandler`에 `AuthorizationDeniedException`을 `403 Forbidden`과 `ErrorResponse` DTO로 처리하는 `@ExceptionHandler`를 명시적으로 **추가**하여, 2단계 보안 예외 처리 아키텍처를 완성했습니다.

### (3) [동시성] '좋아요' 중복 저장 (Race Condition)
* **문제:** `LikeService`의 `toggleLike` 로직(1. 읽기 `findBy...` &rarr; 2. 쓰기 `save/delete`)이 0.002초 이내의 동시 요청(스크립트 공격 등)에 뚫려, **데이터가 중복 저장**될 수 있는 Race Condition(경쟁 상태)을 발견했습니다.
* **해결:**
  * 1차 방어(Service 로직) 외에, **`Like.java`** 엔티티의 `@Table` 어노테이션에 **`@UniqueConstraint`**를 추가했습니다.
  *
