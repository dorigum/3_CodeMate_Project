# 🦄CodeMate

개발자를 위한 스터디(프로젝트 형태) 및 모각코(모여서 각자 코딩, 원데이 스터디) 모집·참여 관리 백엔드 API 프로젝트입니다.

단순한 모집 게시판을 넘어 회원 인증, 참여 신청, 방장 승인·거절, 모집 인원 관리 등 실제 스터디 운영 과정에서 필요한 비즈니스 흐름을 구현합니다.

## 주요 기능

- Spring Security와 JWT 기반 회원가입·로그인
- 로그인 사용자 정보 조회
- 스터디 및 모각코 모집 글 CRUD
- 키워드·카테고리·모집 상태·진행 방식·지역·기술 스택 기반 검색과 페이징
- 기술 스택 등록 및 스터디 연결
- 스터디 참여 신청과 중복 신청 방지
- 방장의 참여 신청 목록 조회
- 참여 신청 승인·거절
- 승인 시 현재 인원 증가
- 정원 도달 시 모집 상태 자동 마감
- 동시 승인 시 비관적 락을 통한 모집 정원 초과 방지
- 인증·인가 및 비즈니스 예외 응답 형식 통일
- Swagger UI와 OpenAPI 기반 API 문서화

## 기술 스택

| 구분 | 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0.6 |
| Web | Spring Web MVC |
| Persistence | Spring Data JPA, Hibernate |
| Security | Spring Security, JWT |
| Database | H2, MySQL Driver |
| Build | Maven, Maven Wrapper |
| Test | JUnit 5, MockMvc, Spring Security Test |
| API Docs | springdoc-openapi, Swagger UI |

## 주요 도메인

- `User`: 회원 정보와 권한
- `Study`: 스터디·모각코 모집 글과 모집 상태
- `StudyMember`: 참여 신청과 승인 상태
- `TechStack`: 기술 스택 정보
- `StudyTechStack`: 스터디와 기술 스택의 연결

## 실행 방법

### 요구 사항

- Java 17 이상

### 서버 실행

```powershell
git clone https://github.com/dorigum/3_CodeMate.git
cd 3_CodeMate
.\mvnw.cmd spring-boot:run
```

서버는 기본적으로 `http://localhost:8080`에서 실행됩니다.

### 테스트 실행

```powershell
.\mvnw.cmd test
```

### JWT Secret 설정

로컬에서는 기본 개발용 키로 실행할 수 있습니다. 별도 키를 사용하려면 서버 실행 전에 환경 변수를 설정합니다.

```powershell
$env:CODEMATE_JWT_SECRET="Base64로 인코딩된 JWT Secret"
.\mvnw.cmd spring-boot:run
```

## H2 Console

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:codemate`
- User Name: `sa`
- Password: 비워두기

H2는 인메모리 데이터베이스이므로 서버를 종료하면 저장된 데이터가 초기화됩니다.

## Swagger/OpenAPI

서버 실행 후 아래 주소에서 API 명세를 확인하고 직접 요청할 수 있습니다.

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

JWT 인증이 필요한 API 테스트 순서:

1. `POST /api/users/signup`으로 회원가입
2. `POST /api/users/login`으로 로그인
3. 응답의 `data.accessToken` 복사
4. Swagger UI 상단 `Authorize` 클릭
5. 토큰 값만 입력하고 인증
6. 자물쇠 표시가 있는 API 실행

`Bearer ` 접두사는 Swagger UI가 자동으로 추가하므로 access token 값만 입력합니다.

## 대표 API

| Method | Endpoint | 설명 | 인증 |
|---|---|---|---|
| `POST` | `/api/users/signup` | 회원가입 | 불필요 |
| `POST` | `/api/users/login` | 로그인 및 JWT 발급 | 불필요 |
| `GET` | `/api/users/me` | 내 정보 조회 | 필요 |
| `POST` | `/api/studies` | 모집 글 생성 | 필요 |
| `GET` | `/api/studies` | 모집 글 목록 조회 | 불필요 |
| `GET` | `/api/studies/{studyId}` | 모집 글 상세 조회 | 불필요 |
| `PATCH` | `/api/studies/{studyId}` | 모집 글 수정 | 방장 |
| `DELETE` | `/api/studies/{studyId}` | 모집 글 삭제 | 방장 |
| `POST` | `/api/studies/{studyId}/members` | 참여 신청 | 필요 |
| `GET` | `/api/studies/{studyId}/members` | 참여 신청 목록 조회 | 방장 |
| `PATCH` | `/api/studies/{studyId}/members/{memberId}/approve` | 참여 승인 | 방장 |
| `PATCH` | `/api/studies/{studyId}/members/{memberId}/reject` | 참여 거절 | 방장 |

인증이 필요한 요청에는 다음 Header를 사용합니다.

```text
Authorization: Bearer {accessToken}
```

스터디 목록은 검색 조건을 자유롭게 조합할 수 있습니다.

```http
GET /api/studies?keyword=코루틴&category=STUDY&status=RECRUITING&meetingType=OFFLINE&location=판교&techStack=Kotlin&page=0&size=10
```

지원 조건:

- `keyword`: 제목 또는 내용 부분 검색
- `category`: `STUDY`, `MOGAKKO`
- `status`: `RECRUITING`, `CLOSED`, `IN_PROGRESS`, `FINISHED`
- `meetingType`: `ONLINE`, `OFFLINE`
- `location`: 지역 부분 검색
- `techStack`: 기술 스택 이름 부분 검색

## 문서

- [프로젝트 개발 기록](3_documents/PROJECT_LOG.md)
- [Postman 실행 가이드](3_documents/Postman_실행_가이드.md)
- [백엔드 프로젝트 기획](3_documents/Backend_Project_기획.md)

## 향후 계획

- MySQL 환경 프로필 분리
- Docker 기반 실행 환경 구성

---
*Updated at_2026.06.05*
