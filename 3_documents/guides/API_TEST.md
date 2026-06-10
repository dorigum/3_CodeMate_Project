# CodeMate API 테스트

Swagger와 Postman을 사용한 회원, 인증, 스터디 및 참여 기능 테스트 절차를 정리한다.

[실행 가이드 목차](CodeMate_실행_가이드.md)로 돌아가기

## Postman 기본 설정

### Environment 생성

Postman에서 `CodeMate Local` 환경을 만들고 아래 변수를 등록한다.

| 변수명           | 초기값                     | 설명              |
| ------------- | ----------------------- | --------------- |
| `baseUrl`     | `http://localhost:8080` | 로컬 서버 주소        |
| `accessToken` | 비워두기                    | 로그인 후 JWT 저장    |
| `studyId`     | 비워두기                    | 생성된 스터디 ID 저장   |
| `memberId`    | 비워두기                    | 생성된 참여 신청 ID 저장 |

운영 배포를 테스트할 때는 별도의 `CodeMate Production` 환경을 만들고 `baseUrl`만 다음 값으로 변경한다.

```text
https://polar-bear.o-r.kr
```

운영 환경에서는 Swagger UI가 비활성화되어 있으므로 Postman 요청을 사용한다.

### 공통 Header

JSON Body를 보내는 요청에는 아래 Header를 추가한다.

```text
Content-Type: application/json
```

로그인이 필요한 요청에는 아래 Header를 추가한다.

```text
Authorization: Bearer {{accessToken}}
```

---

## AWS 운영 API Smoke Test

### 1. Health Check

```http
GET https://polar-bear.o-r.kr/actuator/health
```

`200 OK`와 `status: UP`을 확인한다.

### 2. 회원가입

```http
POST {{baseUrl}}/api/users/signup
Content-Type: application/json
```

```json
{
  "email": "production-test@example.com",
  "password": "password123",
  "nickname": "production-test",
  "mainTechStack": "Spring Boot"
}
```

회원가입이 성공하면 운영 도메인, ALB, EC2 애플리케이션과 MySQL 저장 경로가 연결된 상태이다. 같은 이메일을 반복 사용하면 중복 오류가 발생하므로 테스트마다 이메일을 변경한다.

### 3. 인증 흐름

1. 로그인 API로 Access Token을 발급한다.
2. Postman 환경의 `accessToken` 변수에 저장한다.
3. 인증이 필요한 요청에 `Authorization: Bearer {{accessToken}}`을 추가한다.
4. 내 정보 조회, 모집 글 생성과 참여 관리 API를 순서대로 확인한다.

### 4. 데이터 영속성

1. 운영 API로 회원 또는 모집 글을 생성한다.
2. EC2에서 애플리케이션 컨테이너를 재시작한다.
3. 로그인 후 기존 데이터를 다시 조회한다.
4. 데이터가 유지되면 MySQL named volume의 영속성이 정상이다.

운영 배포 명령과 AWS 구성은 [AWS 배포 문서](../info/AWS_DEPLOYMENT.md)를 참고한다.

---

## 회원가입

### 요청

```http
POST {{baseUrl}}/api/users/signup
```

### Body

```json
{
  "email": "host@example.com",
  "password": "password123",
  "nickname": "host",
  "mainTechStack": "Spring Boot"
}
```

### 정상 응답

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

### 추가 테스트

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

## 로그인

### 요청

```http
POST {{baseUrl}}/api/users/login
```

### Body

```json
{
  "email": "host@example.com",
  "password": "password123"
}
```

### 정상 응답

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

### accessToken 저장

응답의 `data.accessToken` 값을 복사해서 Postman 환경 변수 `accessToken`에 저장한다.

Tests 탭에 아래 스크립트를 넣으면 자동 저장할 수 있다.

```javascript
const json = pm.response.json();
pm.environment.set("accessToken", json.data.accessToken);
```

---

## 내 정보 조회

### 요청

```http
GET {{baseUrl}}/api/users/me
```

### Header

```text
Authorization: Bearer {{accessToken}}
```

### 정상 응답

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

## 스터디 생성

### 요청

