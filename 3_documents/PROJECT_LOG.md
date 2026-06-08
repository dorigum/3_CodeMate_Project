# CodeMate Project Log

## 1. 프로젝트 개요

CodeMate는 스터디와 모각코(모여서 각자 코딩) 모집을 위한 백엔드 API 프로젝트이다. 단순 게시판 CRUD를 넘어서, 모집 글 작성, 참여 신청, 방장 승인/거절, 모집 인원 제한, 권한 검증 같은 백엔드 비즈니스 로직을 중심으로 구현하는 것을 목표로 한다.

초기 MVP는 6일 안에 완성 가능한 API 서버를 목표로 하며, Swagger 또는 Postman을 통해 API 결과를 시연할 수 있는 수준까지 개발 진행


---
## 2. MVP 범위

초기 버전에서 다룰 핵심 기능
- 회원가입
- 로그인 및 JWT 인증
- 스터디/모각코 모집 글 CRUD
- 모집 글 목록 조회, 상세 조회
- 검색 및 페이징
- 참여 신청
- 방장 승인/거절
- 모집 인원 초과 방지
- 공통 응답 및 예외 처리
- H2 기반 개발 환경
- Swagger 문서화
- Docker 실행 환경

현재까지는 프로젝트 구조 정리, 엔티티/enum 설계, Repository 뼈대, H2/JPA 설정, 공통 응답/예외 처리, 회원가입 API까지 구현


---
## 3. 기술 스택

### Backend
- Java 17
- Spring Boot 4.0.6
- Spring Web MVC
- Spring Data JPA
- Spring Security
- Bean Validation
- Lombok

### Database
- 개발 단계: H2 Database
- 운영/실행 단계 후보: MySQL

- 현재는 빠른 개발과 테스트를 위해 H2 인메모리 DB를 사용
- MySQL 드라이버는 이미 의존성에 포함되어 있으므로, 이후 `local`, `mysql` 같은 프로필을 나눠 설정을 추가하면 MySQL로 전환 가능

### Build
- Maven
- Maven Wrapper

### Test
- JUnit 5
- SpringBootTest
- MockMvc
- Spring Security Test

### 향후 예정
- JWT: jjwt 계열 의존성 추가 예정
- API Docs: springdoc-openapi Swagger UI 추가 예정
- DevOps: Docker, Docker Compose, AWS EC2 배포 예정


---
## 4. 패키지 구조

```text
com.codemate
├─ domain
│  ├─ user
│  │  ├─ controller
│  │  ├─ dto
│  │  ├─ entity
│  │  ├─ repository
│  │  └─ service
│  ├─ study
│  │  ├─ entity
│  │  └─ repository
│  ├─ studymember
│  │  ├─ entity
│  │  └─ repository
│  └─ techstack
│     ├─ entity
│     └─ repository
└─ global
   ├─ config
   ├─ entity
   ├─ exception
   ├─ response
   └─ security
```

Java 패키지 관례에 맞춰 `studymember`, `techstack`으로 모두 소문자로 변경


---
## 5. 구현 내역

### 5.1 엔티티 공통 기반

`BaseTimeEntity` 

- 위치: `src/main/java/com/codemate/global/entity/BaseTimeEntity.java`
- 역할: `createdAt`, `updatedAt` 공통 관리
- 적용 방식: `@MappedSuperclass`, `@EntityListeners(AuditingEntityListener.class)`

`JpaAuditingConfig`

- 위치: `src/main/java/com/codemate/global/config/JpaAuditingConfig.java`
- 역할: JPA Auditing 활성화
- 적용 어노테이션: `@EnableJpaAuditing`

### 5.2 User 도메인

추가한 파일:
- `User`
- `UserRole`
- `UserRepository`
- `SignupRequest`
- `SignupResponse`
- `UserService`
- `UserController`

`User` 엔티티 주요 필드:
- `id`
- `email`
- `password`
- `nickname`
- `mainTechStack`
- `role`
- `createdAt`
- `updatedAt`

`UserRole` enum:

```java
ROLE_USER,
ROLE_ADMIN
```

회원가입 API:

```text
POST /api/users/signup
```

요청 예시:

```json
{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "codemate",
  "mainTechStack": "Spring Boot"
}
```

응답 예시:

```json
{
  "success": true,
  "message": "회원가입이 완료되었습니다.",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "nickname": "codemate",
    "mainTechStack": "Spring Boot"
  }
}
```

비밀번호는 `BCryptPasswordEncoder`로 암호화해서 저장

### 5.3 Study 도메인

추가한 파일:
- `Study`
- `StudyCategory`
- `StudyStatus`
- `MeetingType`
- `StudyRepository`

`Study` 엔티티 주요 필드:
- `id`
- `host`
- `title`
- `content`
- `category`
- `meetingType`
- `location`
- `maxMemberCount`
- `currentMemberCount`
- `status`
- `createdAt`
- `updatedAt`

`StudyCategory` enum:

```java
STUDY,
MOGAKKO
```

`StudyStatus` enum:

```java
RECRUITING,
CLOSED,
IN_PROGRESS,
FINISHED
```

`MeetingType` enum:

```java
ONLINE,
OFFLINE
```

### 5.4 StudyMember 도메인

추가한 파일:
- `StudyMember`
- `StudyMemberStatus`
- `StudyMemberRepository`

`StudyMember`는 유저와 스터디 사이의 참여 신청/승인 상태를 표현하는 핵심 조인 엔티티

`StudyMember` 엔티티 주요 필드:
- `id`
- `study`
- `user`
- `status`
- `createdAt`
- `updatedAt`

중복 신청 방지를 위해 `study_id`, `user_id` 조합에 unique constraint를 추가

`StudyMemberStatus` enum:

```java
PENDING,
APPROVED,
REJECTED
```

향후 구현할 핵심 로직:
- 로그인 사용자만 참여 신청 가능
- 본인 글에는 신청 불가
- 이미 신청한 스터디에는 중복 신청 불가
- 모집 완료 상태에서는 신청 불가
- 방장만 승인/거절 가능
- 승인 시 현재 인원 증가
- 정원 도달 시 모집 상태를 `CLOSED`로 변경

### 5.5 TechStack 도메인

추가한 파일:
- `TechStack`
- `StudyTechStack`
- `TechStackRepository`
- `StudyTechStackRepository`

`TechStack` 엔티티 주요 필드:
- `id`
- `name`
- `createdAt`
- `updatedAt`

`StudyTechStack`은 Study와 TechStack의 N:M 관계를 직접 조인 엔티티로 표현한다.

중복 매핑 방지를 위해 `study_id`, `tech_stack_id` 조합에 unique constraint를 추가


---
## 6. 공통 응답 및 예외 처리

### 6.1 ApiResponse

위치:

```text
src/main/java/com/codemate/global/response/ApiResponse.java
```

공통 응답 구조:

```json
{
  "success": true,
  "message": "요청 처리 메시지",
  "data": {}
}
```

실패 응답 구조:

```json
{
  "success": false,
  "message": "에러 메시지"
}
```

`data`가 없을 때는 `@JsonInclude(JsonInclude.Include.NON_NULL)`에 의해 응답에서 제외된다.

### 6.2 ErrorCode

현재 정의된 에러 코드:
- `INVALID_INPUT_VALUE`
- `DUPLICATE_EMAIL`
- `DUPLICATE_NICKNAME`
- `INTERNAL_SERVER_ERROR`

### 6.3 BusinessException

- 비즈니스 예외를 표현하는 커스텀 RuntimeException
- 서비스 레이어에서 도메인 규칙 위반이 발생하면 `BusinessException`을 던지고, 글로벌 예외 핸들러에서 공통 응답 형태로 변환

### 6.4 GlobalExceptionHandler

현재 처리하는 예외:
- `BusinessException`
- `MethodArgumentNotValidException`

회원가입 요청값 검증 실패 시 필드명과 검증 메시지를 조합해 응답


---
## 7. Security 설정

`SecurityConfig`

현재 설정:
- CSRF 비활성화
- H2 Console iframe 접근을 위해 frame option same-origin 설정
- `/api/users/signup` 허용
- `/h2-console/**` 허용
- 그 외 요청은 인증 필요
- form login 비활성화
- http basic 비활성화
- `BCryptPasswordEncoder` Bean 등록

현재는 회원가입 API 확인을 위한 최소 보안 설정
이후 로그인/JWT 구현 단계에서 인증 필터와 토큰 검증 로직을 추가해야 한다.


---
## 8. H2/JPA 설정

파일:

```text
src/main/resources/application.properties
```

현재 설정:

```properties
spring.application.name=CodeMate

spring.datasource.url=jdbc:h2:mem:codemate;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.highlight_sql=true
```

H2 Console 접속 정보:

```text
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:codemate
User Name: sa
Password: 비워두기
```

`spring.jpa.open-in-view=false`를 명시해 API 서버에서 View 렌더링 단계의 지연 로딩 경고를 방지


---
## 9. Maven Wrapper 문제 해결

초기 `mvnw.cmd` 실행 시 다음 오류가 발생

```text
Cannot index into a null array.
Cannot start maven from wrapper
```

원인:
`mvnw.cmd` 내부에서 `C:\Users\KOSTA\.m2`가 일반 폴더인데도 심볼릭 링크처럼 `.Target[0]`에 바로 접근하고 있는 문제

수정 내용:

```powershell
$MAVEN_M2_ITEM = Get-Item $MAVEN_M2_PATH
if (!$MAVEN_M2_ITEM.Target -or $MAVEN_M2_ITEM.Target.Count -eq 0) {
  $MAVEN_WRAPPER_DISTS = "$MAVEN_M2_PATH/wrapper/dists"
} else {
  $MAVEN_WRAPPER_DISTS = $MAVEN_M2_ITEM.Target[0] + "/wrapper/dists"
}
```

