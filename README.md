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
* **문제점 (AS-IS):** V1.0의 `Match` API는 매번 외부 API(`FootballDataClient`)를 실시간으로 호출했습니다. **성능 테스트 결과(브라우저 개발자 도구 기준), 평균 응답 속도가 약 2,030ms (2.03초)로 측정되었습니다.**
* **L1/L2 캐시 도입 (TO-BE):** V2.0에서는 `Redis`(L2)와 `DB`(L1)를 사용하는 2-Layer Caching을 도입하고 `Cache-Aside` 패턴을 적용했습니다.
* **개선 결과 (Cache HIT):** **동일한 API**를 다시 호출했을 때, `Redis`에서 캐시를 조회하여 **평균 59ms** (0.059초)의 응답 속도를 확보했습니다.
* **효과:** 약 **97%의 응답 속도 향상**을 달성하여, 사용자 경험과 서버 부하, API 비용 문제를 모두 개선했습니다.

**[🗺️ 향후 로드맵 (Future Roadmap)]**
* **스포츠 이슈 크롤링:** `Jsoup`을 활용하여 실시간 스포츠 뉴스를 크롤링하여 제공.
* **실시간 알림:** `SSE (Server-Sent Events)`를 도입하여, 내 게시글/댓글에 '좋아요'나 '새 댓글'이 달릴 시 실시간 알림 기능 구현.
* **소셜 로그인:** `OAuth2` (Google, Kakao)를 이용한 간편 로그인 기능 추가.


## 🔧 기술 스택
| 구분 | 기술 | 상세 내용 |
| :--- | :--- | :--- |
| **Backend** | Java 17, Spring Boot 3.x | Spring Web, Spring Data JPA |
| | Spring Security | JWT (Access/Refresh Token) 기반 인증/인가 |
| | **QueryDSL 5.0** | 동적 쿼리 및 페이징 구현 (`PostRepositoryCustom`) |
| **Database** | MySQL | 운영 DB (AWS RDS 배포 예정) |
| | **H2 Database** | **테스트 환경 격리** (`@TestPropertySource`) |
| **Cache & Batch** | **Redis** | **(L2 캐시)** `Match` API 응답 속도 97% 향상 (Cache-Aside) |
| | **`@Scheduled`** | **(L1 캐시)** 매일 새벽 `Match` API 호출 및 DB 적재/삭제 |
| **Test** | **JUnit 5, Mockito** | **(단위 테스트)** `Service` 로직 격리 및 검증 |
| | **`@SpringBootTest`** | **(통합 테스트)** `Controller` - `Service` - `DB` E2E 흐름 검증 |
| **API & Docs** | REST API, **Springdoc (OpenAPI 3.0)** | `springdoc-openapi-starter-webmvc-ui:2.5.0` |
| | `FootballDataClient` | `RestTemplate` (또는 `WebClient`)을 이용한 외부 API 연동 |
| **Build** | Gradle | |

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
