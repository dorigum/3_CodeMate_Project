# CodeMate 실행 가이드

이 문서는 CodeMate 서버 실행과 데이터베이스 연결, Swagger/Postman API 테스트를 한 곳에서 확인하기 위한 통합 가이드이다.

## 문서 구성

1. 서버 테스트
   - H2 기본 프로필 실행
   - MySQL 프로필 실행
   - 서버와 Swagger 동작 확인
2. Postman 및 Swagger API 테스트
   - 회원가입과 로그인
   - JWT 인증
   - 스터디 CRUD
   - 참여 신청, 승인, 거절, 재신청
   - 내 신청 상태 조회
3. 데이터베이스 테스트
   - H2 Console 확인
   - MySQL 테이블과 저장 데이터 확인
   - 서버 재시작 후 데이터 영속성 확인

## 1. 서버 실행

### 1.1 프로젝트 경로 이동

```powershell
cd C:\KOSTA_Projects\3_CodeMate
```

### 1.2 서버 실행

기본 H2 프로필:

```powershell
.\mvnw.cmd spring-boot:run
```

MySQL 프로필은 사용하는 터미널에 맞는 문법으로 환경변수를 설정한다.

PowerShell:

```powershell
$env:CODEMATE_DB_HOST="localhost"
$env:CODEMATE_DB_PORT="3306"
$env:CODEMATE_DB_NAME="codemate"
$env:CODEMATE_DB_USERNAME="codemate"
$env:CODEMATE_DB_PASSWORD="비밀번호"

.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

명령 프롬프트(cmd):

```cmd
set CODEMATE_DB_HOST=localhost
set CODEMATE_DB_PORT=3306
set CODEMATE_DB_NAME=codemate
set CODEMATE_DB_USERNAME=codemate
set CODEMATE_DB_PASSWORD=비밀번호

mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

MySQL 프로필을 실행하기 전에 `codemate` 데이터베이스와 접속 계정을 생성해야 한다.
`CODEMATE_DB_PASSWORD`는 설정 파일에 저장하지 않고 반드시 환경변수로 전달한다.

PowerShell의 `$env:변수명="값"` 문법을 cmd에서 실행하면 파일 이름 또는 디렉터리 이름 구문 오류가 발생한다.
cmd에서는 `set 변수명=값` 문법을 사용한다.

### 1.3 서버 주소

```text
http://localhost:8080
```

### 1.4 Swagger UI

Postman 대신 브라우저에서 API를 확인하려면 아래 주소로 접속한다.

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

Swagger UI에서 JWT 인증이 필요한 API를 실행하는 방법:

1. 회원가입과 로그인 API를 실행한다.
2. 로그인 응답의 `data.accessToken` 값을 복사한다.
3. 화면 상단의 `Authorize` 버튼을 누른다.
4. `bearerAuth` 입력란에 access token 값만 입력한다.
5. 인증 후 자물쇠 표시가 있는 API를 실행한다.

`Bearer ` 문자열은 Swagger UI가 자동으로 요청 Header에 추가한다.

---

## 2. Postman 기본 설정

### 2.1 Environment 생성

Postman에서 `CodeMate Local` 환경을 만들고 아래 변수를 등록한다.

| 변수명           | 초기값                     | 설명              |
| ------------- | ----------------------- | --------------- |
| `baseUrl`     | `http://localhost:8080` | 로컬 서버 주소        |
| `accessToken` | 비워두기                    | 로그인 후 JWT 저장    |
| `studyId`     | 비워두기                    | 생성된 스터디 ID 저장   |
| `memberId`    | 비워두기                    | 생성된 참여 신청 ID 저장 |

### 2.2 공통 Header

JSON Body를 보내는 요청에는 아래 Header를 추가한다.

```text
Content-Type: application/json
```

로그인이 필요한 요청에는 아래 Header를 추가한다.

```text
Authorization: Bearer {{accessToken}}
```

---

## 3. H2 Console 확인

H2 Console은 기본 `h2` 프로필에서만 사용할 수 있다. `mysql` 프로필에서는 비활성화된다.

### 3.1 접속 URL

```text
{{baseUrl}}/h2-console
```

### 3.2 접속 정보

| 항목 | 값 |
|---|---|
| JDBC URL | `jdbc:h2:mem:codemate` |
| User Name | `sa` |
| Password | 비워두기 |

### 3.3 주요 테이블

- `users`
- `study`
- `study_members`
- `tech_stacks`
- `study_tech_stacks`

---

## 4. 회원가입

### 4.1 요청