수정 후 Maven Wrapper 정상 실행을 확인

```text
Apache Maven 3.9.16
Java version: 21.0.10
```


---
## 10. 테스트 내역

테스트 파일:

```text
src/test/java/com/codemate/CodeMateApplicationTests.java
```

현재 테스트:
- Spring context 로딩 테스트
- 회원가입 API MockMvc 테스트

회원가입 테스트 검증 내용:
- `POST /api/users/signup`
- HTTP 201 Created
- `success=true`
- 응답 email 확인
- 응답 nickname 확인

최종 테스트 결과:

```text
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

테스트 실행 명령:

```powershell
.\mvnw.cmd test
```

## 11. 서버 실행 및 직접 확인 방법

서버 실행:

```powershell
.\mvnw.cmd spring-boot:run
```

회원가입 API:

```text
POST http://localhost:8080/api/users/signup
```

Postman 요청 Body:

```json
{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "codemate",
  "mainTechStack": "Spring Boot"
}
```

중복 이메일로 다시 요청하면 `DUPLICATE_EMAIL`에 해당하는 에러 응답이 반환


---
## 12. MySQL 전환 계획

현재는 H2로 개발을 진행한다. 이유는 다음과 같다.
- 초기 개발 속도가 빠르다.
- 별도 DB 서버 없이 테스트 가능하다.
- 엔티티 변경이 잦은 단계에서 부담이 적다.
- H2를 MySQL 모드로 실행해 MySQL과의 차이를 일부 줄일 수 있다.

MySQL은 다음 시점에 붙이는 것이 적절하다.
- 회원가입/로그인/JWT 흐름이 안정된 후
- Study CRUD가 어느 정도 완성된 후
- Docker Compose 또는 로컬 MySQL 연결이 필요해진 후

추후 예상 설정:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/codemate
spring.datasource.username=codemate
spring.datasource.password=비밀번호
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
```

운영 또는 배포 환경에서는 `ddl-auto=validate` 또는 migration 도구 사용을 검토한다.



---
## 2026-06-05 - Study CRUD API 1차 구현

### 배경
회원가입과 공통 응답 구조가 먼저 잡히면서, 이제 실제 서비스의 중심이 되는 모집 글 흐름을 확인할 수 있는 단계가 되었다. 아직 로그인/JWT가 붙기 전이기 때문에 작성자 권한 검증은 토큰 기반이 아니라 요청값의 `hostId`를 기준으로 처리했다. 이 방식은 MVP 개발 중 API 흐름을 빠르게 검증하기 위한 임시 구조이며, JWT 구현 후에는 인증된 사용자 ID로 대체할 예정이다.

### 구현 내용
- **`src/main/java/com/codemate/domain/study/controller/StudyController.java`**
  - `POST /api/studies`: 스터디/모각코 모집 글 생성 API 추가.
  - `GET /api/studies`: 모집 글 목록 조회 API 추가. `category`, `status`, `page`, `size` 조건을 받을 수 있게 구성.
  - `GET /api/studies/{studyId}`: 모집 글 상세 조회 API 추가.
  - `PATCH /api/studies/{studyId}`: 모집 글 수정 API 추가. 현재는 요청 body의 `hostId`로 방장 여부를 검증.
  - `DELETE /api/studies/{studyId}?hostId=`: 모집 글 삭제 API 추가. 현재는 query parameter의 `hostId`로 방장 여부를 검증.

- **`src/main/java/com/codemate/domain/study/service/StudyService.java`**
  - Study 생성, 목록 조회, 상세 조회, 수정, 삭제 비즈니스 로직 추가.
  - 존재하지 않는 회원으로 글 생성 시 `USER_NOT_FOUND` 예외 처리.
  - 존재하지 않는 모집 글 조회/수정/삭제 시 `STUDY_NOT_FOUND` 예외 처리.
  - 방장이 아닌 사용자가 수정/삭제를 시도하면 `FORBIDDEN_STUDY_HOST` 예외 처리.
  - 모집 정원을 현재 인원보다 작게 수정하지 못하도록 `INVALID_STUDY_CAPACITY` 검증 추가.

- **`src/main/java/com/codemate/domain/study/dto/`**
  - `StudyCreateRequest`: 모집 글 생성 요청 DTO 추가.
  - `StudyUpdateRequest`: 모집 글 수정 요청 DTO 추가.
  - `StudyResponse`: 상세 응답 DTO 추가.
  - `StudySummaryResponse`: 목록 응답 DTO 추가.

- **`src/main/java/com/codemate/domain/study/entity/Study.java`**
  - 모집 글 수정을 위한 `update()` 메서드 추가.
  - 방장 여부 확인을 위한 `isHostedBy()` 메서드 추가.

- **`src/main/java/com/codemate/domain/study/repository/StudyRepository.java`**
  - 상태별 목록 조회 메서드 추가.
  - 카테고리별 목록 조회 메서드 추가.
  - 카테고리 + 상태 조합 목록 조회 메서드 추가.