```http
POST {{baseUrl}}/api/studies
```

### Header

```text
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

### Body

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

### 정상 응답

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

### studyId 저장

응답의 `data.id` 값을 복사해서 Postman 환경 변수 `studyId`에 저장한다.

Tests 탭에 아래 스크립트를 넣으면 자동 저장할 수 있다.

```javascript
const json = pm.response.json();
pm.environment.set("studyId", json.data.id);
```

---

## 스터디 목록 조회

### 전체 조회

```http
GET {{baseUrl}}/api/studies
```

### 조건 조회

```http
GET {{baseUrl}}/api/studies?category=STUDY&status=RECRUITING&page=0&size=10
```

### 복합 검색

```http
GET {{baseUrl}}/api/studies?keyword=백엔드&category=STUDY&status=RECRUITING&meetingType=OFFLINE&location=판교&techStack=Kotlin&page=0&size=10
```

사용 가능한 검색 조건:

| 조건 | 설명 | 예시 |
|---|---|---|
| `keyword` | 제목 또는 내용 부분 검색 | `백엔드` |
| `category` | 모집 글 분류 | `STUDY`, `MOGAKKO` |
| `status` | 모집 상태 | `RECRUITING`, `CLOSED`, `IN_PROGRESS`, `FINISHED` |
| `meetingType` | 진행 방식 | `ONLINE`, `OFFLINE` |
| `location` | 지역 부분 검색 | `판교` |
| `techStack` | 기술 스택 이름 부분 검색 | `Kotlin` |
| `page` | 페이지 번호, 0부터 시작 | `0` |
| `size` | 페이지 크기 | `10` |

검색 조건은 필요한 값만 전달할 수 있으며 여러 조건을 함께 사용하면 모든 조건을 만족하는 모집 글만 반환된다.

### 정상 응답 구조

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

## 스터디 상세 조회

### 요청

```http
GET {{baseUrl}}/api/studies/{{studyId}}
```

### 특징

- 로그인하지 않아도 조회 가능하다.
- `studyId`는 스터디 생성 응답의 `data.id` 값을 사용한다.

---

## 스터디 수정

### 요청

```http
PATCH {{baseUrl}}/api/studies/{{studyId}}
```

### Header

```text
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

### Body

```json
{
  "title": "Spring Boot 심화 스터디",
  "content": "JPA와 Security까지 함께 다룹니다.",
  "category": "STUDY",
  "meetingType": "OFFLINE",
  "location": "강남",
  "maxMemberCount": 5,
  "techStackNames": ["Java", "Spring Security"]
}
```

### 권한 규칙

- 로그인 필요.
- 스터디 방장만 수정 가능.
- 모집 상태는 일반 수정 API에서 변경하지 않는다.
- `techStackNames`를 보내면 기존 기술 스택 연결이 요청값 기준으로 교체된다.
- 다른 사용자의 토큰으로 요청하면 403 응답이 발생한다.

### 스터디 모집 수동 마감

방장 토큰을 사용해 아래 요청을 실행한다.

```http
PATCH {{baseUrl}}/api/studies/{{studyId}}/close
Authorization: Bearer {{accessToken}}
```

요청 Body는 사용하지 않는다.

성공 시 확인할 값:

- HTTP 상태 코드 `200`.
- 응답의 `data.status`가 `CLOSED`.
- 마감 이후 다른 사용자가 참여 신청하면 HTTP `400`.
- 이미 마감된 스터디를 다시 마감하면 HTTP `409`.
- 방장이 아닌 사용자가 요청하면 HTTP `403`.

모집 마감 시간에 따른 자동 마감 기능은 아직 포함하지 않으며, 현재는 정원 도달 또는 방장의 수동 요청으로 모집을 마감한다.

---

## 스터디 삭제

### 요청

```http
DELETE {{baseUrl}}/api/studies/{{studyId}}
```

### Header

```text
Authorization: Bearer {{accessToken}}
```

### 권한 규칙

- 로그인 필요.
- 스터디 방장만 삭제 가능.
- 삭제 후 같은 `studyId`로 상세 조회하면 404 응답이 발생한다.

---

## 스터디 참여 신청

