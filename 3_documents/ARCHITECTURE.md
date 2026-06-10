# CodeMate 시스템 아키텍처

CodeMate의 실행 구성, 애플리케이션 계층과 JWT 인증 요청 흐름을 정리한다.

## 시스템 구성도

```mermaid
flowchart LR
    Client["Swagger UI / Postman / Web Client"]
    App["CodeMate Spring Boot API"]
    Security["Spring Security + JWT Filter"]
    Controller["Controller"]
    Service["Service / Transaction"]
    Repository["Spring Data JPA"]
    DB[("H2 / MySQL")]
    Flyway["Flyway Migration"]
    Health["Actuator Health"]

    Client -->|HTTP + JSON| App
    App --> Security
    Security --> Controller
    Controller --> Service
    Service --> Repository
    Repository --> DB
    Flyway --> DB
    App --> Health
```

## Docker 실행 구성

```mermaid
flowchart LR
    User["사용자"]
    Host["Host localhost"]
    App["codemate-app :8080"]
    MySQL[("codemate-mysql :3306")]
    Volume[("codemate-mysql-data")]

    User -->|localhost:8081| Host
    Host --> App
    App -->|내부 네트워크 mysql:3306| MySQL
    Host -->|localhost:3307| MySQL
    MySQL --> Volume
```

기본 포트는 `.env` 값에 따라 바꿀 수 있다. 애플리케이션 컨테이너는 MySQL health check가 성공한 뒤 시작한다.

## AWS 운영 배포 구성

```mermaid
flowchart LR
    Client["Postman / Web Client"]
    Domain["polar-bear.o-r.kr"]
    ALB["Application Load Balancer<br/>80 / 443"]
    Target["Target Group<br/>HTTP 8080"]
    EC2["EC2 Ubuntu"]
    App["codemate-app :8080"]
    MySQL[("codemate-mysql :3306")]
    Volume[("Docker Named Volume")]

    Client -->|HTTPS| Domain
    Domain --> ALB
    ALB --> Target
    Target --> EC2
    EC2 --> App
    App -->|Docker 내부 네트워크| MySQL
    MySQL --> Volume
```

1. HTTP `80` 요청은 ALB에서 HTTPS `443`으로 리다이렉트한다.
2. ACM 인증서를 적용한 ALB가 TLS를 종료한다.
3. ALB는 Target Group을 통해 EC2의 애플리케이션 `8080` 포트로 전달한다.
4. EC2 `8080` 인바운드는 ALB 보안 그룹만 허용한다.
5. MySQL `3306`은 외부에 공개하지 않고 Docker 내부에서만 사용한다.
6. ALB는 `/actuator/health`의 `200` 응답으로 대상 상태를 확인한다.

## 운영 배포 흐름

```mermaid
sequenceDiagram
    actor Developer
    participant GitHub
    participant Actions as GitHub Actions
    participant Hub as Docker Hub
    participant EC2

    Developer->>GitHub: Push 또는 수동 CD 실행
    GitHub->>Actions: Workflow 시작
    Actions->>Actions: Maven 검증과 Docker Build
    Actions->>Hub: SHA / latest 이미지 Push
    Actions->>EC2: Compose와 .env.prod 전송
    EC2->>Hub: 새 이미지 Pull
    EC2->>EC2: app 컨테이너 교체
    Actions->>EC2: Actuator Health Check
    EC2-->>Actions: status UP
```

MySQL 컨테이너와 named volume은 애플리케이션 재배포 시 유지한다. 상세 설정은 [AWS 배포 문서](AWS_DEPLOYMENT.md)에서 확인한다.

## 애플리케이션 계층

| 계층 | 책임 |
|---|---|
| Controller | HTTP 요청 매핑, 입력 검증, 인증 사용자 전달, 응답 생성 |
| Service | 트랜잭션, 권한 검증, 비즈니스 상태 전이 |
| Repository | 엔티티 조회·저장, Specification, 비관적 락 |
| Entity | 핵심 상태와 상태 변경 규칙 보유 |
| Global | Security, 예외 처리, 공통 응답, 설정 |

패키지는 기능별 도메인을 먼저 나누고 각 도메인 내부에서 Controller, Service, Repository, Entity, DTO 계층을 구분한다.

## 일반 API 요청 흐름