```http
POST {{baseUrl}}/api/users/signup
```

### 4.2 Body

```json
{
  "email": "host@example.com",
  "password": "password123",
  "nickname": "host",
  "mainTechStack": "Spring Boot"
}
```

### 4.3 정상 응답

```json
{
  "success": true,
  "message": "회원가입이 완료되었습니다.",
  "data": {
    "id": 1,
    "email": "host@example.com",
    "nickname": "host",
    "mainTechStack": "Spring Boot"
  }
}
```

### 4.4 추가 테스트

참여 신청 테스트를 위해 다른 사용자도 하나 더 생성한다.

```json
{
  "email": "applicant@example.com",
  "password": "password123",
  "nickname": "applicant",
  "mainTechStack": "JPA"
}
```

---

## 5. 로그인

### 5.1 요청

```http
POST {{baseUrl}}/api/users/login
```

### 5.2 Body

```json
{
  "email": "host@example.com",
  "password": "password123"
}
```

### 5.3 정상 응답

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

### 5.4 accessToken 저장

응답의 `data.accessToken` 값을 복사해서 Postman 환경 변수 `accessToken`에 저장한다.

Tests 탭에 아래 스크립트를 넣으면 자동 저장할 수 있다.

```javascript
const json = pm.response.json();
pm.environment.set("accessToken", json.data.accessToken);
```

---

## 6. 내 정보 조회

### 6.1 요청

```http
GET {{baseUrl}}/api/users/me
```

### 6.2 Header

```text
Authorization: Bearer {{accessToken}}
```

### 6.3 정상 응답

```json
{
  "success": true,
  "message": "내 정보를 조회했습니다.",
  "data": {
    "id": 1,
    "email": "host@example.com",
    "nickname": "host",
    "mainTechStack": "Spring Boot",
    "role": "ROLE_USER"
  }
}
```

---

## 7. 스터디 생성

### 7.1 요청

```http
POST {{baseUrl}}/api/studies
```

### 7.2 Header