### 사전 준비

1. 방장 계정으로 회원가입한다.
2. 방장 계정으로 로그인한다.
3. 방장 토큰으로 스터디를 생성한다.
4. 신청자 계정으로 회원가입한다.
5. 신청자 계정으로 로그인한다.
6. 환경 변수 `accessToken`을 신청자 토큰으로 교체한다.

### 요청

```http
POST {{baseUrl}}/api/studies/{{studyId}}/members
```

### Header

```text
Authorization: Bearer {{accessToken}}
```

### Body

없음.

### 정상 응답

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

### 실패 케이스

#### 본인 글 신청

방장 토큰으로 참여 신청하면 실패한다.

```json
{
  "success": false,
  "message": "본인이 만든 스터디에는 참여 신청할 수 없습니다."
}
```

#### 중복 신청

같은 신청자 토큰으로 한 번 더 신청하면 실패한다.

```json
{
  "success": false,
  "message": "이미 참여 신청한 스터디입니다."
}
```

#### 비로그인 신청

`Authorization` Header 없이 신청하면 인증 실패가 발생한다.

---

## 추천 테스트 순서

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

## 참여 신청 목록 조회

### 사전 준비

1. 방장 계정으로 스터디를 생성한다.
2. 신청자 계정으로 참여 신청을 보낸다.
3. `accessToken` 환경 변수를 방장 토큰으로 다시 교체한다.

### 전체 목록 요청

```http
GET {{baseUrl}}/api/studies/{{studyId}}/members
```

### 상태별 목록 요청

```http
GET {{baseUrl}}/api/studies/{{studyId}}/members?status=PENDING
```

사용 가능한 status 값:

- `PENDING`
- `APPROVED`
- `REJECTED`

### Header

```text
Authorization: Bearer {{accessToken}}
```

### 정상 응답

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

### 권한 규칙

- 로그인 필요.
- 스터디 방장만 신청 목록을 조회할 수 있다.
- 신청자 토큰으로 조회하면 403 응답이 발생한다.

---

## 참여 신청 승인

### 사전 준비

1. 방장 계정으로 스터디를 생성한다.
2. 신청자 계정으로 참여 신청을 보낸다.
3. 참여 신청 응답의 `data.id` 값을 `memberId` 환경 변수에 저장한다.
4. `accessToken` 환경 변수를 방장 토큰으로 다시 교체한다.

### 요청

```http
PATCH {{baseUrl}}/api/studies/{{studyId}}/members/{{memberId}}/approve
```

### Header

```text
Authorization: Bearer {{accessToken}}
```

### 정상 응답

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

### 승인 후 확인

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

## 참여 신청 거절

### 사전 준비

1. 방장 계정으로 스터디를 생성한다.
2. 신청자 계정으로 참여 신청을 보낸다.
3. 참여 신청 응답의 `data.id` 값을 `memberId` 환경 변수에 저장한다.
4. `accessToken` 환경 변수를 방장 토큰으로 다시 교체한다.

### 요청

```http
PATCH {{baseUrl}}/api/studies/{{studyId}}/members/{{memberId}}/reject
```

### Header

```text
Authorization: Bearer {{accessToken}}
```

### 정상 응답

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

### 거절 후 확인

- `currentMemberCount`는 증가하지 않는다.
- 스터디 상태는 그대로 유지된다.

---

## 참여 신청 처리 실패 케이스

### 방장이 아닌 사용자 승인/거절

신청자 토큰으로 승인/거절 API를 호출하면 실패한다.

```json
{
  "success": false,
  "message": "스터디 방장만 처리할 수 있습니다."
}
```

### 이미 처리된 신청 재처리

이미 승인 또는 거절된 신청을 다시 처리하면 실패한다.

```json
{
  "success": false,
  "message": "대기 중인 신청만 처리할 수 있습니다."
}
```

### 정원 초과 승인

이미 정원이 찬 스터디에서 추가 승인을 시도하면 실패한다.

```json
{
  "success": false,
  "message": "모집 인원이 마감되었습니다."
}
```

---

## 인증/인가 실패 응답 확인

### 토큰 없이 인증 필요 API 호출

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

