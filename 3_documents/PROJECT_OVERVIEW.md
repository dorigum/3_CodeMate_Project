# CodeMate 프로젝트 개요

프로젝트의 목표, 기술 스택, 구조와 초기 개발 환경을 정리한 문서이다.

## 프로젝트 개요

CodeMate는 스터디와 모각코(모여서 각자 코딩) 모집을 위한 백엔드 API 프로젝트이다. 단순 게시판 CRUD를 넘어서, 모집 글 작성, 참여 신청, 방장 승인/거절, 모집 인원 제한, 권한 검증 같은 백엔드 비즈니스 로직을 중심으로 구현하는 것을 목표로 한다.

초기 MVP는 6일 안에 완성 가능한 API 서버를 목표로 하며, Swagger 또는 Postman을 통해 API 결과를 시연할 수 있는 수준까지 개발 진행


---

## MVP 범위

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

### 개발 진행 상황(2026.06.10)
현재까지는 프로젝트 구조 정리, 엔티티/enum 설계, Repository 뼈대, H2/JPA 설정, 공통 응답/예외 처리, 회원가입 API까지 구현


---

## 기술 스택

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

## 패키지 구조

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

## 구현 내역

### 엔티티 공통 기반

`BaseTimeEntity`

- 위치: `src/main/java/com/codemate/global/entity/BaseTimeEntity.java`
- 역할: `createdAt`, `updatedAt` 공통 관리
- 적용 방식: `@MappedSuperclass`, `@EntityListeners(AuditingEntityListener.class)`

`JpaAuditingConfig`

- 위치: `src/main/java/com/codemate/global/config/JpaAuditingConfig.java`
- 역할: JPA Auditing 활성화
- 적용 어노테이션: `@EnableJpaAuditing`

### User 도메인

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

### Study 도메인

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

### StudyMember 도메인

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

### TechStack 도메인

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

## 공통 응답 및 예외 처리

### ApiResponse

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

### ErrorCode

현재 정의된 에러 코드:
- `INVALID_INPUT_VALUE`
- `DUPLICATE_EMAIL`
- `DUPLICATE_NICKNAME`
- `INTERNAL_SERVER_ERROR`

### BusinessException

- 비즈니스 예외를 표현하는 커스텀 RuntimeException
- 서비스 레이어에서 도메인 규칙 위반이 발생하면 `BusinessException`을 던지고, 글로벌 예외 핸들러에서 공통 응답 형태로 변환

### GlobalExceptionHandler

현재 처리하는 예외:
- `BusinessException`
- `MethodArgumentNotValidException`

회원가입 요청값 검증 실패 시 필드명과 검증 메시지를 조합해 응답


---

## Security 설정

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

## H2/JPA 설정

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

## Maven Wrapper 문제 해결

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

## 테스트 내역

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

---

## 서버 실행 및 직접 확인 방법

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

## 설계 및 운영 문서

- [프로젝트 명세](PROJECT_SPECIFICATION.md): 기능·비기능 요구사항과 API 규칙
- [시스템 아키텍처](ARCHITECTURE.md): 실행 구성, 계층 구조, JWT와 동시성 흐름
- [데이터베이스 설계](DATABASE_DESIGN.md): Mermaid ERD, 테이블과 Flyway 정의
- [CodeMate 실행 가이드](CodeMate_실행_가이드.md): 로컬·API·MySQL·Docker·CI 실행 방법
- [프로젝트 회고](RETROSPECTIVE.md): 개발 회고 초안과 배포 후 작성 항목

## 현재 단계

1. 회원·JWT·Study·참여 관리 핵심 기능 구현 완료
2. H2/MySQL Flyway V1~V3 구성 완료
3. Docker Compose와 GitHub Actions CI 구성 완료
4. API·통합·도메인 단위 테스트 구성
5. AWS 배포 환경 구성 예정


---