```text
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

### 7.3 Body

```json
{
  "title": "Spring Boot 스터디",
  "content": "매주 프로젝트 코드를 리뷰합니다.",
  "category": "STUDY",
  "meetingType": "ONLINE",
  "location": "Discord",
  "maxMemberCount": 4,
  "techStackNames": ["Java", "Spring Boot", "JPA"]
}
```

### 7.4 정상 응답

```json
{
  "success": true,
  "message": "스터디 모집 글이 생성되었습니다.",
  "data": {
    "id": 1,
    "hostId": 1,
    "hostNickname": "host",
    "title": "Spring Boot 스터디",
    "content": "매주 프로젝트 코드를 리뷰합니다.",
    "category": "STUDY",
    "meetingType": "ONLINE",
    "location": "Discord",
    "maxMemberCount": 4,
    "currentMemberCount": 1,
    "status": "RECRUITING",
    "techStackNames": [
      "Java",
      "Spring Boot",
      "JPA"
    ]
  }
}
```

### 7.5 studyId 저장

응답의 `data.id` 값을 복사해서 Postman 환경 변수 `studyId`에 저장한다.

Tests 탭에 아래 스크립트를 넣으면 자동 저장할 수 있다.

```javascript
const json = pm.response.json();
pm.environment.set("studyId", json.data.id);
```

---

## 8. 스터디 목록 조회

### 8.1 전체 조회

```http
GET {{baseUrl}}/api/studies
```

### 8.2 조건 조회

```http
GET {{baseUrl}}/api/studies?category=STUDY&status=RECRUITING&page=0&size=10
```

### 8.3 복합 검색

```http
GET {{baseUrl}}/api/studies?keyword=코루틴&category=STUDY&status=RECRUITING&meetingType=OFFLINE&location=판교&techStack=Kotlin&page=0&size=10
```

사용 가능한 검색 조건:

| 조건 | 설명 | 예시 |
|---|---|---|
| `keyword` | 제목 또는 내용 부분 검색 | `코루틴` |
| `category` | 모집 글 분류 | `STUDY`, `MOGAKKO` |
| `status` | 모집 상태 | `RECRUITING`, `CLOSED`, `IN_PROGRESS`, `FINISHED` |
| `meetingType` | 진행 방식 | `ONLINE`, `OFFLINE` |
| `location` | 지역 부분 검색 | `판교` |
| `techStack` | 기술 스택 이름 부분 검색 | `Kotlin` |
| `page` | 페이지 번호, 0부터 시작 | `0` |
| `size` | 페이지 크기 | `10` |

검색 조건은 필요한 값만 전달할 수 있으며 여러 조건을 함께 사용하면 모든 조건을 만족하는 모집 글만 반환된다.

### 8.4 정상 응답 구조

```json
{
  "success": true,
  "message": "스터디 모집 글 목록을 조회했습니다.",
  "data": {
    "items": [],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

---

## 9. 스터디 상세 조회

### 9.1 요청

```http
GET {{baseUrl}}/api/studies/{{studyId}}
```

### 9.2 특징

- 로그인하지 않아도 조회 가능하다.
- `studyId`는 스터디 생성 응답의 `data.id` 값을 사용한다.

---

## 10. 스터디 수정

### 10.1 요청

```http
PATCH {{baseUrl}}/api/studies/{{studyId}}
```

### 10.2 Header

```text
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

### 10.3 Body

```json
{
  "title": "Spring Boot 심화 스터디",
  "content": "JPA와 Security까지 함께 다룹니다.",
  "category": "STUDY",
  "meetingType": "OFFLINE",
  "location": "강남",
  "maxMemberCount": 5,
  "status": "RECRUITING",
  "techStackNames": ["Java", "Spring Security"]
}
```

### 10.4 권한 규칙

- 로그인 필요.
- 스터디 방장만 수정 가능.
- `techStackNames`를 보내면 기존 기술 스택 연결이 요청값 기준으로 교체된다.
- 다른 사용자의 토큰으로 요청하면 403 응답이 발생한다.

---

## 11. 스터디 삭제

### 11.1 요청

```http
DELETE {{baseUrl}}/api/studies/{{studyId}}
```

### 11.2 Header

```text
Authorization: Bearer {{accessToken}}
```

### 11.3 권한 규칙

- 로그인 필요.
- 스터디 방장만 삭제 가능.
- 삭제 후 같은 `studyId`로 상세 조회하면 404 응답이 발생한다.

---

## 12. 스터디 참여 신청

### 12.1 사전 준비

1. 방장 계정으로 회원가입한다.
2. 방장 계정으로 로그인한다.
3. 방장 토큰으로 스터디를 생성한다.
4. 신청자 계정으로 회원가입한다.
5. 신청자 계정으로 로그인한다.
6. 환경 변수 `accessToken`을 신청자 토큰으로 교체한다.

### 12.2 요청

```http
POST {{baseUrl}}/api/studies/{{studyId}}/members
```

### 12.3 Header

```text
Authorization: Bearer {{accessToken}}
```

### 12.4 Body

없음.

### 12.5 정상 응답

```json
{
  "success": true,
  "message": "스터디 참여 신청이 완료되었습니다.",
  "data": {
    "id": 1,
    "studyId": 1,
    "userId": 2,
    "userNickname": "applicant",
    "status": "PENDING"
  }
}
```

### 12.6 실패 케이스

#### 12.6.1 본인 글 신청

방장 토큰으로 참여 신청하면 실패한다.

```json
{
  "success": false,
  "message": "본인이 만든 스터디에는 참여 신청할 수 없습니다."
}
```

#### 12.6.2 중복 신청

같은 신청자 토큰으로 한 번 더 신청하면 실패한다.

```json
{
  "success": false,
  "message": "이미 참여 신청한 스터디입니다."
}
```

#### 12.6.3 비로그인 신청

`Authorization` Header 없이 신청하면 인증 실패가 발생한다.

---

## 13. 추천 테스트 순서

1. 서버 실행
2. H2 Console 접속 확인
3. 방장 회원가입
4. 방장 로그인
5. 방장 토큰 저장
6. 스터디 생성
7. `studyId` 저장
8. 스터디 목록 조회
9. 스터디 상세 조회
10. 스터디 수정
11. 신청자 회원가입
12. 신청자 로그인
13. 신청자 토큰 저장
14. 스터디 참여 신청
15. 중복 참여 신청 실패 확인
16. 참여 신청 응답의 `data.id`를 `memberId`로 저장
17. 방장 토큰으로 다시 교체
18. 참여 신청 목록 조회
19. 참여 신청 승인 또는 거절
20. H2 Console에서 `study_members` 테이블 확인

---

## 14. 참여 신청 목록 조회

### 14.1 사전 준비

1. 방장 계정으로 스터디를 생성한다.
2. 신청자 계정으로 참여 신청을 보낸다.
3. `accessToken` 환경 변수를 방장 토큰으로 다시 교체한다.

### 14.2 전체 목록 요청

```http
GET {{baseUrl}}/api/studies/{{studyId}}/members
```

### 14.3 상태별 목록 요청

```http
GET {{baseUrl}}/api/studies/{{studyId}}/members?status=PENDING
```

사용 가능한 status 값:

- `PENDING`
- `APPROVED`
- `REJECTED`

### 14.4 Header

```text
Authorization: Bearer {{accessToken}}
```

### 14.5 정상 응답

```json
{
  "success": true,
  "message": "스터디 참여 신청 목록을 조회했습니다.",
  "data": [
    {
      "id": 1,
      "studyId": 1,
      "userId": 2,
      "userNickname": "applicant",
      "status": "PENDING"
    }
  ]
}
```

### 14.6 권한 규칙

- 로그인 필요.
- 스터디 방장만 신청 목록을 조회할 수 있다.
- 신청자 토큰으로 조회하면 403 응답이 발생한다.

---

## 15. 참여 신청 승인

### 15.1 사전 준비

1. 방장 계정으로 스터디를 생성한다.
2. 신청자 계정으로 참여 신청을 보낸다.
3. 참여 신청 응답의 `data.id` 값을 `memberId` 환경 변수에 저장한다.
4. `accessToken` 환경 변수를 방장 토큰으로 다시 교체한다.

### 15.2 요청

```http
PATCH {{baseUrl}}/api/studies/{{studyId}}/members/{{memberId}}/approve
```

### 15.3 Header

```text
Authorization: Bearer {{accessToken}}
```

### 15.4 정상 응답

```json
{
  "success": true,
  "message": "스터디 참여 신청을 승인했습니다.",
  "data": {
    "id": 1,
    "studyId": 1,
    "userId": 2,
    "userNickname": "applicant",
    "status": "APPROVED"
  }
}
```

### 15.5 승인 후 확인

스터디 상세 조회:

```http
GET {{baseUrl}}/api/studies/{{studyId}}
```

확인할 값:

- `currentMemberCount`가 1 증가한다.
- `currentMemberCount == maxMemberCount`가 되면 `status`가 `CLOSED`로 변경된다.
- 여러 승인 요청이 동시에 들어와도 스터디 행에 쓰기 락을 적용해 모집 정원을 초과하지 않는다.
- 마지막 한 자리에 대한 동시 승인 요청은 하나만 성공하고 나머지는 모집 마감 응답을 받는다.

---

## 16. 참여 신청 거절

### 16.1 사전 준비

1. 방장 계정으로 스터디를 생성한다.
2. 신청자 계정으로 참여 신청을 보낸다.
3. 참여 신청 응답의 `data.id` 값을 `memberId` 환경 변수에 저장한다.
4. `accessToken` 환경 변수를 방장 토큰으로 다시 교체한다.

### 16.2 요청

```http
PATCH {{baseUrl}}/api/studies/{{studyId}}/members/{{memberId}}/reject
```

### 16.3 Header

```text
Authorization: Bearer {{accessToken}}
```

### 16.4 정상 응답

```json
{
  "success": true,
  "message": "스터디 참여 신청을 거절했습니다.",
  "data": {
    "id": 1,
    "studyId": 1,
    "userId": 2,
    "userNickname": "applicant",
    "status": "REJECTED"
  }
}
```

### 16.5 거절 후 확인

- `currentMemberCount`는 증가하지 않는다.
- 스터디 상태는 그대로 유지된다.

---

## 17. 참여 신청 처리 실패 케이스

### 17.1 방장이 아닌 사용자 승인/거절

신청자 토큰으로 승인/거절 API를 호출하면 실패한다.

```json
{
  "success": false,
  "message": "스터디 방장만 처리할 수 있습니다."
}
```

### 17.2 이미 처리된 신청 재처리

이미 승인 또는 거절된 신청을 다시 처리하면 실패한다.

```json
{
  "success": false,
  "message": "대기 중인 신청만 처리할 수 있습니다."
}
```

### 17.3 정원 초과 승인

이미 정원이 찬 스터디에서 추가 승인을 시도하면 실패한다.

```json
{
  "success": false,
  "message": "모집 인원이 마감되었습니다."
}
```

---

## 18. 인증/인가 실패 응답 확인

### 18.1 토큰 없이 인증 필요 API 호출

```http
POST {{baseUrl}}/api/studies
```

`Authorization` Header 없이 요청하면 아래 응답이 발생한다.

```json
{
  "success": false,
  "message": "로그인이 필요합니다."
}
```

### 18.2 잘못된 토큰으로 API 호출

```http
GET {{baseUrl}}/api/users/me
Authorization: Bearer invalid-token
```

정상 JWT가 아니면 아래 응답이 발생한다.

```json
{
  "success": false,
  "message": "유효하지 않은 토큰입니다."
}
```

### 18.3 방장이 아닌 사용자로 신청 목록 조회

```http
GET {{baseUrl}}/api/studies/{{studyId}}/members
Authorization: Bearer {{accessToken}}
```

신청자 토큰으로 요청하면 아래 응답이 발생한다.

```json
{
  "success": false,
  "message": "스터디 방장만 처리할 수 있습니다."
}
```

---

## 19. 주의사항

1. H2는 인메모리 DB라서 서버를 끄면 데이터가 사라진다.
2. 서버를 재시작하면 회원가입부터 다시 진행해야 한다.
3. 생성/수정/삭제/참여 신청은 JWT 토큰이 필요하다.
4. 목록/상세 조회는 로그인하지 않아도 가능하다.
5. 참여 신청 목록 조회는 방장 토큰으로만 가능하다.
6. 참여 신청 승인/거절은 방장 토큰으로만 가능하다.
7. 승인된 신청은 다시 거절할 수 없다.
8. 거절된 신청은 다시 승인할 수 없다.

---

## 20. Postman 실전 설정 및 오류 해결

### 20.1 계정별 로그인 요청 분리

방장과 신청자를 반복해서 테스트할 때 하나의 로그인 Body를 주석 처리하며 바꾸지 않는다.

로그인 요청을 복제해 아래처럼 별도로 관리한다.

- `방장 로그인 요청`
- `신청자 로그인 요청`

방장 로그인 Body:

```json
{
  "email": "host@example.com",
  "password": "password123"
}
```

신청자 로그인 Body:

```json
{
  "email": "applicant@example.com",
  "password": "password123"
}
```

두 요청의 `Scripts -> Post-response`에는 동일한 스크립트를 설정한다.

```javascript
const json = pm.response.json();

if (json.success && json.data?.accessToken) {
    pm.environment.set("accessToken", json.data.accessToken);
}
```

- 방장 로그인 요청을 실행하면 `accessToken`이 방장 토큰으로 교체된다.
- 신청자 로그인 요청을 실행하면 `accessToken`이 신청자 토큰으로 교체된다.
- 로그인 API에는 `email`, `password`만 전달한다.

### 20.2 Collection 공통 Bearer 인증

`CodeMate` Collection의 `Authorization`에서 다음과 같이 설정한다.

```text
Auth Type: Bearer Token
Token: {{accessToken}}
```

인증이 필요한 개별 요청은 아래 설정을 사용한다.

```text
Auth Type: Inherit auth from parent
```

회원가입, 로그인, 스터디 목록/상세처럼 공개 API는 개별 요청에서 `No Auth`를 사용한다.

`Authorization` 탭과 `Headers` 탭에 인증값을 동시에 입력하지 않는다. 두 곳에 모두 설정하면 다음 경고가 표시된다.

```text
This is a duplicate header and will be overridden by the Authorization header generated by Postman.
```

이 경우 `Headers`에서 직접 만든 `Authorization` 행을 삭제하고 `Authorization` 탭만 사용한다.

### 20.3 안전한 studyId 저장

스터디 생성 요청의 `Scripts -> Post-response`:

```javascript
const json = pm.response.json();

if (json.success && json.data?.id) {
    pm.environment.set("studyId", json.data.id);
}
```

실패 응답에는 `data`가 없을 수 있으므로 성공 여부와 `data.id` 존재 여부를 확인한 후 저장한다.

### 20.4 안전한 memberId 저장

참여 신청 요청의 `Scripts -> Post-response`:

```javascript
const json = pm.response.json();

if (json.success && json.data?.id) {
    pm.environment.set("memberId", json.data.id);
}
```

중복 신청은 `409 Conflict`이며 응답에 `data`가 없다. 아래처럼 무조건 `json.data.id`를 읽으면 오류가 발생한다.

```text
TypeError: Cannot read properties of undefined (reading 'id')
```

이미 신청한 상태라면 방장 토큰으로 신청 목록을 조회해 `memberId`를 다시 저장할 수 있다.

```http
GET {{baseUrl}}/api/studies/{{studyId}}/members?status=PENDING
```

신청 목록 조회 요청의 `Scripts -> Post-response`:

```javascript
const json = pm.response.json();

if (json.success && Array.isArray(json.data) && json.data.length > 0) {
    pm.environment.set("memberId", json.data[0].id);
}
```

### 20.5 승인 URL에 memberId가 비어 있는 경우

Postman Console에 아래처럼 표시되면 `memberId`가 저장되지 않은 상태다.

```text
PATCH /api/studies/7/members//approve
```

정상 요청은 숫자 ID가 포함돼야 한다.

```text
PATCH /api/studies/7/members/3/approve
```

`CodeMate Local` 환경에서 `studyId`, `memberId` 값을 확인한 후 다시 요청한다.

### 20.6 401 로그인 필요 응답 확인

`401 Unauthorized`와 `"로그인이 필요합니다."` 응답은 방장 권한 검사 전에 JWT가 전달되지 않았다는 의미다.

확인 순서:

1. 오른쪽 위 선택 환경이 `CodeMate Local`인지 확인.
2. 방장 로그인 요청을 다시 실행.
3. `GET {{baseUrl}}/api/users/me`가 방장 정보로 `200 OK`인지 확인.
4. 승인 요청이 `Bearer Token {{accessToken}}` 또는 `Inherit auth from parent`인지 확인.
5. `Headers`에 중복 `Authorization`이 없는지 확인.
6. Postman Console에서 실제 요청 Header가 `Authorization: Bearer eyJ...` 형태인지 확인.

로그인 Post-response에서 저장값을 임시 확인할 수도 있다.

```javascript
console.log(pm.environment.get("accessToken"));
```

`eyJ...`로 시작하는 JWT가 출력되면 환경 변수 저장은 정상이다.

### 20.7 Query Parameter 뒤의 %0A

Console에 아래처럼 `%0A`가 붙으면 Query Parameter 값에 줄바꿈이 포함된 상태다.

```text
?status=PENDING%0A
```

Params 탭에서 값을 지우고 `PENDING`을 다시 직접 입력한다.

### 20.8 승인 테스트 최종 점검표

승인 요청 전 환경 변수:

```text
accessToken = 방장 로그인으로 발급한 JWT
studyId = 방장이 만든 스터디 ID
memberId = 신청자의 참여 신청 ID
```

승인 요청:

```http
PATCH {{baseUrl}}/api/studies/{{studyId}}/members/{{memberId}}/approve
```

정상 응답:

```json
{
  "success": true,
  "message": "스터디 참여 신청을 승인했습니다.",
  "data": {
    "status": "APPROVED"
  }
}
```

---

## 21. 내 스터디 신청 내역 조회

### 21.1 사전 준비

1. 신청자 계정으로 로그인한다.
2. 로그인 응답의 `accessToken`을 환경 변수에 저장한다.
3. 신청자 계정으로 하나 이상의 스터디에 참여 신청한다.

### 21.2 전체 신청 내역 조회

```http
GET {{baseUrl}}/api/users/me/study-applications
Authorization: Bearer {{accessToken}}
```

정상 응답 예시:

```json
{
  "success": true,
  "message": "내 스터디 신청 내역을 조회했습니다.",
  "data": [
    {
      "applicationId": 3,
      "studyId": 7,
      "studyTitle": "Spring Boot 스터디",
      "hostNickname": "host",
      "studyStatus": "RECRUITING",
      "applicationStatus": "PENDING",
      "appliedAt": "2026-06-08T10:30:00"
    }
  ]
}
```

### 21.3 상태별 조회

Params 탭의 `status`에 아래 값 중 하나를 입력한다.

```text
PENDING
APPROVED
REJECTED
```

요청 예시:

```http
GET {{baseUrl}}/api/users/me/study-applications?status=APPROVED
```

상태 의미:

- `PENDING`: 방장 승인 대기.
- `APPROVED`: 참여 승인 완료.
- `REJECTED`: 참여 신청 거절.

### 21.4 확인 사항

1. 신청자 토큰으로 요청한다.
2. `applicationId`는 참여 신청 ID이며 사용자 ID와 다르다.
3. 신청 내역이 없으면 HTTP `200`과 빈 배열 `data: []`가 반환된다.
4. 토큰 없이 요청하면 HTTP `401`이 반환된다.

---

## 22. 거절 후 재신청 테스트

### 22.1 테스트 순서

1. 신청자 토큰으로 스터디 참여 신청.
2. 방장 토큰으로 신청 거절.
3. 신청자 토큰으로 내 신청 내역 조회.
4. `applicationStatus`가 `REJECTED`인지 확인.
5. 신청자 토큰으로 같은 스터디에 다시 참여 신청.
6. 응답 상태가 `PENDING`인지 확인.

재신청 요청:

```http
POST {{baseUrl}}/api/studies/{{studyId}}/members
Authorization: Bearer {{accessToken}}
```

정상 응답 확인:

```json
{
  "success": true,
  "message": "스터디 참여 신청이 완료되었습니다.",
  "data": {
    "id": 3,
    "studyId": 7,
    "status": "PENDING"
  }
}
```

### 22.2 재신청 정책

1. `REJECTED` 신청만 다시 신청할 수 있다.
2. 기존 신청 데이터를 재사용하므로 `memberId`가 유지된다.
3. `PENDING` 상태에서 다시 신청하면 HTTP `409`가 반환된다.
4. `APPROVED` 상태에서 다시 신청하면 HTTP `409`가 반환된다.
5. 모집이 마감됐거나 정원이 가득 찬 스터디에는 재신청할 수 없다.

### 22.3 계정 전환 주의

1. 신청 및 재신청은 신청자 토큰 사용.
2. 승인 및 거절은 방장 토큰 사용.
3. 요청 전에 `/api/users/me`로 현재 토큰의 계정을 확인.
4. 로그인 요청의 Post-response 스크립트가 `accessToken`을 올바르게 교체했는지 확인.

---

## 23. MySQL 연결 및 데이터 영속성 테스트

### 23.1 MySQL 사전 준비

MySQL Workbench에서 로컬 MySQL 서버에 접속한 후 프로젝트 전용 데이터베이스와 계정을 준비한다.

```sql
CREATE DATABASE codemate
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER 'codemate'@'localhost' IDENTIFIED BY '비밀번호';
GRANT ALL PRIVILEGES ON codemate.* TO 'codemate'@'localhost';
FLUSH PRIVILEGES;
```

이미 `codemate` 계정이 존재한다면 `CREATE USER`는 생략하고 권한만 확인한다.
기존 MySQL 계정을 재사용할 수도 있지만 프로젝트 전용 계정을 사용하는 것을 권장한다.

### 23.2 MySQL 프로필 실행 확인

서버 시작 로그에서 아래 내용을 확인한다.

```text
The following 1 profile is active: "mysql"
Database JDBC URL [jdbc:mysql://localhost:3306/codemate...]
```

다음 로그가 표시되면 H2 프로필이 실행된 상태이므로 서버를 종료하고 프로필 옵션을 확인한다.

```text
Database JDBC URL [jdbc:h2:mem:codemate]
```

### 23.3 테이블 생성 확인

MySQL Workbench에서 실행한다.

```sql
USE codemate;
SHOW TABLES;
```

확인할 테이블:

- `users`
- `study`
- `study_members`
- `tech_stacks`
- `study_tech_stacks`

### 23.4 API 저장 결과 확인

Swagger 또는 Postman에서 회원가입, 스터디 생성, 참여 신청을 실행한 후 MySQL에서 확인한다.

```sql
SELECT * FROM users;
SELECT * FROM study;
SELECT * FROM study_members;
SELECT * FROM tech_stacks;
SELECT * FROM study_tech_stacks;
```

### 23.5 서버 재시작 후 데이터 유지 확인

1. MySQL 프로필로 회원가입과 스터디 생성을 완료한다.
2. 서버 실행 창에서 `Ctrl+C`로 서버를 종료한다.
3. 같은 터미널에서 MySQL 프로필로 서버를 다시 실행한다.
4. Swagger를 새로고침한다.
5. 기존 계정으로 다시 로그인한다.
6. 이전에 생성한 스터디가 목록과 상세 조회에서 유지되는지 확인한다.

cmd 환경변수 확인:

```cmd
echo %CODEMATE_DB_USERNAME%
echo %CODEMATE_DB_PASSWORD%
```

PowerShell 환경변수 확인:

```powershell
echo $env:CODEMATE_DB_USERNAME
echo $env:CODEMATE_DB_PASSWORD
```

서버 재시작 후에도 기존 회원과 스터디가 조회되면 데이터 영속성 검증이 완료된 것이다.
Swagger의 JWT 인증 상태는 서버 또는 페이지 재시작 후 유지되지 않으므로 로그인 후 새 토큰을 `Authorize`에 다시 입력한다.

### 23.6 참여 신청 상태 변경 확인

1. 신청자 토큰으로 참여 신청.
2. 방장 토큰으로 신청 거절.
3. 신청자 토큰으로 같은 스터디에 재신청.
4. 방장 토큰으로 재신청 승인.
5. 신청자 토큰으로 내 신청 내역 조회.
6. `applicationStatus=APPROVED` 확인.

MySQL에서도 상태를 확인할 수 있다.

```sql
SELECT id, study_id, user_id, status, created_at, updated_at
FROM study_members
ORDER BY id;
```

거절 후 재신청은 새 행을 생성하지 않고 기존 신청의 상태를 다시 `PENDING`으로 변경하므로 동일한 `id`가 유지된다.

### 23.7 MySQL 검증 완료 기준

1. `mysql` 프로필로 서버가 정상 실행됨.
2. Hibernate가 MySQL에 테이블을 생성함.
3. API로 생성한 데이터가 MySQL Workbench에서 조회됨.
4. 서버 재시작 후에도 데이터가 유지됨.
5. 신청 거절 → 재신청 → 승인 흐름이 정상 처리됨.
6. 내 신청 내역에서 최종 상태가 `APPROVED`로 조회됨.

---

## 24. Docker Compose 서버 테스트

### 24.1 사전 준비

1. Docker Desktop을 실행한다.
2. 프로젝트 루트에서 Docker 명령이 동작하는지 확인한다.

```powershell
docker --version
docker compose version
```

### 24.2 환경변수 파일 생성

PowerShell:

```powershell
Copy-Item .env.example .env
```

cmd:

```cmd
copy .env.example .env
```

`.env`에서 다음 값을 실제 로컬 개발용 값으로 변경한다.

```dotenv
CODEMATE_DB_PASSWORD=MySQL 사용자 비밀번호
MYSQL_ROOT_PASSWORD=MySQL root 비밀번호
CODEMATE_JWT_SECRET=Base64 JWT Secret
```

`.env`는 `.gitignore`에 등록되어 원격 저장소에 포함되지 않는다.

### 24.3 컨테이너 실행

```powershell
docker compose up --build -d
```

구성:

- `codemate-app`: Spring Boot 애플리케이션.
- `codemate-mysql`: MySQL 8.4.
- `codemate-mysql-data`: MySQL 데이터 영속 볼륨.

기본 포트:

| 서비스 | 컨테이너 포트 | 호스트 포트 |
|---|---:|---:|
| Spring Boot | `8080` | `8080` |
| MySQL | `3306` | `3307` |

로컬 MySQL의 기본 포트 `3306`과 충돌하지 않도록 Docker MySQL은 호스트 `3307`을 사용한다.

### 24.4 실행 상태 확인

```powershell
docker compose ps
```

두 서비스가 모두 `healthy`인지 확인한다.

애플리케이션 Health Check:

```text
http://localhost:8080/actuator/health
```

정상 응답:

```json
{
  "status": "UP"
}
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

### 24.5 로그 확인

전체 로그:

```powershell
docker compose logs -f
```

애플리케이션 로그:

```powershell
docker compose logs -f app
```

MySQL 로그:

```powershell
docker compose logs -f mysql
```

로그 출력을 종료할 때는 `Ctrl+C`를 누른다. 컨테이너는 백그라운드에서 계속 실행된다.

### 24.6 컨테이너 종료와 재실행

컨테이너 종료:

```powershell
docker compose down
```

MySQL 볼륨은 유지되므로 다시 실행해도 데이터가 남는다.

```powershell
docker compose up -d
```

### 24.7 MySQL 데이터 초기화

아래 명령은 컨테이너와 MySQL 데이터 볼륨을 함께 삭제한다.

```powershell
docker compose down -v
```

회원, 스터디, 참여 신청 데이터가 모두 삭제되므로 초기화가 필요한 경우에만 사용한다.

### 24.8 포트 충돌 해결

호스트의 `8080` 포트를 이미 사용 중이면 `.env`를 수정한다.

```dotenv
APP_PORT=8081
```

이 경우 Swagger 주소:

```text
http://localhost:8081/swagger-ui/index.html
```

Docker MySQL의 외부 포트를 변경하려면:

```dotenv
MYSQL_HOST_PORT=3308
```

애플리케이션 컨테이너는 Docker 내부에서 `mysql:3306`으로 연결되므로 외부 포트 변경의 영향을 받지 않는다.

### 24.9 Docker 검증 순서

1. `docker compose up --build -d`.
2. `docker compose ps`에서 MySQL과 앱의 `healthy` 확인.
3. `/actuator/health`에서 `UP` 확인.
4. Swagger UI 접속.
5. 회원가입, 로그인, 스터디 생성.
6. `docker compose down`.
7. `docker compose up -d`.
8. 기존 계정 로그인 및 스터디 데이터 유지 확인.