### 잘못된 토큰으로 API 호출

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

### 방장이 아닌 사용자로 신청 목록 조회

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

## 주의사항

1. H2는 인메모리 DB라서 서버를 끄면 데이터가 사라진다.
2. 서버를 재시작하면 회원가입부터 다시 진행해야 한다.
3. 생성/수정/삭제/참여 신청은 JWT 토큰이 필요하다.
4. 목록/상세 조회는 로그인하지 않아도 가능하다.
5. 참여 신청 목록 조회는 방장 토큰으로만 가능하다.
6. 참여 신청 승인/거절은 방장 토큰으로만 가능하다.
7. 승인된 신청은 다시 거절할 수 없다.
8. 거절된 신청은 다시 승인할 수 없다.

---

## Postman 실전 설정 및 오류 해결

### 계정별 로그인 요청 분리

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

### Collection 공통 Bearer 인증

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

### 안전한 studyId 저장

스터디 생성 요청의 `Scripts -> Post-response`:

```javascript
const json = pm.response.json();

if (json.success && json.data?.id) {
    pm.environment.set("studyId", json.data.id);
}
```

실패 응답에는 `data`가 없을 수 있으므로 성공 여부와 `data.id` 존재 여부를 확인한 후 저장한다.

### 안전한 memberId 저장

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

### 승인 URL에 memberId가 비어 있는 경우

Postman Console에 아래처럼 표시되면 `memberId`가 저장되지 않은 상태다.

```text
PATCH /api/studies/7/members//approve
```

정상 요청은 숫자 ID가 포함돼야 한다.

```text
PATCH /api/studies/7/members/3/approve
```

`CodeMate Local` 환경에서 `studyId`, `memberId` 값을 확인한 후 다시 요청한다.

### 401 로그인 필요 응답 확인

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

### Query Parameter 뒤의 %0A

Console에 아래처럼 `%0A`가 붙으면 Query Parameter 값에 줄바꿈이 포함된 상태다.

```text
?status=PENDING%0A
```

Params 탭에서 값을 지우고 `PENDING`을 다시 직접 입력한다.

### 승인 테스트 최종 점검표

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

## 내 스터디 신청 내역 조회

### 사전 준비

1. 신청자 계정으로 로그인한다.
2. 로그인 응답의 `accessToken`을 환경 변수에 저장한다.
3. 신청자 계정으로 하나 이상의 스터디에 참여 신청한다.

### 전체 신청 내역 조회

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

### 상태별 조회

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

### 확인 사항

1. 신청자 토큰으로 요청한다.
2. `applicationId`는 참여 신청 ID이며 사용자 ID와 다르다.
3. 신청 내역이 없으면 HTTP `200`과 빈 배열 `data: []`가 반환된다.
4. 토큰 없이 요청하면 HTTP `401`이 반환된다.

---

## 거절 후 재신청 테스트

### 테스트 순서

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

### 재신청 정책

1. `REJECTED` 신청만 다시 신청할 수 있다.
2. 기존 신청 데이터를 재사용하므로 `memberId`가 유지된다.
3. `PENDING` 상태에서 다시 신청하면 HTTP `409`가 반환된다.
4. `APPROVED` 상태에서 다시 신청하면 HTTP `409`가 반환된다.
5. 모집이 마감됐거나 정원이 가득 찬 스터디에는 재신청할 수 없다.

### 계정 전환 주의

1. 신청 및 재신청은 신청자 토큰 사용.
2. 승인 및 거절은 방장 토큰 사용.
3. 요청 전에 `/api/users/me`로 현재 토큰의 계정을 확인.
4. 로그인 요청의 Post-response 스크립트가 `accessToken`을 올바르게 교체했는지 확인.

---

## 회원 정보 수정 및 비밀번호 변경

### 사전 준비

1. 회원가입과 로그인을 진행한다.
2. 로그인 응답의 `data.accessToken`을 복사한다.
3. Swagger UI의 `Authorize`에 access token 값만 입력한다.
4. Postman에서는 `Authorization: Bearer {accessToken}` Header를 추가한다.

### 회원 정보 수정

