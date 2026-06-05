# 🦄CodeMate

개발자를 위한 스터디(프로젝트 형태) 및 모각코(모여서 각자 코딩, 원데이 스터디) 모집·참여 관리 백엔드 API 프로젝트입니다.

단순한 모집 게시판을 넘어 회원 인증, 참여 신청, 방장 승인·거절, 모집 인원 관리 등 실제 스터디 운영 과정에서 필요한 비즈니스 흐름을 구현합니다.

## 주요 기능

- Spring Security와 JWT 기반 회원가입·로그인
- 로그인 사용자 정보 조회
- 스터디 및 모각코 모집 글 CRUD
- 카테고리·모집 상태 기반 목록 조회와 페이징
- 기술 스택 등록 및 스터디 연결
- 스터디 참여 신청과 중복 신청 방지
- 방장의 참여 신청 목록 조회
- 참여 신청 승인·거절
- 승인 시 현재 인원 증가
- 정원 도달 시 모집 상태 자동 마감
- 인증·인가 및 비즈니스 예외 응답 형식 통일

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

## 문서

- [프로젝트 개발 기록](3_documents/PROJECT_LOG.md)
- [Postman 실행 가이드](3_documents/Postman_실행_가이드.md)
- [백엔드 프로젝트 기획](3_documents/Backend_Project_기획.md)

## 향후 계획

- 기술 스택·지역 기반 검색 조건 확장
- Swagger/OpenAPI 문서화
- MySQL 환경 프로필 분리
- Docker 기반 실행 환경 구성
- 동시 승인 상황의 모집 정원 정합성 보강

---
*Updated at_2026.06.05*
