# CodeMate Postman 실행 가이드

## 1. 서버 실행

### 1.1 프로젝트 경로 이동

```powershell
cd C:\KOSTA_Projects\3_CodeMate
```

### 1.2 서버 실행

```powershell
.\mvnw.cmd spring-boot:run
```

### 1.3 서버 주소

```text
http://localhost:8080
```

---

## 2. Postman 기본 설정

### 2.1 Environment 생성

Postman에서 `CodeMate Local` 환경을 만들고 아래 변수를 등록한다.

| 변수명 | 초기값 | 설명 |
|---|---|---|
| `baseUrl` | `http://localhost:8080` | 로컬 서버 주소 |
| `accessToken` | 비워두기 | 로그인 후 JWT 저장 |
| `studyId` | 비워두기 | 생성된 스터디 ID 저장 |
| `memberId` | 비워두기 | 생성된 참여 신청 ID 저장 |

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

### 8.3 정상 응답 구조

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