- **`src/main/java/com/codemate/global/response/PageResponse.java`**
  - Spring Data `PageImpl`을 그대로 응답하지 않도록 공통 페이지 응답 DTO 추가.
  - 목록 응답 구조를 `items`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last` 형태로 고정.

- **`src/main/java/com/codemate/global/exception/ErrorCode.java`**
  - Study CRUD에서 필요한 에러 코드를 추가.
  - 추가 코드: `INVALID_STUDY_CAPACITY`, `USER_NOT_FOUND`, `STUDY_NOT_FOUND`, `FORBIDDEN_STUDY_HOST`

- **`src/main/java/com/codemate/global/security/SecurityConfig.java`**
  - JWT 구현 전 Postman 테스트가 가능하도록 `/api/studies/**`를 임시 허용.
  - 이후 JWT 인증 필터가 붙으면 생성/수정/삭제 요청은 인증 필수로 전환할 예정.

### API 확인 예시

스터디 생성:

```http
POST /api/studies
```

```json
{
  "hostId": 1,
  "title": "Spring Boot 스터디",
  "content": "매주 프로젝트 코드를 리뷰합니다.",
  "category": "STUDY",
  "meetingType": "ONLINE",
  "location": "Discord",
  "maxMemberCount": 4
}
```

스터디 목록 조회:

```http
GET /api/studies?category=STUDY&status=RECRUITING&page=0&size=10
```

스터디 수정:

```http
PATCH /api/studies/1
```

```json
{
  "hostId": 1,
  "title": "Spring Boot 심화 스터디",
  "content": "JPA와 Security까지 함께 다룹니다.",
  "category": "STUDY",
  "meetingType": "OFFLINE",
  "location": "강남",
  "maxMemberCount": 5,
  "status": "RECRUITING"
}
```

스터디 삭제:

```http
DELETE /api/studies/1?hostId=1
```

### 테스트
`CodeMateApplicationTests`에 Study CRUD 흐름 테스트를 추가했다.

- 테스트용 방장 회원 생성
- 모집 글 생성
- 모집 글 목록 조회
- 모집 글 상세 조회
- 모집 글 수정
- 모집 글 삭제

최종 테스트 결과:

```text
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 남은 이슈

- 현재 Study 생성/수정/삭제는 `hostId`를 요청값으로 받는 임시 구조다.
- JWT 구현 후 `hostId`는 클라이언트가 보내지 않고, 서버가 토큰에서 로그인 사용자 ID를 꺼내 검증하는 방식으로 변경해야 한다.
- Study와 TechStack 연결은 엔티티/Repository만 준비되어 있고, 생성/수정 API에는 아직 반영하지 않았다.

---

## 13. 다음 작업 후보

우선순위 기준 다음 작업은 다음과 같다.

1. 로그인 API 구현
2. JWT 의존성 추가
3. JWT 발급 및 인증 필터 구현
4. 인증된 사용자 정보 조회 유틸 구성
5. Study 생성/수정/삭제의 `hostId` 요청값 제거
6. Study API 권한 검증을 JWT 기반으로 전환
7. Study와 TechStack 연결 로직 추가
8. StudyMember 참여 신청 API 구현
9. 방장 승인/거절 API 구현
10. Swagger 문서화 추가

다음 개발 단계에서는 로그인/JWT를 붙이는 것이 가장 자연스럽다. 지금 Study CRUD는 기능 흐름을 먼저 확인하기 위한 1차 구현이고, 권한 검증을 제대로 세우려면 인증된 사용자 정보가 필요하다.

---

## 2026-06-05 - 로그인 및 JWT 인증 1차 구현

### 배경

Study CRUD까지 구현되면서 작성자 권한 검증의 필요성이 커졌다. 이전 단계에서는 JWT가 없어서 Study 생성/수정/삭제 요청에 `hostId`를 직접 받는 임시 방식을 사용했다. 이번 작업에서는 로그인 API와 JWT 인증 필터를 먼저 붙여, 이후 Study API의 권한 검증을 토큰 기반으로 전환할 수 있는 기반을 마련했다.

### 구현 내용

- **`pom.xml`**
  - JWT 발급과 검증을 위해 `jjwt-api`, `jjwt-impl`, `jjwt-jackson` 의존성을 추가.

- **`src/main/resources/application.properties`**
  - JWT 서명용 secret 설정 추가.
  - access token 유효 시간 설정 추가.
  - 현재 유효 시간은 1시간(`3600000ms`)으로 설정.

- **`src/main/java/com/codemate/global/security/JwtTokenProvider.java`**
  - 로그인 성공 시 access token 생성.
  - 토큰 subject에는 사용자 email 저장.
  - claim에는 `userId`, `nickname` 저장.
  - 요청 토큰에서 email을 추출하고 서명을 검증하는 로직 추가.

- **`src/main/java/com/codemate/global/security/JwtAuthenticationFilter.java`**
  - 요청 헤더의 `Authorization: Bearer {token}` 값을 읽어 JWT 검증.
  - 토큰이 유효하면 SecurityContext에 인증 객체 저장.
  - 토큰이 없거나 유효하지 않으면 인증 없이 다음 필터로 넘김.

- **`src/main/java/com/codemate/global/security/CustomUserDetails.java`**
  - Spring Security에서 사용할 사용자 인증 객체 추가.
  - 사용자 id, email, nickname, role 정보를 SecurityContext에서 다룰 수 있게 구성.

- **`src/main/java/com/codemate/global/security/CustomUserDetailsService.java`**
  - email 기준으로 사용자를 조회하는 UserDetailsService 추가.
  - JWT 필터에서 토큰 subject로 사용자 정보를 다시 조회할 때 사용.

- **`src/main/java/com/codemate/global/security/SecurityConfig.java`**
  - 세션 정책을 `STATELESS`로 변경.
  - JWT 필터를 `UsernamePasswordAuthenticationFilter` 앞에 등록.
  - `/api/users/signup`, `/api/users/login`, `/api/studies/**`, `/h2-console/**`는 임시 허용.
  - 그 외 API는 인증 필요로 유지.

- **`src/main/java/com/codemate/domain/user/dto/`**
  - `LoginRequest`: 로그인 요청 DTO 추가.
  - `LoginResponse`: 로그인 응답 DTO 추가. `tokenType`, `accessToken` 반환.
  - `UserInfoResponse`: 현재 로그인 사용자 정보 응답 DTO 추가.

- **`src/main/java/com/codemate/domain/user/service/UserService.java`**
  - 로그인 로직 추가.
  - email로 사용자 조회 후 BCrypt로 비밀번호 검증.
  - 로그인 실패 시 `INVALID_LOGIN` 예외 반환.
  - 로그인 성공 시 JWT access token 발급.
  - 현재 사용자 정보 조회 로직 추가.

- **`src/main/java/com/codemate/domain/user/controller/UserController.java`**
  - `POST /api/users/login`: 로그인 API 추가.
  - `GET /api/users/me`: JWT 인증 확인용 내 정보 조회 API 추가.
  - 기존 회원가입 응답 메시지의 깨진 문자열도 정상 한글 메시지로 수정.

- **`src/main/java/com/codemate/global/exception/ErrorCode.java`**
  - `INVALID_LOGIN`: 로그인 실패 에러 코드 추가.
  - `INVALID_TOKEN`: 토큰 검증 실패 상황을 위한 에러 코드 추가.

### API 확인 예시

로그인:

```http
POST /api/users/login
```

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

응답 예시:

```json
{
  "success": true,
  "message": "로그인이 완료되었습니다.",
  "data": {
    "tokenType": "Bearer",
    "accessToken": "jwt-token-value"
  }
}
```

내 정보 조회:

```http
GET /api/users/me
Authorization: Bearer jwt-token-value
```

응답 예시:

```json
{
  "success": true,
  "message": "내 정보를 조회했습니다.",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "nickname": "codemate",
    "mainTechStack": "Spring Boot",
    "role": "ROLE_USER"
  }
}
```

### 테스트

`CodeMateApplicationTests`에 로그인/JWT 흐름 테스트를 추가했다.

- 회원가입으로 테스트 사용자 생성
- 로그인 요청
- access token 발급 확인
- 발급받은 토큰을 `Authorization` 헤더에 넣어 `/api/users/me` 호출
- 응답 사용자 email, nickname 검증

최종 테스트 결과:

```text
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 남은 이슈
- 현재 JWT secret은 로컬 개발용으로 `application.properties`에 직접 들어가 있다.
- 실제 배포 전에는 환경 변수 또는 별도 profile 설정으로 분리해야 한다.
- 유효하지 않은 토큰에 대한 응답 형식은 아직 Spring Security 기본 응답에 가깝다.
- 이후 `AuthenticationEntryPoint`, `AccessDeniedHandler`를 추가하면 인증/인가 실패도 `ApiResponse` 형식으로 통일할 수 있다.
- Study API 생성/수정/삭제는 JWT 기반으로 전환 완료.

---

## 2026-06-05 - Study API JWT 권한 구조 적용

### 1. Study 요청 DTO 정리

- `StudyCreateRequest`에서 `hostId` 제거.
- `StudyUpdateRequest`에서 `hostId` 제거.
- 생성/수정 요청값은 모집 글 자체의 입력값만 받도록 정리.
- 깨져 있던 검증 메시지를 정상 한글 메시지로 수정.

### 2. Study Controller 인증 사용자 적용

- `@AuthenticationPrincipal CustomUserDetails` 적용.
- `POST /api/studies`에서 로그인 사용자 ID를 작성자 ID로 사용.
- `PATCH /api/studies/{studyId}`에서 로그인 사용자 ID로 방장 권한 검증.
- `DELETE /api/studies/{studyId}`에서 query parameter `hostId` 제거.
- 깨져 있던 API 응답 메시지를 정상 한글 메시지로 수정.

### 3. Study Service 권한 검증 변경

- `createStudy(userId, request)` 구조로 변경.
- `updateStudy(userId, studyId, request)` 구조로 변경.
- `deleteStudy(userId, studyId)` 구조로 변경.
- 기존 요청값 `hostId` 대신 JWT 인증 사용자 ID로 방장 여부 검증.
- 내부 권한 검증 메서드 파라미터를 `userId`로 정리.

### 4. Security 접근 제어 변경

- `GET /api/studies/**`는 비로그인 사용자도 조회 가능.
- `POST /api/studies`는 로그인 사용자만 가능.
- `PATCH /api/studies/{studyId}`는 로그인 사용자만 가능.
- `DELETE /api/studies/{studyId}`는 로그인 사용자만 가능.
- `/api/studies/**` 전체 임시 허용 제거.

### 5. 테스트 코드 정리

- Study CRUD 테스트에서 테스트용 회원가입 및 로그인 흐름 추가.
- 로그인 응답에서 JWT access token 추출.
- Study 생성/수정/삭제 요청에 `Authorization: Bearer {token}` 헤더 추가.
- Study 생성/수정 요청 body에서 `hostId` 제거.
- Study 삭제 요청 query parameter `hostId` 제거.
- 깨져 있던 테스트 문자열을 정상 한글 문자열로 정리.

### 6. 테스트 결과

```text
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 7. 다음 작업 후보

1. 인증/인가 실패 응답 `ApiResponse` 형식 통일
2. Study와 TechStack 연결 로직 추가
3. StudyMember 참여 신청 API 구현
4. 방장 승인/거절 API 구현
5. Swagger 문서화 추가

---

## 2026-06-05 - StudyMember 참여 신청 API 구현

### 1. 참여 신청 API 추가

- `POST /api/studies/{studyId}/members` 추가.
- 로그인 사용자만 참여 신청 가능.
- 요청 Body 없이 JWT 인증 정보와 `studyId`로 신청 처리.

### 2. 참여 신청 응답 DTO 추가

- `StudyMemberResponse` 추가.
- 응답 필드 구성:
  - `id`
  - `studyId`
  - `userId`
  - `userNickname`
  - `status`
  - `createdAt`

### 3. 참여 신청 비즈니스 로직 추가

- `StudyMemberService` 추가.
- 신청자 회원 조회.
- 스터디 조회.
- 참여 신청 상태는 `PENDING`으로 저장.
- `study_members` 테이블에 신청 이력 저장.

### 4. 참여 신청 검증 추가

- 본인이 만든 스터디에는 신청 불가.
- 이미 신청한 스터디에는 중복 신청 불가.
- `RECRUITING` 상태의 스터디에만 신청 가능.
- 현재 승인 인원이 모집 정원에 도달한 경우 신청 불가.

### 5. Study 엔티티 메서드 추가

- `isRecruiting()` 추가.
- `isFull()` 추가.

### 6. ErrorCode 정리

- 깨져 있던 한글 메시지 복구.
- 참여 신청 관련 에러 코드 추가:
  - `DUPLICATE_STUDY_APPLICATION`
  - `CANNOT_APPLY_OWN_STUDY`
  - `STUDY_NOT_RECRUITING`
  - `STUDY_CAPACITY_FULL`

### 7. 테스트 추가

- 방장 회원가입.
- 신청자 회원가입.
- 방장 로그인.
- 신청자 로그인.
- 방장 토큰으로 스터디 생성.
- 신청자 토큰으로 참여 신청.
- 신청 상태 `PENDING` 검증.
- 같은 신청자로 중복 신청 시 409 응답 검증.

### 8. Postman 실행 가이드 추가

- `documents/Postman_실행_가이드.md` 생성.
- 회원가입 테스트 방법 정리.
- 로그인 및 JWT 저장 방법 정리.
- 내 정보 조회 방법 정리.
- Study CRUD 테스트 방법 정리.
- StudyMember 참여 신청 테스트 방법 정리.
- H2 Console 확인 방법 정리.

### 9. 테스트 결과

```text
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 10. 다음 작업 후보

1. 방장 승인/거절 API 구현
2. 승인 시 `currentMemberCount` 증가 처리
3. 정원 도달 시 Study 상태 `CLOSED` 자동 변경
4. 신청 목록 조회 API 구현
5. 인증/인가 실패 응답 `ApiResponse` 형식 통일

---

## 2026-06-05 - StudyMember 승인/거절 API 구현

### 1. 승인 API 추가

- `PATCH /api/studies/{studyId}/members/{memberId}/approve` 추가.
- 방장만 승인 가능.
- `PENDING` 상태의 신청만 승인 가능.
- 승인 시 `StudyMemberStatus.APPROVED`로 변경.

### 2. 거절 API 추가

- `PATCH /api/studies/{studyId}/members/{memberId}/reject` 추가.
- 방장만 거절 가능.
- `PENDING` 상태의 신청만 거절 가능.
- 거절 시 `StudyMemberStatus.REJECTED`로 변경.

### 3. Study 인원 처리 추가

- 승인 시 `currentMemberCount` 1 증가.
- `currentMemberCount >= maxMemberCount`가 되면 Study 상태를 `CLOSED`로 변경.
- 거절 시 `currentMemberCount`는 변경하지 않음.

### 4. 엔티티 메서드 추가

- `Study.increaseCurrentMemberCount()` 추가.
- `StudyMember.isPending()` 추가.
- `StudyMember.approve()` 추가.
- `StudyMember.reject()` 추가.

### 5. 상태 검증 추가

- 신청 내역이 없으면 `STUDY_MEMBER_NOT_FOUND`.
- 방장이 아니면 `FORBIDDEN_STUDY_HOST`.
- 대기 상태가 아니면 `INVALID_STUDY_MEMBER_STATUS`.
- 정원이 이미 찼으면 `STUDY_CAPACITY_FULL`.
- 다른 스터디의 신청 ID를 처리하려고 하면 `STUDY_MEMBER_NOT_FOUND`.

### 6. 테스트 추가

- 승인 테스트 추가.
- 승인 후 `currentMemberCount` 증가 검증.
- 정원 도달 후 Study 상태 `CLOSED` 변경 검증.
- 거절 테스트 추가.
- 거절 후 `currentMemberCount` 유지 검증.
- 거절 후 Study 상태 `RECRUITING` 유지 검증.

### 7. Postman 실행 가이드 업데이트

- 참여 신청 승인 테스트 방법 추가.
- 참여 신청 거절 테스트 방법 추가.
- 승인/거절 실패 케이스 추가.
- 추천 테스트 순서에 `memberId` 저장과 방장 토큰 교체 과정 추가.

### 8. 테스트 결과

```text
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 9. 다음 작업 후보

1. 신청 목록 조회 API 구현
2. 인증/인가 실패 응답 `ApiResponse` 형식 통일
3. Study와 TechStack 연결 로직 추가
4. Swagger 문서화 추가

---

## 2026-06-05 - StudyMember 신청 목록 조회 API 구현

### 1. 신청 목록 조회 API 추가

- `GET /api/studies/{studyId}/members` 추가.
- 스터디 참여 신청 목록을 배열 형태로 반환.
- 응답 DTO는 기존 `StudyMemberResponse`를 재사용.

### 2. status 필터 추가

- Query Parameter로 `status`를 받을 수 있게 구성.
- 전체 조회:

```http
GET /api/studies/{studyId}/members
```

- 상태별 조회:

```http
GET /api/studies/{studyId}/members?status=PENDING
```

- 사용 가능한 값:
  - `PENDING`
  - `APPROVED`
  - `REJECTED`

### 3. 방장 권한 검증

- JWT 인증 사용자 ID로 스터디 방장 여부 검증.
- 방장이 아닌 사용자가 신청 목록을 조회하면 `FORBIDDEN_STUDY_HOST` 예외 반환.
- 존재하지 않는 스터디는 `STUDY_NOT_FOUND` 예외 반환.

### 4. Repository 조회 메서드 추가

- `findAllByStudy(Study study)` 추가.
- `findAllByStudyAndStatus(Study study, StudyMemberStatus status)` 추가.
- status가 없으면 전체 목록 조회.
- status가 있으면 해당 상태의 신청만 조회.

### 5. Controller 메시지 정리

- 신청 목록 조회 성공 메시지 추가.
- 참여 신청, 승인, 거절 응답 메시지를 정상 한글 문장으로 정리.

### 6. 테스트 추가

- 방장 회원가입.
- 신청자 회원가입.
- 방장 로그인.
- 신청자 로그인.
- 방장 토큰으로 스터디 생성.
- 신청자 토큰으로 참여 신청.
- 방장 토큰으로 신청 목록 조회.
- 응답 배열의 첫 번째 신청 상태가 `PENDING`인지 검증.
- 신청자 닉네임이 응답에 포함되는지 검증.

### 7. Postman 실행 가이드 업데이트

- `memberId` 환경 변수 추가.
- 신청 목록 조회 테스트 순서 추가.
- 전체 목록 조회 예시 추가.
- status 필터 조회 예시 추가.
- 방장만 조회 가능하다는 권한 규칙 추가.

### 8. 테스트 결과

```text
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 9. 다음 작업 후보

1. 인증/인가 실패 응답 `ApiResponse` 형식 통일
2. Study와 TechStack 연결 로직 추가
3. Swagger 문서화 추가
4. MySQL profile 분리

---

## 2026-06-05 - 인증/인가 실패 응답 ApiResponse 형식 통일

### 1. 인증 실패 응답 핸들러 추가

- `CustomAuthenticationEntryPoint` 추가.
- 인증이 필요한 API를 토큰 없이 호출하면 401 응답 반환.
- 응답 형식은 공통 `ApiResponse` 구조와 동일하게 맞춤.

```json
{
  "success": false,
  "message": "로그인이 필요합니다."
}
```

### 2. 잘못된 JWT 응답 처리

- `JwtAuthenticationFilter`에서 토큰 검증 실패 시 request attribute에 `INVALID_TOKEN` 저장.
- 인증 실패 핸들러가 해당 값을 읽어 메시지 분기.

```json
{
  "success": false,
  "message": "유효하지 않은 토큰입니다."
}
```

### 3. 인가 실패 응답 핸들러 추가

- `CustomAccessDeniedHandler` 추가.
- Spring Security 레벨의 403 응답도 공통 응답 형식으로 반환.

```json
{
  "success": false,
  "message": "접근 권한이 없습니다."
}
```

### 4. SecurityConfig 예외 처리 연결

- `exceptionHandling` 설정 추가.
- `authenticationEntryPoint` 연결.
- `accessDeniedHandler` 연결.

### 5. 신청 목록 조회 인증 규칙 보정

- `GET /api/studies/**` 공개 설정 때문에 신청 목록 조회까지 공개될 수 있던 구조를 보정.
- `GET /api/studies/*/members`는 인증 필요 API로 먼저 매칭되도록 순서 조정.
- 일반 스터디 목록/상세 조회는 기존처럼 비로그인 조회 가능.

### 6. ErrorCode 추가

- `UNAUTHORIZED`: 로그인이 필요한 요청.
- `ACCESS_DENIED`: 접근 권한이 없는 요청.
- 기존 `INVALID_TOKEN`은 잘못된 JWT 응답에 사용.

### 7. 테스트 추가

- 토큰 없이 `POST /api/studies` 요청 시 401 응답 검증.
- 잘못된 토큰으로 `GET /api/users/me` 요청 시 401 응답 검증.
- 신청자 토큰으로 신청 목록 조회 시 403 응답 검증.
- 모든 실패 응답의 `success=false`, `message` 값 검증.

### 8. Postman 실행 가이드 업데이트

- 인증 실패 응답 확인 섹션 추가.
- 토큰 없음 예시 추가.
- 잘못된 토큰 예시 추가.
- 방장이 아닌 사용자 접근 예시 추가.

### 9. 테스트 결과

```text
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 10. 다음 작업 후보

