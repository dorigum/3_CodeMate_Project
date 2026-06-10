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
회원·JWT·Study·참여 관리 기능, 자동화 테스트, Flyway, Docker, CI/CD와 AWS HTTPS 운영 배포까지 완료


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
- 로컬 개발: H2 Database
- 통합·운영: MySQL 8.4
- Schema 관리: Flyway V1~V3
- 운영 영속성: Docker named volume

### Build
- Maven
- Maven Wrapper

### Test
- JUnit 5
- SpringBootTest
- MockMvc
- Spring Security Test

### DevOps
- Docker, Docker Compose
- GitHub Actions CI/CD
- Docker Hub
- AWS EC2, ALB, ACM
- `polar-bear.o-r.kr` HTTPS 도메인


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
- [CodeMate 실행 가이드](../guides/CodeMate_실행_가이드.md): 로컬·API·MySQL·Docker·CI 실행 방법
- [AWS 배포](AWS_DEPLOYMENT.md): EC2·Docker Hub·CD·ALB·ACM·HTTPS 구성과 검증
- [프로젝트 회고](RETROSPECTIVE.md): 개발과 배포 과정 회고

## 현재 단계

1. 회원·JWT·Study·참여 관리 핵심 기능 구현 완료
2. H2/MySQL Flyway V1~V3 구성 완료
3. Docker Compose와 GitHub Actions CI 구성 완료
4. API·통합·도메인 단위 테스트 구성
5. GitHub Actions CD와 Docker Hub 이미지 배포 완료
6. AWS EC2·ALB·ACM·도메인 HTTPS 배포 완료
7. Postman 운영 API 및 MySQL 데이터 영속성 검증 완료


---