```http
PATCH /api/users/me
Authorization: Bearer {accessToken}
Content-Type: application/json
```

요청 예시:

```json
{
  "nickname": "코드메이트2",
  "mainTechStack": "Java, Spring Boot"
}
```

확인 항목:

1. 성공 시 HTTP `200`.
2. 응답의 `nickname`, `mainTechStack`이 변경된 값인지 확인.
3. 다른 회원이 사용 중인 닉네임은 HTTP `409`.
4. 기존 닉네임을 그대로 사용하면서 기술 스택만 변경하는 요청은 허용.
5. 이메일은 로그인 식별자이므로 이번 수정 API에서 변경하지 않음.

### 비밀번호 변경

```http
PATCH /api/users/me/password
Authorization: Bearer {accessToken}
Content-Type: application/json
```

요청 예시:

```json
{
  "currentPassword": "strongPassword1!",
  "newPassword": "newStrongPassword2!"
}
```

확인 항목:

1. 성공 시 HTTP `200`.
2. 현재 비밀번호가 틀리면 HTTP `400`.
3. 새 비밀번호가 현재 비밀번호와 같으면 HTTP `400`.
4. 새 비밀번호는 8자 이상 30자 이하.
5. 변경 후 기존 비밀번호 로그인은 HTTP `401`.
6. 변경한 새 비밀번호 로그인은 HTTP `200`과 새 access token 반환.
7. DB에는 새 비밀번호 원문이 아닌 BCrypt 해시가 저장됨.

### 자동화 테스트

회원 기능 보강 테스트만 실행:

```powershell
.\mvnw.cmd "-Dtest=UserProfileIntegrationTest" test
```

전체 테스트 실행:

```powershell
.\mvnw.cmd verify
```

`UserProfileIntegrationTest` 확인 범위:

1. 닉네임과 주요 기술 스택 수정.
2. 현재 닉네임 유지.
3. 다른 회원의 닉네임 사용 차단.
4. 인증 및 요청값 검증.
5. 현재 비밀번호 불일치와 동일 비밀번호 변경 차단.
6. BCrypt 재암호화와 새 비밀번호 로그인.

### 현재 토큰 정책

1. 비밀번호 변경 시 기존 access token과 refresh token이 모두 무효화된다.
2. 변경 직후 새 비밀번호로 다시 로그인하여 새 토큰 쌍을 발급받아야 한다.

---

## JWT 재발급 및 로그아웃

### 토큰 구성

1. Access Token
   - 인증이 필요한 API의 `Authorization` Header에 사용.
   - 기본 유효 시간 1시간.
   - JWT `tokenType` 값은 `ACCESS`.
2. Refresh Token
   - Access Token 재발급에만 사용.
   - 기본 유효 시간 14일.
   - JWT `tokenType` 값은 `REFRESH`.
3. Access Token과 Refresh Token은 서로의 용도로 사용할 수 없음.
4. 로그인 응답의 `accessTokenExpiresIn`, `refreshTokenExpiresIn`은 초 단위.

### 로그인 응답 확인

```json
{
  "success": true,
  "message": "로그인이 완료되었습니다.",
  "data": {
    "tokenType": "Bearer",
    "accessToken": "Access Token",
    "refreshToken": "Refresh Token",
    "accessTokenExpiresIn": 3600,
    "refreshTokenExpiresIn": 1209600
  }
}
```

Refresh Token은 인증 Header에 넣지 않고 재발급 요청 Body에만 사용한다.

### 토큰 재발급

```http
POST /api/users/token/refresh
Content-Type: application/json
```

```json
{
  "refreshToken": "로그인에서 받은 Refresh Token"
}
```

확인 항목:

1. 성공 시 HTTP `200`과 새 Access Token·Refresh Token 반환.
2. 재발급이 성공하면 요청에 사용한 이전 Refresh Token은 즉시 폐기.
3. 이전 Refresh Token 재사용 시 HTTP `401`.
4. Access Token을 재발급 API에 전달하면 HTTP `401`.
5. 만료되거나 변조된 Refresh Token은 HTTP `401`.
6. 재발급 조회에는 비관적 락을 적용해 동일 Refresh Token의 동시 사용을 차단.