1. Study와 TechStack 연결 로직 추가
2. Swagger 문서화 추가
3. MySQL profile 분리
4. 테스트용 SQL 로그 정리

---

## 2026-06-05 - Study와 TechStack 연결 로직 구현

### 1. Study 요청 DTO 확장

- `StudyCreateRequest`에 `techStackNames` 추가.
- `StudyUpdateRequest`에 `techStackNames` 추가.
- 기술 스택은 최대 10개까지 입력 가능.
- 기술 스택 이름은 비어 있을 수 없고 50자 이하로 제한.

### 2. Study 응답 DTO 확장

- `StudyResponse`에 `techStackNames` 추가.
- `StudySummaryResponse`에 `techStackNames` 추가.
- 생성, 목록, 상세, 수정 응답에서 기술 스택 이름 목록 반환.

### 3. 기술 스택 생성/재사용 로직 추가

- 요청으로 들어온 기술 스택 이름을 trim 처리.
- 중복 기술 스택 이름 제거.
- 기존 `TechStack`이 있으면 재사용.
- 기존 `TechStack`이 없으면 새로 생성.

### 4. StudyTechStack 연결 저장

- Study 생성 시 `study_tech_stacks` 연결 데이터 저장.
- Study 수정 시 기존 연결을 삭제하고 요청값 기준으로 다시 저장.
- 수정 시 unique constraint 충돌을 피하기 위해 삭제 후 flush 처리.

### 5. Study 삭제 보강

- Study 삭제 전에 `study_tech_stacks` 연결 데이터 먼저 삭제.
- Study와 TechStack 사이의 FK 제약으로 인한 삭제 실패를 방지.

### 6. 테스트 보강

- Study 생성 응답의 `techStackNames` 검증.
- Study 목록 응답의 `techStackNames` 검증.
- Study 상세 응답의 `techStackNames` 검증.
- Study 수정 후 기술 스택 목록이 교체되는지 검증.

### 7. Postman 실행 가이드 업데이트

- 스터디 생성 Body에 `techStackNames` 추가.
- 스터디 생성 응답 예시에 `techStackNames` 추가.
- 스터디 수정 Body에 `techStackNames` 추가.
- 수정 시 기존 기술 스택 연결이 교체된다는 규칙 추가.

### 8. 테스트 결과