```mermaid
sequenceDiagram
    actor Client
    participant Filter as JwtAuthenticationFilter
    participant Security as SecurityContext
    participant Controller
    participant Service
    participant Repository
    participant DB

    Client->>Filter: HTTP 요청 + Bearer Access Token
    Filter->>Filter: 서명, 만료, tokenType 검증
    Filter->>Repository: 이메일로 사용자 조회
    Repository->>DB: SELECT users
    DB-->>Repository: 사용자와 tokenVersion
    Filter->>Filter: JWT tokenVersion 비교
    Filter->>Security: Authentication 등록
    Filter->>Controller: 요청 전달
    Controller->>Service: 로그인 사용자 ID + 요청 DTO
    Service->>Repository: 조회·저장
    Repository->>DB: SQL 실행
    DB-->>Repository: 결과
    Service-->>Controller: 응답 DTO
    Controller-->>Client: ApiResponse JSON
```

토큰이 없거나 유효하지 않으면 SecurityContext를 비우고 인증이 필요한 경로에서 공통 `401` 응답을 반환한다.

## 로그인과 토큰 발급

```mermaid
sequenceDiagram
    actor User
    participant API as UserController
    participant Service as UserService
    participant Auth as AuthenticationManager
    participant JWT as JwtTokenProvider
    participant DB

    User->>API: 이메일 + 비밀번호
    API->>Service: 로그인 요청
    Service->>Auth: 자격 증명 검증
    Auth->>DB: 사용자와 BCrypt 비밀번호 조회
    DB-->>Auth: 사용자 정보
    Auth-->>Service: Authentication
    Service->>JWT: Access/Refresh Token 생성
    Service->>DB: Refresh Token 해시와 만료 시각 저장
    Service-->>User: 토큰 쌍과 만료 시간
```

## 토큰 재발급

```mermaid
sequenceDiagram
    actor User
    participant API
    participant Service
    participant JWT
    participant DB

    User->>API: Refresh Token
    API->>Service: 재발급 요청
    Service->>JWT: 서명·만료·REFRESH 유형 검증
    Service->>DB: Refresh Token 해시 잠금 조회
    DB-->>Service: 저장 토큰
    Service->>Service: 해시·사용자·tokenVersion 검증
    Service->>JWT: 새 Access/Refresh Token 생성
    Service->>DB: Refresh Token 회전 저장
    Service-->>User: 새 토큰 쌍
```

이전 Refresh Token은 회전 직후 저장 해시와 달라져 다시 사용할 수 없다.

## 참여 승인 동시성 흐름

```mermaid
sequenceDiagram
    actor Host
    participant API
    participant Service
    participant StudyRepo as StudyRepository
    participant DB

    Host->>API: 참여 신청 승인
    API->>Service: studyId + memberId + hostId
    Service->>StudyRepo: findByIdForUpdate
    StudyRepo->>DB: SELECT ... FOR UPDATE
    DB-->>Service: 잠긴 Study
    Service->>Service: 방장·신청 상태·정원 검증
    Service->>DB: 신청 APPROVED + 현재 인원 증가
    Service-->>Host: 승인 결과
```

동일 스터디 승인 요청은 비관적 락으로 직렬화되어 정원을 초과하지 않는다.

## 프로필별 실행

| 프로필 | DB | 용도 |
|---|---|---|
| `h2` | 인메모리 H2 | 빠른 로컬 개발과 대부분의 테스트 |
| `mysql` | MySQL | 로컬·Docker 통합 실행 |
| `prod` | MySQL | 운영 설정, Swagger/H2 Console 비활성화 |

모든 프로필은 JPA `ddl-auto=validate`를 사용하고 스키마 변경은 Flyway가 담당한다.

## 보안 경계

1. JWT Secret과 DB 비밀번호는 환경변수로 전달한다.
2. 비밀번호는 BCrypt로 해시한다.
3. Refresh Token 원문은 DB에 저장하지 않고 SHA-256 해시를 저장한다.
4. 로그아웃과 비밀번호 변경은 사용자 `tokenVersion`을 증가시켜 기존 Access Token을 무효화한다.
5. 운영 프로필에서는 Swagger, OpenAPI JSON과 H2 Console을 노출하지 않는다.
6. 외부 HTTPS는 ALB에서 종료하고 EC2 애플리케이션 포트는 ALB 보안 그룹에만 허용한다.
7. 운영 MySQL은 호스트 포트를 공개하지 않고 Docker 내부 네트워크에서만 접근한다.