### 로그아웃

```http
POST /api/users/logout
Authorization: Bearer {accessToken}
```

확인 항목:

1. 성공 시 HTTP `200`.
2. DB에 저장된 사용자의 Refresh Token 삭제.
3. 사용자 `tokenVersion` 증가.
4. 로그아웃 전에 발급된 Access Token은 이후 요청에서 HTTP `401`.
5. 로그아웃 전에 발급된 Refresh Token도 재발급 요청에서 HTTP `401`.

### Refresh Token 저장 정책

1. Refresh Token 원문은 DB에 저장하지 않음.
2. SHA-256 해시만 `refresh_tokens.token_hash`에 저장.
3. 사용자당 활성 Refresh Token은 1개.
4. 다시 로그인하거나 재발급하면 저장된 Refresh Token이 새 값으로 교체됨.
5. 탈취된 DB만으로 Refresh Token 원문을 바로 사용할 수 없도록 구성.

### 토큰 만료 시간 환경변수

```text
CODEMATE_JWT_ACCESS_TOKEN_VALIDITY_MILLISECONDS=3600000
CODEMATE_JWT_REFRESH_TOKEN_VALIDITY_MILLISECONDS=1209600000
```

운영 환경에서는 서비스 정책에 따라 값을 조정할 수 있다.

### 자동화 테스트

```powershell
.\mvnw.cmd "-Dtest=JwtTokenLifecycleIntegrationTest" test
```

확인 범위:

1. 로그인 토큰 쌍과 만료 시간 반환.
2. Refresh Token 원문 미저장.
3. 토큰 재발급과 Refresh Token 회전.
4. 이전 Refresh Token 재사용 차단.
5. Access Token과 Refresh Token 용도 혼용 차단.
6. 로그아웃 후 기존 토큰 무효화.
7. 비밀번호 변경 후 기존 토큰 무효화.

---

## 참여 신청 취소 및 스터디 탈퇴

### 참여 신청 취소

승인 대기 중인 사용자는 자신의 참여 신청을 취소할 수 있다.

```http
DELETE /api/studies/{studyId}/members/me/application
Authorization: Bearer {accessToken}
```

1. 참여 신청 API로 신청을 생성한다.
2. 내 신청 내역 조회에서 상태가 `PENDING`인지 확인한다.
3. 참여 신청 취소 API를 실행한다.
4. 내 신청 내역에서 해당 신청이 삭제됐는지 확인한다.
5. 같은 스터디에 다시 신청할 수 있는지 확인한다.

`APPROVED` 또는 `REJECTED` 상태의 신청에는 취소 API를 사용할 수 없다.

### 승인된 스터디 탈퇴

참여 승인을 받은 사용자는 스터디에서 탈퇴할 수 있다.

```http
DELETE /api/studies/{studyId}/members/me/membership
Authorization: Bearer {accessToken}
```

1. 참여 신청 후 방장 계정으로 신청을 승인한다.
2. 참여자 계정으로 다시 로그인하고 토큰을 등록한다.
3. 스터디 탈퇴 API를 실행한다.
4. 스터디 상세 조회에서 현재 참여 인원이 1명 감소했는지 확인한다.
5. 내 신청 내역에서 해당 참여 기록이 삭제됐는지 확인한다.

`PENDING` 또는 `REJECTED` 상태에는 탈퇴 API를 사용할 수 없다.

### 모집 상태 확인

1. 정원 도달로 자동 마감된 스터디에서 승인 회원이 탈퇴하면 빈자리가 생기고 모집 상태가 `RECRUITING`으로 돌아간다.
2. 방장이 수동으로 마감한 스터디는 승인 회원이 탈퇴해도 `CLOSED` 상태를 유지한다.
3. 방장은 참여 취소 또는 탈퇴 API의 대상이 아니다.

### 자동화 테스트

```powershell
.\mvnw.cmd "-Dtest=StudyMemberStatusTransitionIntegrationTest" test
```

Flyway V3에서는 자동 마감과 수동 마감을 구분하기 위해 `study.recruitment_closed_manually` 컬럼을 추가한다.