```text
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 9. 다음 작업 후보

1. 기술 스택 기반 Study 검색 필터 추가
2. Swagger 문서화 추가
3. MySQL profile 분리
4. 테스트용 SQL 로그 정리

---

## 2026-06-07 - Study 복합 검색 필터 구현

### 1. 검색 조건 확장

- `GET /api/studies`에서 다음 Query Parameter를 조합해 사용할 수 있도록 확장.
  - `keyword`: 제목 또는 내용 부분 검색
  - `category`: `STUDY`, `MOGAKKO`
  - `status`: `RECRUITING`, `CLOSED`, `IN_PROGRESS`, `FINISHED`
  - `meetingType`: `ONLINE`, `OFFLINE`
  - `location`: 지역 부분 검색
  - `techStack`: 기술 스택 이름 부분 검색
  - `page`, `size`: 페이징

### 2. 동적 검색 구조

- `StudySearchCondition`을 추가해 검색 입력값을 하나의 객체로 전달.
- `StudyRepository`에 `JpaSpecificationExecutor` 적용.
- `StudySpecifications`에서 값이 있는 조건만 동적으로 Predicate에 추가.
- 기술 스택 검색은 `StudyTechStack`을 대상으로 한 `exists` 서브쿼리로 처리.
- 빈 문자열 검색 조건은 적용하지 않음.
- 지역과 기술 스택은 대소문자를 구분하지 않는 부분 검색으로 처리.

### 3. 검색 요청 예시

```http
GET /api/studies?keyword=코루틴&category=STUDY&status=RECRUITING&meetingType=OFFLINE&location=판교&techStack=Kotlin&page=0&size=10
```

### 4. 테스트

- 서로 다른 카테고리, 진행 방식, 지역, 기술 스택을 가진 모집 글 생성.
- 키워드, 카테고리, 상태, 진행 방식, 지역, 기술 스택을 모두 조합해 조회.
- 모든 조건을 만족하는 모집 글 하나만 반환되는지 검증.

### 5. 다음 작업 후보

1. Swagger/OpenAPI 문서화
2. 동시 승인 상황의 모집 정원 정합성 보강
3. MySQL profile 분리
4. 목록 조회 N+1 쿼리 최적화

---

## 2026-06-07 - 동시 승인 모집 정원 초과 방지

### 1. 문제 상황

- 모집 가능 인원이 한 자리 남은 상태에서 두 승인 요청이 동시에 실행되면 두 트랜잭션이 같은 `currentMemberCount`를 읽을 수 있었다.
- 기존 로직은 조회 후 애플리케이션에서 정원을 검사했기 때문에, 요청 타이밍에 따라 모집 정원을 초과할 가능성이 있었다.

### 2. 비관적 락 적용

- `StudyRepository.findByIdForUpdate()` 추가.
- `@Lock(LockModeType.PESSIMISTIC_WRITE)`를 적용해 승인 시 Study 행을 쓰기 잠금으로 조회.
- 승인 트랜잭션이 끝날 때까지 동일한 Study의 다른 승인 요청이 기다리도록 처리.
- 대기하던 요청은 첫 승인 커밋 후 최신 인원을 읽고 `STUDY_CAPACITY_FULL` 검증을 수행.

### 3. 적용 범위

- 참여 신청 승인 API에만 비관적 락 적용.
- 일반 목록/상세 조회와 참여 신청, 거절 요청에는 기존 조회 방식을 유지.
- 동일 스터디의 승인 요청만 직렬화되며 서로 다른 스터디 승인은 독립적으로 처리.

### 4. 병렬 통합 테스트

- 방장을 포함해 최대 인원이 2명인 스터디 생성.
- 서로 다른 신청자 두 명이 `PENDING` 상태로 참여 신청.
- 두 스레드가 같은 시점에 각각 승인 로직을 호출.
- 승인 성공 1건과 `STUDY_CAPACITY_FULL` 실패 1건 확인.
- 최종 `currentMemberCount=2`, `status=CLOSED` 확인.
- 실행 SQL에서 `select ... for update` 적용 확인.

### 5. 다음 작업 후보

1. Swagger/OpenAPI 문서화
2. MySQL profile 분리
3. 목록 조회 N+1 쿼리 최적화
4. 테스트 SQL 로그 분리

---

## 2026-06-07 - Swagger/OpenAPI 문서화

### 1. springdoc 의존성 추가

- Spring Boot 4.0.6과 호환되는 `springdoc-openapi-starter-webmvc-ui:3.0.3` 추가.
- Swagger UI와 OpenAPI JSON을 자동 생성하도록 구성.

### 2. OpenAPI 기본 정보 및 JWT 설정

- `OpenApiConfig` 추가.
- 문서 제목: `CodeMate API`
- 문서 버전: `v1`
- HTTP Bearer 방식의 `bearerAuth` 보안 스키마 추가.
- Swagger UI의 `Authorize`에서 access token만 입력하면 JWT Header가 적용되도록 구성.

### 3. Security 공개 경로

- 다음 문서 경로를 인증 없이 접근할 수 있도록 허용.
  - `/swagger-ui/**`
  - `/swagger-ui.html`
  - `/v3/api-docs/**`

### 4. API 설명 추가

- Controller별 태그 추가:
  - `Users`
  - `Studies`
  - `Study Members`
- 각 API에 기능 요약과 상세 설명 추가.
- 주요 성공 및 실패 HTTP 상태 코드 설명 추가.
- 인증이 필요한 API에 `bearerAuth` 보안 요구사항 표시.
- 참여 승인 API에 비관적 락 기반 정원 보호 동작 설명.

### 5. Swagger UI 설정

- 태그 이름 알파벳 정렬.
- 같은 태그의 API를 HTTP Method 기준으로 정렬.

### 6. 테스트

- 비로그인 상태에서 `/v3/api-docs`가 200 응답인지 검증.
- 문서 제목과 `bearerAuth` 설정 검증.
- 로그인, 스터디 목록, 참여 승인 경로가 OpenAPI JSON에 포함되는지 검증.

### 7. 접속 주소

```text
Swagger UI: http://localhost:8080/swagger-ui/index.html
OpenAPI JSON: http://localhost:8080/v3/api-docs
```

### 8. 요청·응답 JSON 예시 문서화

- Swagger UI에서 API를 처음 확인하는 사용자도 별도 문서를 열지 않고 요청 형식을 이해할 수 있도록 JSON 예시를 제공하는 방향으로 정리.
- 회원가입, 로그인, 모집 글 생성·수정처럼 Request Body가 있는 API를 우선 적용 대상으로 선정.
- 단순 성공 응답뿐 아니라 검증 실패, 인증 실패, 권한 실패, 중복 요청과 같은 대표 오류 응답 예시도 함께 제공할 필요가 있음.
- 예시는 실제 공통 응답 구조인 `success`, `message`, `data` 형식을 기준으로 작성.

회원가입 요청 예시:

```json
{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "codemate",
  "mainTechStack": "Spring Boot"
}
```

로그인 성공 응답 예시:

```json
{
  "success": true,
  "message": "로그인이 완료되었습니다.",
  "data": {
    "tokenType": "Bearer",
    "accessToken": "JWT_ACCESS_TOKEN"
  }
}
```

모집 글 생성 요청 예시:

```json
{
  "title": "Spring Boot 스터디",
  "content": "매주 프로젝트 코드를 리뷰합니다.",
  "category": "STUDY",
  "meetingType": "ONLINE",
  "location": "Discord",
  "maxMemberCount": 4,
  "techStackNames": [
    "Java",
    "Spring Boot",
    "JPA"
  ]
}
```

- 실제 코드 보강 시 `@io.swagger.v3.oas.annotations.media.ExampleObject`, `@Content`, `@Schema(example = "...")` 적용을 검토.
- 현재 단계에서는 Controller의 API 설명과 상태 코드가 등록되어 있으며, 구체적인 JSON example annotation은 후속 보강 대상으로 분류.

### 9. DTO 필드별 Schema 설명

- 요청·응답 DTO의 각 필드가 의미하는 값, 입력 제한, 예시를 Swagger Schema에서 확인할 수 있도록 정리.
- Bean Validation의 `@NotBlank`, `@Size`, `@Min`과 OpenAPI의 `@Schema`를 함께 사용해 실행 검증과 문서 설명을 일치시키는 방향.
- enum 필드는 문자열 입력값과 허용 범위를 명확히 표시.

주요 적용 대상:

- `SignupRequest`
  - `email`: 로그인에 사용할 이메일
  - `password`: 8자 이상 비밀번호
  - `nickname`: 서비스에서 표시할 닉네임
  - `mainTechStack`: 사용자의 대표 기술 스택
- `LoginRequest`
  - `email`: 가입된 이메일
  - `password`: 가입 시 등록한 비밀번호
- `StudyCreateRequest`, `StudyUpdateRequest`
  - `title`, `content`: 모집 글 제목과 상세 내용
  - `category`: 스터디 또는 모각코 구분
  - `meetingType`: 온라인 또는 오프라인 진행 방식
  - `location`: 오프라인 장소 또는 온라인 채널
  - `maxMemberCount`: 방장을 포함한 최대 참여 인원
  - `techStackNames`: 최대 10개의 기술 스택 이름
- `StudyResponse`, `StudySummaryResponse`
  - 방장 정보, 모집 인원, 모집 상태, 기술 스택, 생성·수정 시각
- `StudyMemberResponse`
  - 신청 ID, 스터디 ID, 신청자 정보, 신청 상태, 신청 시각
- `LoginResponse`
  - JWT 인증 방식과 access token

적용 형태 예시:

```java
@Schema(
        description = "방장을 포함한 최대 참여 인원",
        example = "4",
        minimum = "2"
)
int maxMemberCount
```

- 현재 DTO에는 Bean Validation이 적용되어 있으나 필드별 `@Schema` 설명은 아직 적용되지 않아 후속 문서화 보강 항목으로 관리.

### 10. 공통 오류 응답 모델 명시

- 모든 오류 응답이 공통 `ApiResponse<Void>` 구조를 사용한다는 점을 OpenAPI 문서에서도 일관되게 표현할 필요가 있음.
- Controller의 응답 코드 설명만으로는 실제 JSON 구조가 충분히 드러나지 않으므로 공통 오류 Schema 또는 전용 문서 DTO 적용을 검토.

공통 오류 응답 구조:

```json
{
  "success": false,
  "message": "오류 메시지"
}
```

대표 오류 응답:

```json
{
  "success": false,
  "message": "로그인이 필요합니다."
}
```

```json
{
  "success": false,
  "message": "스터디 방장만 처리할 수 있습니다."
}
```

```json
{
  "success": false,
  "message": "이미 참여 신청한 스터디입니다."
}
```

주요 상태 코드:

- `400 Bad Request`: 입력값 검증 실패, 잘못된 모집 인원, 처리할 수 없는 신청 상태
- `401 Unauthorized`: 토큰 없음, 만료 또는 유효하지 않은 JWT
- `403 Forbidden`: 방장 권한 없음
- `404 Not Found`: 회원, 스터디 또는 참여 신청 내역 없음
- `409 Conflict`: 이메일·닉네임 중복, 스터디 중복 신청
- `500 Internal Server Error`: 처리되지 않은 서버 오류

- 실제 코드 보강 시 공통 오류 응답 문서 DTO 또는 재사용 가능한 OpenAPI annotation 구성을 검토.
- `CustomAuthenticationEntryPoint`, `CustomAccessDeniedHandler`, `GlobalExceptionHandler`의 실제 응답과 Swagger 예시가 달라지지 않도록 함께 관리.

### 11. enum 값 설명

- Swagger UI에서 enum 필드의 허용 값뿐 아니라 각 값의 의미까지 이해할 수 있도록 설명을 보강할 필요가 있음.

`StudyCategory`:

- `STUDY`: 일정한 목표나 커리큘럼을 가진 스터디
- `MOGAKKO`: 모여서 각자 코딩하는 단기·원데이 모임

`MeetingType`:

- `ONLINE`: Discord, Zoom 등 온라인 공간에서 진행
- `OFFLINE`: 카페, 스터디룸 등 실제 장소에서 진행

`StudyStatus`:

- `RECRUITING`: 참여자를 모집 중인 상태
- `CLOSED`: 정원 도달 또는 방장 결정으로 모집이 마감된 상태
- `IN_PROGRESS`: 모집이 끝나고 활동이 진행 중인 상태
- `FINISHED`: 스터디 또는 모각코 활동이 종료된 상태

`StudyMemberStatus`:

- `PENDING`: 방장 승인 대기
- `APPROVED`: 참여 승인 완료
- `REJECTED`: 참여 신청 거절

`UserRole`:

- `ROLE_USER`: 일반 사용자
- `ROLE_ADMIN`: 관리자

- 실제 코드 보강 시 DTO enum 필드의 `@Schema(description = "...", allowableValues = {...})` 또는 enum 자체의 설명 노출 방식을 검토.

### 12. 문서화 보강 상태 정리

- 2026년 6월 7일 기준 완료:
  - springdoc 의존성 및 Swagger UI 적용
  - OpenAPI 기본 정보 등록
  - JWT Bearer 인증 스키마 적용
  - Controller별 태그, 기능 설명, 주요 HTTP 상태 코드 등록
  - OpenAPI JSON 노출 및 통합 테스트
- 후속 코드 적용 필요:
  - 요청·응답별 구체적인 JSON example annotation
  - DTO 필드별 `@Schema` 설명과 example
  - 공통 오류 응답 Schema의 재사용 구성
  - enum 값별 의미 설명 노출

### 13. 다음 작업 후보

1. Swagger 요청·응답 예시 annotation 적용
2. DTO 및 enum Schema 설명 적용
3. 공통 오류 응답 문서 모델 구성
4. MySQL profile 분리
5. 목록 조회 N+1 쿼리 최적화
6. Dockerfile 및 Docker Compose 구성

---

## 2026-06-07 - Postman 참여 승인 테스트 트러블슈팅

### 1. 테스트 목적

- 방장 계정으로 스터디 생성.
- 신청자 계정으로 참여 신청.
- 방장 계정으로 신청 목록 조회 및 참여 승인.
- JWT 인증, 환경 변수, 참여 신청 ID 전달이 실제 클라이언트에서도 올바르게 동작하는지 확인.

### 2. 발생한 문제

#### 동일 계정으로 신청

- 방장 계정이 계속 로그인된 상태에서 본인 스터디에 참여 신청.
- `본인이 만든 스터디에는 참여 신청할 수 없습니다.` 응답 발생.

원인:

- 로그인 요청 Body를 주석 처리하며 바꾸는 과정에서 실제 로그인 계정이 기대와 달랐다.

해결:

- 로그인 요청을 `방장 로그인 요청`, `신청자 로그인 요청`으로 분리.
- 로그인할 때마다 Post-response에서 `accessToken` 환경 변수를 자동 교체.
- `/api/users/me`로 현재 로그인 계정을 확인.

#### 인증 요청에서 401 발생

- 방장으로 로그인했지만 신청 목록 및 승인 API에서 `로그인이 필요합니다.` 응답 발생.

원인:

- 일부 요청의 Authorization이 `No Auth`로 설정됨.
- Authorization 탭과 Headers 탭에 인증값을 중복 설정.
- 환경 변수 대신 `Bearer {{accessToken}}` 문자열이 그대로 전달될 가능성을 점검해야 했음.

해결:

- Collection에 `Bearer Token {{accessToken}}` 공통 인증 설정.
- 인증 필요 요청은 `Inherit auth from parent` 사용.
- 직접 추가한 Authorization Header 삭제.
- Postman Console에서 실제 Request Header 확인.
- `/api/users/me`가 200인지 먼저 확인해 토큰 문제와 방장 권한 문제를 분리.

#### memberId가 비어 있는 승인 요청

Postman Console:

```text
PATCH /api/studies/7/members//approve
```

원인:

- 이미 참여 신청한 사용자가 다시 신청해 `409 Conflict` 응답 발생.
- 실패 응답에는 `data`가 없지만 Post-response에서 `json.data.id`에 무조건 접근.
- `TypeError: Cannot read properties of undefined (reading 'id')` 발생.
- `memberId` 환경 변수가 저장되지 않아 승인 URL 경로가 비어 있음.

해결:

- 성공 응답일 때만 값을 저장하는 방어적 Post-response 스크립트 적용.

```javascript
const json = pm.response.json();

if (json.success && json.data?.id) {
    pm.environment.set("memberId", json.data.id);
}
```

- 이미 신청된 데이터는 방장 토큰으로 `GET /api/studies/{studyId}/members?status=PENDING` 조회.
- 목록의 첫 번째 신청 ID를 `memberId`로 저장.

```javascript
const json = pm.response.json();

if (json.success && Array.isArray(json.data) && json.data.length > 0) {
    pm.environment.set("memberId", json.data[0].id);
}
```

#### Query Parameter에 줄바꿈 포함

Postman Console:

```text
GET /api/studies/7/members?status=PENDING%0A
```

원인:

- Params 값 끝에 보이지 않는 줄바꿈 문자가 포함됨.

해결:

- Params 탭에서 값을 삭제하고 `PENDING`을 다시 직접 입력.

### 3. 최종 성공 흐름

1. 방장 로그인 후 `accessToken` 저장.
2. 방장 토큰으로 스터디 생성 후 `studyId` 저장.
3. 신청자 로그인으로 토큰 교체.
4. 신청자 토큰으로 참여 신청 후 `memberId` 저장.
5. 방장 로그인으로 토큰 교체.
6. `/api/users/me`로 방장 계정 확인.
7. 방장 토큰으로 신청 목록 조회.
8. `PATCH /api/studies/{studyId}/members/{memberId}/approve` 실행.
9. HTTP 200, `status=APPROVED` 확인.

최종 확인 값:

```text
studyId=7
memberId=3
status=APPROVED
```

### 4. 정리

- `401`은 JWT 전달 문제, `403`은 방장 권한 문제로 구분해서 확인한다.
- 환경 변수는 실패 응답에서 덮어쓰지 않도록 성공 조건을 검사한다.
- 동적 URL에 사용되는 `studyId`, `memberId`는 Console의 실제 요청 URL로 검증한다.
- 계정 전환이 많은 테스트는 요청을 계정별로 분리해 실수를 줄인다.


---
## 2026-06-08 - Swagger 스터디 목록 조회 401 오류 트러블슈팅

### 1. 문제 상황

- Swagger UI에서 `GET /api/studies` 모집 글 목록 및 검색 API 실행.
- 스터디 목록 조회는 공개 API이지만 HTTP `401 Unauthorized` 응답 발생.
- 응답 Body에 `로그인이 필요합니다.` 메시지 표시.
- Swagger UI가 다음과 같은 요청 URL을 자동 생성.

```text
GET /api/studies?keyword=Java&page=0&size=1&sort=%5B%22string%22%5D
```

- 디코딩한 `sort` 값:

```text
sort=["string"]
```

### 2. 원인 분석

1. 공개 API 인증 설정 확인
   - `/api/studies`의 `GET` 요청은 Spring Security에서 `permitAll()`로 설정된 상태.
   - 정상적인 페이징 요청을 인증 없이 직접 실행했을 때 HTTP `200 OK` 확인.
   - 로그인이나 JWT가 필요한 API로 잘못 설정된 문제는 아니었음.

2. Swagger의 `Pageable` 문서 생성 방식 확인
   - Controller의 `Pageable` 파라미터가 Swagger에서 하나의 필수 JSON 객체로 표시됨.
   - Swagger 예시값의 `sort`에 `["string"]`이 자동으로 포함됨.
   - Spring Data가 기대하는 `sort=필드명,정렬방향` 형식과 일치하지 않는 요청 생성.

3. 오류 응답이 401로 표시된 이유 확인
   - 잘못된 정렬 조건으로 요청 처리 중 예외 발생.
   - Spring Boot가 예외 처리를 위해 `/error` 경로로 ERROR 디스패치 수행.
   - Security 설정에서 ERROR 디스패치가 허용되지 않아 인증 실패 처리 실행.
   - 실제 원인은 잘못된 `sort` 값이지만 최종 응답은 `로그인이 필요합니다.`라는 401로 표시됨.

### 3. 해결 방법

1. Swagger 페이징 파라미터 분리
   - `StudyController`의 `Pageable` 파라미터에 `@ParameterObject` 적용.
   - Swagger UI에서 `pageable` JSON 객체 제거.
   - `page`, `size`, `sort`를 각각 독립된 Query Parameter로 표시.

```java
@ParameterObject
@PageableDefault(size = 10)
Pageable pageable
```

2. ERROR 디스패치 허용
   - Spring Security 설정에 `DispatcherType.ERROR` 허용 규칙 추가.
   - 애플리케이션 처리 오류가 인증 오류인 것처럼 401로 변환되는 현상 방지.

```java
.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
```

3. Swagger 입력 방법 정리
   - 정렬이 필요하지 않으면 `sort` 입력값을 비워서 요청.
   - 정렬이 필요하면 다음 형식 사용.

```text
sort=createdAt,desc
sort=title,asc
```

### 4. 검증 결과

1. OpenAPI 문서 확인
   - `/api/studies` Query Parameter에 `page`, `size`, `sort`가 각각 생성되는지 확인.
   - 잘못 생성되던 `pageable` 객체가 제거되었는지 확인.

2. 공개 API 확인
   - JWT 없이 `GET /api/studies?keyword=Java&page=0&size=1` 실행.
   - HTTP `200 OK` 및 공통 성공 응답 반환 확인.

3. 회귀 테스트 추가
   - OpenAPI 문서의 페이징 파라미터 구성 검증.
   - 비로그인 상태의 스터디 목록 조회 성공 검증.
   - Maven 전체 테스트 `13개` 통과.

### 5. 정리

- 화면에 표시된 HTTP 상태와 메시지만으로 인증 문제라고 단정하지 않고 실제 Request URL을 먼저 확인.
- Swagger가 자동 생성한 예시값도 서버가 기대하는 형식과 일치하는지 검증 필요.
- 공개 API의 정상 응답뿐 아니라 오류 처리 경로의 Security 설정도 함께 확인.
- `Pageable`을 OpenAPI에 노출할 때 `@ParameterObject`를 적용해 실제 Query Parameter 구조와 문서를 일치.


---
## 2026-06-08 - 참여 신청 조회 및 재신청 기능 보강

### 1. 모집 글 삭제 연관 데이터 처리

1. 문제 확인
   - 참여 신청이 존재하는 모집 글 삭제 시 `DataIntegrityViolationException` 발생.
   - `study_members.study_id` 외래키가 삭제 대상 스터디를 참조해 HTTP `500` 반환.

2. 삭제 순서 변경
   - 참여 신청 내역 삭제.
   - 스터디 기술 스택 연결 삭제.
   - Repository `flush()`로 하위 데이터 삭제 SQL 우선 반영.
   - 마지막으로 스터디 모집 글 삭제.

3. 검증
   - 참여 신청이 존재하는 스터디 삭제 통합 테스트 추가.
   - 삭제 후 상세 조회 시 `404 Not Found` 확인.

### 2. 내 스터디 신청 내역 조회 API

1. API 추가

```http
GET /api/users/me/study-applications
```

2. 상태별 필터 지원

```http
GET /api/users/me/study-applications?status=PENDING
GET /api/users/me/study-applications?status=APPROVED
GET /api/users/me/study-applications?status=REJECTED
```

3. 응답 정보
   - `applicationId`: 참여 신청 ID.
   - `studyId`: 스터디 모집 글 ID.
   - `studyTitle`: 스터디 모집 글 제목.
   - `hostNickname`: 방장 닉네임.
   - `studyStatus`: 모집 글 진행 상태.
   - `applicationStatus`: 신청 처리 상태.
   - `appliedAt`: 최초 신청 또는 최근 재신청 시각.

4. 조회 정책
   - 로그인 사용자 본인의 신청 내역만 조회.
   - 최신 신청 또는 재신청 시각 기준 내림차순 정렬.
   - 상태 조건이 없으면 전체 신청 내역 반환.

### 3. 거절된 스터디 재신청

1. 기존 문제
   - `(study_id, user_id)` 유니크 제약과 중복 신청 검사로 `REJECTED` 상태도 재신청 불가.

2. 재신청 정책
   - `REJECTED`: 기존 신청 행의 상태를 `PENDING`으로 변경.
   - `PENDING`: 중복 신청으로 `409 Conflict`.
   - `APPROVED`: 중복 신청으로 `409 Conflict`.
   - 모집 중이 아니거나 정원이 가득 찬 스터디는 재신청 불가.

3. 데이터 처리
   - 새로운 신청 행을 생성하지 않고 기존 `memberId` 유지.
   - `updatedAt` 갱신으로 최근 재신청 시각 관리.
   - 신청 이력 조회의 `appliedAt`에 최근 재신청 시각 사용.

4. 검증
   - 신청 → 거절 → 재신청 시 `PENDING` 전환 확인.
   - 재신청 후 같은 사용자의 추가 신청은 `409 Conflict` 확인.
   - 기존 `memberId`가 유지되는지 확인.

### 4. Swagger 및 Security 보완

1. 스터디 목록의 `Pageable`에 `@ParameterObject` 적용.
2. Swagger Query Parameter를 `page`, `size`, `sort`로 분리.
3. Spring Security의 `DispatcherType.ERROR` 허용.
4. 잘못된 요청 처리 중 발생한 오류가 인증 오류 `401`로 표시되는 현상 방지.
5. 재신청 정책에 맞춰 참여 신청 API의 `409` 설명 수정.

### 5. 테스트 결과

1. 공개 스터디 목록 비로그인 조회 테스트.
2. OpenAPI 페이징 파라미터 구성 테스트.
3. 참여 신청이 존재하는 스터디 삭제 테스트.
4. 내 신청 내역 전체 및 상태별 조회 테스트.
5. 거절 후 재신청 및 중복 신청 차단 테스트.
6. Maven 전체 테스트 `16개` 통과.


---
## 2026-06-08 - H2/MySQL 프로필 분리

### 1. 공통 설정 분리

1. `application.properties`
   - 애플리케이션 이름.
   - 기본 활성 프로필 `h2`.
   - JPA 공통 설정.
   - JWT 설정.
   - Swagger UI 설정.

2. 기본 프로필

```properties
spring.profiles.default=h2
```

- 별도 프로필 없이 실행하면 기존과 동일하게 H2 인메모리 DB 사용.
- 테스트와 빠른 로컬 개발 흐름 유지.

### 2. H2 프로필

설정 파일:

```text
src/main/resources/application-h2.properties
```

주요 설정:

1. H2 인메모리 데이터베이스 사용.
2. MySQL 호환 모드 적용.
3. H2 Console 활성화.
4. Hibernate `ddl-auto=update` 적용.

실행:

```powershell
.\mvnw.cmd spring-boot:run
```

### 3. MySQL 프로필

설정 파일:

```text
src/main/resources/application-mysql.properties
```

환경변수:

| 환경변수 | 기본값 | 설명 |
|---|---|---|
| `CODEMATE_DB_HOST` | `localhost` | MySQL 호스트 |
| `CODEMATE_DB_PORT` | `3306` | MySQL 포트 |
| `CODEMATE_DB_NAME` | `codemate` | 데이터베이스 이름 |
| `CODEMATE_DB_USERNAME` | `codemate` | 접속 계정 |
| `CODEMATE_DB_PASSWORD` | 없음 | 접속 비밀번호, 필수 |

실행:

```powershell
$env:CODEMATE_DB_HOST="localhost"
$env:CODEMATE_DB_PORT="3306"
$env:CODEMATE_DB_NAME="codemate"
$env:CODEMATE_DB_USERNAME="codemate"
$env:CODEMATE_DB_PASSWORD="비밀번호"

.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

### 4. 프로필별 차이

| 항목 | `h2` | `mysql` |
|---|---|---|
| 용도 | 테스트 및 빠른 로컬 개발 | 실제 MySQL 연동 개발 |
| 데이터 유지 | 서버 종료 시 초기화 | MySQL 볼륨 또는 서버에 유지 |
| H2 Console | 활성화 | 비활성화 |
| 비밀번호 | 빈 값 | 환경변수 필수 |
| DDL | `update` | `update` |

### 5. 보안 및 운영 기준

1. DB 비밀번호를 Git 추적 파일에 저장하지 않음.
2. MySQL 비밀번호는 `CODEMATE_DB_PASSWORD`로만 전달.
3. 현재 `ddl-auto=update`는 로컬 개발 단계 기준.
4. 운영 배포 전 Flyway 적용 후 `ddl-auto=validate` 전환 예정.

### 6. 검증

1. 기본 프로필이 H2 설정을 로딩하는지 테스트.
2. MySQL 환경변수가 JDBC URL과 계정 설정에 반영되는지 테스트.
3. MySQL 프로필에서 H2 Console이 비활성화되는지 테스트.
4. 기존 통합 테스트가 기본 H2 프로필에서 정상 동작하는지 전체 확인.

### 7. 실제 MySQL 연동 검증

1. MySQL 준비
   - 로컬 MySQL 서버의 기존 Connection 사용.
   - `codemate` 데이터베이스 생성.
   - 프로젝트 전용 `codemate` 계정과 데이터베이스 권한 설정.

2. 터미널 환경 확인
   - PowerShell에서는 `$env:CODEMATE_DB_USERNAME="codemate"` 형식 사용.
   - 명령 프롬프트(cmd)에서는 `set CODEMATE_DB_USERNAME=codemate` 형식 사용.
   - cmd에서 PowerShell의 `$env:` 문법을 사용하면 파일 이름 또는 디렉터리 이름 구문 오류가 발생하는 점 확인.

3. MySQL 프로필 실행

```cmd
set CODEMATE_DB_USERNAME=codemate
set CODEMATE_DB_PASSWORD=비밀번호
mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

4. 데이터 저장 확인
   - 회원가입 데이터가 `users` 테이블에 저장됨.
   - 스터디 모집 글이 `study` 테이블에 저장됨.
   - 기술 스택과 연결 정보가 `tech_stacks`, `study_tech_stacks`에 저장됨.
   - 참여 신청 정보가 `study_members`에 저장됨.

5. 데이터 영속성 확인
   - MySQL 프로필 서버 종료.
   - 동일한 MySQL 프로필로 서버 재실행.
   - 기존 계정 로그인 성공.
   - 이전에 생성한 스터디 데이터 유지 확인.

6. 참여 신청 흐름 확인
   - 사용자 참여 신청.
   - 방장 신청 거절.
   - 거절된 사용자의 동일 스터디 재신청.
   - 방장의 재신청 승인.
   - 내 신청 내역에서 최종 `APPROVED` 상태 확인.

7. 검증 결과
   - H2가 아닌 실제 MySQL 환경에서도 핵심 API 정상 동작.
   - 서버 재시작 이후 데이터 유지.
   - 거절 → 재신청 → 승인 상태 전환 정상 동작.

### 8. 실행 가이드 통합

1. 문서 파일명 변경

```text
Postman_실행_가이드.md
→ CodeMate_실행_가이드.md
```

2. 변경 이유
   - 기존 문서가 Postman 요청 예시뿐 아니라 Swagger, H2, MySQL, 서버 실행 방법까지 포함.
   - 실제 문서 범위와 파일 제목을 일치시키기 위해 통합 실행 가이드로 변경.

3. 문서 구성
   - 서버 테스트.
   - Postman 및 Swagger API 테스트.
   - H2/MySQL 데이터베이스 테스트.
   - 서버 재시작과 데이터 영속성 테스트.
   - 참여 신청 거절, 재신청, 승인 테스트.

4. README 문서 링크를 새 파일명으로 변경.


---
## 2026-06-08 - Docker 실행 환경 구성

### 1. Docker 이미지 구성

1. 멀티 스테이지 `Dockerfile` 적용.
2. 빌드 단계
   - Eclipse Temurin 17 JDK Alpine 이미지 사용.
   - Maven Wrapper로 의존성 준비 및 JAR 빌드.
   - 테스트는 로컬 Maven 검증 단계에서 실행하고 이미지 빌드에서는 생략.
3. 실행 단계
   - Eclipse Temurin 17 JRE Alpine 이미지 사용.
   - Health Check용 `curl` 설치.
   - `codemate` 비루트 사용자로 애플리케이션 실행.
   - 애플리케이션 포트 `8080` 노출.

### 2. Docker Compose 구성

서비스:

1. `app`
   - CodeMate Spring Boot 이미지 빌드.
   - `mysql` 프로필 활성화.
   - Docker 내부의 `mysql:3306`으로 접속.
   - MySQL이 healthy 상태가 된 후 시작.

2. `mysql`
   - MySQL `8.4` 이미지 사용.
   - 데이터베이스와 사용자 정보를 `.env`에서 주입.
   - MySQL 데이터 볼륨 연결.

기본 포트:

- 애플리케이션: `8080:8080`.
- MySQL: `3307:3306`.
- 로컬 MySQL `3306`과 Docker MySQL의 포트 충돌 방지.

### 3. 환경변수 관리

1. `.env.example` 추가.
2. `.env`를 `.gitignore`에 등록.
3. 관리 항목
   - 애플리케이션 외부 포트.
   - MySQL 외부 포트.
   - 데이터베이스 이름과 계정.
   - DB 사용자 및 root 비밀번호.
   - JWT Secret.
4. 실제 비밀번호와 Secret은 Git에 포함하지 않음.

### 4. Health Check

1. Spring Boot Actuator 의존성 추가.
2. `/actuator/health`만 외부 노출.
3. Health 상세 정보는 공개하지 않음.
4. Spring Security에서 `/actuator/health` 공개 접근 허용.
5. MySQL은 `mysqladmin ping`으로 상태 확인.
6. 애플리케이션은 Actuator 응답의 `status=UP` 확인.

### 5. Docker 빌드 최적화

1. `.dockerignore` 추가.
2. 제외 대상
   - Git 메타데이터.
   - IDE 설정.
   - 기존 `target` 빌드 결과.
   - 로컬 `.env`.
   - 프로젝트 문서.
3. Maven 의존성 다운로드 레이어와 소스 빌드 레이어 분리.

### 6. 실행 및 데이터 유지

실행:

```powershell
docker compose up --build -d
```

상태 확인:

```powershell
docker compose ps
```

종료:

```powershell
docker compose down
```

- 일반 종료 시 MySQL 볼륨 유지.
- 재실행 후 회원과 스터디 데이터 유지.
- `docker compose down -v` 실행 시 MySQL 데이터 초기화.

### 7. 검증 항목

1. Dockerfile 멀티 스테이지 빌드.
2. Compose 환경변수 치환과 문법 검증.
3. Actuator Health API 인증 없는 접근 테스트.
4. 기존 API 전체 회귀 테스트.
5. 애플리케이션 이미지 빌드.
6. MySQL과 애플리케이션 컨테이너 healthy 상태 확인.

### 8. 검증 결과

1. `docker compose --env-file .env.example config` 설정 검증 통과.
2. `3_codemate-app:latest` 애플리케이션 이미지 빌드 성공.
3. `codemate-mysql` 컨테이너 `healthy` 상태 확인.
4. `codemate-app` 컨테이너 `healthy` 상태 확인.
5. `http://localhost:8081/actuator/health` 응답 `UP` 확인.
6. 기존 로컬 서버의 8080 포트와 충돌하지 않도록 Docker 애플리케이션 검증 시 8081 포트 사용.
7. Maven 테스트 19개 전체 통과.


---
## 2026-06-08 - Flyway 기반 DB 마이그레이션 도입

### 1. Flyway 의존성 구성

1. `spring-boot-starter-flyway` 추가.
2. MySQL 지원을 위한 `flyway-mysql` 런타임 모듈 추가.
3. Spring Boot 시작 과정에서 JPA 초기화 전에 Flyway 마이그레이션 실행.

### 2. 초기 스키마 마이그레이션

1. `V1__create_initial_schema.sql` 작성.
2. 초기 테이블
   - `users`.
   - `study`.
   - `study_members`.
   - `tech_stacks`.
   - `study_tech_stacks`.
3. 기본키, 외래키, 유니크 제약조건, 조회용 인덱스 명시.
4. 엔티티의 컬럼 길이와 nullable 조건을 SQL에 반영.

### 3. DB별 마이그레이션 분리

1. H2 경로
   - `db/migration/h2`.
2. MySQL 경로
   - `db/migration/mysql`.
3. 분리 이유
   - H2의 긴 문자열은 `CLOB`.
   - MySQL의 긴 문자열은 `LONGTEXT`.
   - 날짜 타입은 H2 `TIMESTAMP(6)`, MySQL `DATETIME(6)` 사용.
4. 두 DB에서 동일한 버전과 논리적 스키마 유지.

### 4. Hibernate 스키마 관리 전환

1. H2와 MySQL의 `spring.jpa.hibernate.ddl-auto`를 `update`에서 `validate`로 변경.
2. 테이블 생성과 변경 책임을 Flyway로 이동.
3. Hibernate는 엔티티와 실제 DB 구조가 일치하는지만 검증.
4. 모집 글 본문을 `Length.LONG32`로 명시해 MySQL `LONGTEXT`와 매핑.

### 5. 기존 MySQL 전환

1. `baseline-on-migrate=true` 적용.
2. 기존 Hibernate 생성 스키마를 V1 baseline으로 등록.
3. 기존 테이블과 데이터 삭제 없이 Flyway 관리 시작.
4. 모든 환경의 전환 완료 후 자동 baseline 설정 제거 검토.

### 6. 테스트 보강

1. Flyway 현재 버전이 V1인지 확인하는 통합 테스트 추가.
2. H2와 MySQL 프로필의 `ddl-auto=validate` 설정 검증.
3. MySQL 프로필의 baseline 설정 검증.

### 7. 트러블슈팅

1. H2 `LONGTEXT` 타입 불일치
   - 원인: H2 MySQL 모드에서 `LONGTEXT`를 `VARCHAR`로 해석.
   - 증상: Hibernate가 기대한 `CLOB`과 실제 타입 불일치.
   - 해결: H2와 MySQL 마이그레이션 경로 분리.
2. MySQL `@Lob String` 타입 불일치
   - 원인: Hibernate 7이 길이 정보가 없는 `@Lob String`을 `TINYTEXT`로 검증.
   - 증상: V1의 `LONGTEXT`와 Hibernate 기대 타입 불일치.
   - 해결: `Length.LONG32`로 긴 본문 의도를 명시하고 `LONGTEXT` 유지.

### 8. 검증 결과

1. H2에서 Flyway V1 SQL 적용 성공.
2. H2 Hibernate 스키마 검증 성공.
3. Maven 테스트 20개 전체 통과.
4. 빈 임시 MySQL에서 V1 SQL 직접 실행 성공.
5. 빈 MySQL에 5개 도메인 테이블과 `flyway_schema_history` 생성 확인.
6. 새 MySQL 이력의 `type=SQL`, `version=1`, `success=1` 확인.
7. 기존 Docker MySQL 이력의 `type=BASELINE`, `version=1`, `success=1` 확인.
8. 실제 `codemate-app`, `codemate-mysql` 컨테이너 healthy 상태 확인.
9. Docker 애플리케이션 `http://localhost:8081` 정상 기동 확인.
