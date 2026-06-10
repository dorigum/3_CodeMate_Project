# CodeMate 데이터베이스 테스트

MySQL, Flyway, Testcontainers와 자동화 테스트 실행 방법을 정리한다.

[실행 가이드 목차](CodeMate_실행_가이드.md)로 돌아가기

## MySQL 연결 및 데이터 영속성 테스트

### MySQL 사전 준비

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

### MySQL 프로필 실행 확인

서버 시작 로그에서 아래 내용을 확인한다.

```text
The following 1 profile is active: "mysql"
Database JDBC URL [jdbc:mysql://localhost:3306/codemate...]
```

다음 로그가 표시되면 H2 프로필이 실행된 상태이므로 서버를 종료하고 프로필 옵션을 확인한다.

```text
Database JDBC URL [jdbc:h2:mem:codemate]
```

### 테이블 생성 확인

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

### API 저장 결과 확인

Swagger 또는 Postman에서 회원가입, 스터디 생성, 참여 신청을 실행한 후 MySQL에서 확인한다.

```sql
SELECT * FROM users;
SELECT * FROM study;
SELECT * FROM study_members;
SELECT * FROM tech_stacks;
SELECT * FROM study_tech_stacks;
```

### 서버 재시작 후 데이터 유지 확인

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

### 참여 신청 상태 변경 확인

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

### MySQL 검증 완료 기준

1. `mysql` 프로필로 서버가 정상 실행됨.
2. Flyway가 MySQL에 스키마를 생성하거나 기존 스키마를 baseline으로 등록함.
3. API로 생성한 데이터가 MySQL Workbench에서 조회됨.
4. 서버 재시작 후에도 데이터가 유지됨.
5. 신청 거절 → 재신청 → 승인 흐름이 정상 처리됨.
6. 내 신청 내역에서 최종 상태가 `APPROVED`로 조회됨.

---

## Flyway 마이그레이션 확인

### Flyway 역할

1. 애플리케이션 시작 시 DB의 현재 스키마 버전을 확인한다.
2. 아직 적용되지 않은 SQL 파일을 버전 순서대로 실행한다.
3. 실행 결과와 체크섬을 `flyway_schema_history` 테이블에 기록한다.
4. Flyway 적용이 끝난 후 Hibernate가 엔티티와 실제 테이블 구조의 일치 여부를 검증한다.

현재 Hibernate 설정은 `ddl-auto=validate`이므로 테이블을 자동 생성하거나 수정하지 않는다.

### 마이그레이션 파일 위치

H2:

```text
src/main/resources/db/migration/h2
```

MySQL:

```text
src/main/resources/db/migration/mysql
```

현재 초기 버전:

```text
V1__create_initial_schema.sql
```

H2와 MySQL은 긴 문자열과 날짜 타입 표현이 다를 수 있으므로 DB별 경로를 분리한다. 두 경로의 파일 버전과 논리적 변경 내용은 항상 동일하게 유지한다.

### 새 마이그레이션 작성 규칙

1. 이미 적용된 `V1` 파일은 수정하지 않는다.
2. DB 구조 변경은 다음 번호의 파일로 추가한다.
3. 버전과 설명 사이에는 밑줄 두 개를 사용한다.
4. H2와 MySQL 경로에 같은 버전의 파일을 함께 추가한다.
5. 엔티티 변경과 SQL 마이그레이션을 같은 작업 단위로 관리한다.

예시:

```text
db/migration/h2/V2__add_user_profile_image.sql
db/migration/mysql/V2__add_user_profile_image.sql
```

```sql
ALTER TABLE users ADD COLUMN profile_image_url VARCHAR(500);
```

한 번 적용된 파일을 수정하면 Flyway 체크섬 검증 오류가 발생한다. 이 경우 기존 파일을 고치는 대신 새 버전의 보정 마이그레이션을 작성한다.

### 기존 MySQL 데이터베이스 전환

기존 MySQL에는 Hibernate가 만든 테이블이 이미 있으므로 다음 설정을 사용한다.

```properties
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=1
```

동작:

1. 비어 있는 새 DB는 V1 SQL을 직접 실행한다.
2. 테이블이 있지만 Flyway 이력이 없는 기존 DB는 현재 구조를 V1 baseline으로 등록한다.
3. 기존 회원, 스터디, 참여 신청 데이터는 삭제하지 않는다.
4. baseline 등록 이후 Hibernate가 엔티티와 테이블 구조를 검증한다.

모든 개발·배포 환경에 `flyway_schema_history`가 생성된 뒤에는 자동 baseline 설정 제거를 검토한다. 운영 환경에서 잘못된 스키마를 정상 상태로 간주하지 않도록 하기 위함이다.

### MySQL 적용 이력 조회

MySQL Workbench:

```sql
SELECT installed_rank, version, description, type, success, installed_on
FROM flyway_schema_history
ORDER BY installed_rank;
```

새 DB에서 V1 SQL이 실행된 경우:

```text
version=1
type=SQL
success=1
```

기존 DB가 전환된 경우:

```text
version=1
type=BASELINE
success=1
```

Docker MySQL:

```powershell
docker exec codemate-mysql mysql `
  -u codemate `
  -p codemate `
  -e "SELECT installed_rank, version, description, type, success FROM flyway_schema_history;"
```

`-p` 뒤에는 `.env`의 `CODEMATE_DB_PASSWORD` 값을 입력하거나 명령 실행 후 비밀번호를 입력한다.

### 정상 실행 로그

새 DB에서는 다음 흐름의 로그가 출력된다.

```text
Migrating schema ... to version "1 - create initial schema"
Successfully applied 1 migration
Started CodeMateApplication
```

이후 재실행에서는 새 마이그레이션이 없으므로 현재 버전이 최신 상태라는 로그가 출력된다.

### Flyway 오류 확인 기준

1. `Validate failed`
   - 이미 적용된 SQL 파일의 내용이 변경되어 체크섬이 달라진 경우.
2. `Schema validation: missing table`
   - 엔티티는 존재하지만 마이그레이션에 테이블이 없는 경우.
3. `wrong column type`
   - 엔티티 타입과 SQL 컬럼 타입이 다른 경우.
4. `Found non-empty schema but no schema history table`
   - 기존 DB에 baseline 설정 없이 Flyway를 처음 적용한 경우.

오류가 발생하면 기존 V1 파일을 수정하기 전에 DB 이력과 현재 적용 환경부터 확인한다.

---

## Flyway 적용 프로젝트 통합 테스트

### 자동 테스트

프로젝트 루트에서 실행한다.

PowerShell:

```powershell
.\mvnw.cmd test
```

cmd:

```cmd
mvnw.cmd test
```

이 테스트는 별도 MySQL 설치 없이 H2 인메모리 DB를 사용한다.

검증 항목:

1. Flyway가 H2에 V1 마이그레이션 적용.
2. `flyway_schema_history` 현재 버전이 1인지 확인.
3. Hibernate `ddl-auto=validate` 통과.
4. 회원가입, 로그인, 스터디 CRUD, 참여 신청 흐름.
5. Swagger/OpenAPI Schema 생성.
6. 필드별 검증 오류와 잘못된 enum 공통 응답.

정상 결과:

```text
Tests run: 22, Failures: 0, Errors: 0
BUILD SUCCESS
```

### Docker MySQL 실행

Docker Desktop을 실행하고 프로젝트 루트에서 다음 명령을 실행한다.

```powershell
Copy-Item .env.example .env
```

`.env`의 비밀번호와 JWT Secret을 확인한 다음 실행한다.

```powershell
docker compose up --build -d
docker compose ps
```

8080 포트를 다른 서버가 사용 중이면 `.env`를 다음과 같이 변경한다.

```dotenv
APP_PORT=8081
```

정상 기준:

```text
codemate-app     healthy
codemate-mysql   healthy
```

### Flyway 이력 확인

MySQL Workbench에서 Docker MySQL에 접속한다.

```text
Host: localhost
Port: 3307
Username: .env의 CODEMATE_DB_USERNAME
Password: .env의 CODEMATE_DB_PASSWORD
Schema: codemate
```

다음 SQL을 실행한다.

```sql
SELECT installed_rank, version, description, type, success, installed_on
FROM flyway_schema_history
ORDER BY installed_rank;
```

기존 DB를 전환한 현재 개발 환경의 정상 예시:

```text
version=1
description=Existing CodeMate schema
type=BASELINE
success=1
```

빈 DB에서 처음 실행한 정상 예시:

```text
version=1
description=create initial schema
type=SQL
success=1
```

`BASELINE`과 `SQL`은 생성 경로가 다르다는 의미이며 둘 다 정상이다.

### Swagger 문서 테스트

접속 주소:

```text
http://localhost:8080/swagger-ui/index.html
```

`APP_PORT=8081`이면:

```text
http://localhost:8081/swagger-ui/index.html
```

확인 항목:

1. `Users > POST /api/users/signup` 펼치기.
2. `Request body > Schema`에서 email, password, nickname 설명 확인.
3. `Example Value`에 문서용 JSON 예시가 표시되는지 확인.
4. `Responses > 400`에서 `ErrorResponse` Schema 확인.
5. `Responses > 500` 공통 서버 오류 응답 확인.
6. Study DTO의 category, meetingType, status에 enum 값과 설명 확인.

### 필드별 검증 오류 테스트

회원가입 요청에서 `Try it out`을 누르고 다음 JSON을 실행한다.

```json
{
  "email": "invalid-email",
  "password": "",
  "nickname": "",
  "mainTechStack": "Spring Boot"
}
```

정상 응답:

```json
{
  "success": false,
  "message": "입력값이 올바르지 않습니다.",
  "errors": {
    "email": "이메일 형식이 올바르지 않습니다.",
    "password": "비밀번호는 필수입니다.",
    "nickname": "닉네임은 필수입니다."
  }
}
```

필드 순서는 달라질 수 있다.

### 잘못된 enum 테스트

`GET /api/studies`의 category에 허용되지 않은 값을 직접 요청한다.

```text
http://localhost:8081/api/studies?category=INVALID
```

정상 응답 상태는 `400`이며 응답 본문은 다음 형식이다.

```json
{
  "success": false,
  "message": "입력값이 올바르지 않습니다."
}
```

### 인증 오류 테스트

1. Swagger의 `Authorize`에서 기존 토큰을 제거한다.
2. `GET /api/users/me` 실행.
3. `401`과 다음 응답 확인.

```json
{
  "success": false,
  "message": "로그인이 필요합니다."
}
```

잘못된 토큰을 입력하면 `유효하지 않은 토큰입니다.` 메시지가 반환되어야 한다.

### 정상 기능 회귀 테스트

오류 응답 확인 후 정상 API 흐름도 다시 확인한다.

1. 회원가입.
2. 로그인 후 access token 발급.
3. Swagger `Authorize`에 토큰 입력.
4. 스터디 생성.
5. 모집 글 목록과 상세 조회.
6. 다른 사용자로 참여 신청.
7. 방장으로 승인 또는 거절.
8. 내 신청 내역 상태 조회.
9. MySQL Workbench에서 데이터 확인.

Flyway는 DB 구조를 관리하고 API 데이터 자체를 초기화하지 않으므로 기존 기능은 같은 방식으로 테스트할 수 있다.

---

## Testcontainers 기반 MySQL 통합 테스트

### 목적

H2 테스트만으로 확인하기 어려운 MySQL 문법, 자료형, 제약조건과 Flyway MySQL 마이그레이션을 실제 MySQL 8.4 환경에서 자동 검증한다.

### 사전 조건

1. Docker Desktop 실행.
2. Docker Engine이 정상 상태인지 확인.

```powershell
docker info
```

별도의 MySQL 계정, 데이터베이스 또는 `.env` 설정은 필요하지 않다. Testcontainers가 임시 MySQL 컨테이너와 임의 포트를 자동으로 준비하고 테스트 종료 후 제거한다.

### MySQL 통합 테스트만 실행

```powershell
.\mvnw.cmd "-Dtest=MySqlTestcontainersIntegrationTest" test
```

검증 항목:

1. MySQL 8.4 컨테이너 실행.
2. Spring Boot `@ServiceConnection`을 통한 DataSource 자동 연결.
3. `db/migration/mysql/V1__create_initial_schema.sql` 적용.
4. Flyway Schema 버전 `1` 확인.
5. 실제 DB 종류가 MySQL인지 확인.
6. `study_members` 테이블 생성 확인.
7. 회원가입 및 로그인.
8. 스터디 생성.
9. 방장의 수동 모집 마감.
10. 마감된 스터디 상세 조회.

### 전체 테스트 실행

```powershell
.\mvnw.cmd verify
```

Docker Desktop이 실행 중이면 H2 기반 테스트와 MySQL Testcontainers 테스트를 모두 실행한다. Docker를 사용할 수 없는 로컬 환경에서는 MySQL 통합 테스트만 건너뛴다.

### GitHub Actions

1. GitHub Hosted Runner의 Docker 상태 확인.
2. Maven 테스트 단계에서 임시 MySQL 8.4 컨테이너 실행.
3. MySQL 통합 테스트를 포함한 전체 테스트와 패키징 수행.
4. 성공 후 Docker 애플리케이션 이미지 빌드 수행.

CI에서는 `docker info` 단계가 실패하면 Maven 테스트를 시작하지 않으므로 MySQL 통합 테스트가 의도치 않게 생략되지 않는다.

회원, 스터디, 참여 신청 데이터가 모두 삭제되므로 초기화가 필요한 경우에만 사용한다.

### 포트 충돌 해결

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

### Docker 검증 순서

1. `docker compose up --build -d`.
2. `docker compose ps`에서 MySQL과 앱의 `healthy` 확인.
3. `/actuator/health`에서 `UP` 확인.
4. Swagger UI 접속.
5. 회원가입, 로그인, 스터디 생성.
6. `docker compose down`.
7. `docker compose up -d`.
8. 기존 계정 로그인 및 스터디 데이터 유지 확인.

---

## 자동화 테스트 실행

### 전체 테스트

프로젝트 루트에서 실행한다.

PowerShell:

```powershell
.\mvnw.cmd test
```

CMD:

```cmd
mvnw.cmd test
```

정상 결과:

```text
Tests run: 42, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

자동화 테스트는 기본 `h2` 프로필과 H2 인메모리 DB를 사용하므로 Docker나 로컬 MySQL을 먼저 실행하지 않아도 된다.

### 인증·인가 테스트

```powershell
.\mvnw.cmd -Dtest=UserAuthenticationIntegrationTest test
```

확인 범위:

1. 회원가입 및 비밀번호 암호화.
2. 중복 이메일·닉네임.
3. 로그인 성공·실패.
4. JWT 인증 성공·실패.

### Study CRUD 권한 테스트

```powershell
.\mvnw.cmd -Dtest=StudyAuthorizationIntegrationTest test
```

확인 범위:

1. 공개 목록·상세 조회.
2. 로그인 사용자 생성.
3. 방장 수정·삭제.
4. 비방장 수정·삭제 차단.

### 참여 신청 상태 전이 테스트

```powershell
.\mvnw.cmd -Dtest=StudyMemberStatusTransitionIntegrationTest test
```

확인 범위:

1. `PENDING → APPROVED`.
2. `PENDING → REJECTED`.
3. `REJECTED → PENDING` 재신청.
4. 중복 신청 차단.
5. 정원 도달 시 모집 마감.
6. 방장 승인·거절 권한.

### 자동화 테스트와 Docker 테스트의 차이

1. Maven 자동화 테스트
   - H2 인메모리 DB 사용.
   - 실행할 때마다 독립적인 테스트 데이터 사용.
   - 코드 변경으로 발생한 회귀 오류를 빠르게 확인.
2. Docker 통합 테스트
   - 실제 MySQL 사용.
   - Flyway와 Docker 볼륨을 포함한 운영 유사 환경 확인.
   - Swagger 또는 Postman으로 수동 시나리오 확인.
3. 배포 전에는 Maven 전체 테스트와 Docker 통합 테스트를 모두 통과시키는 것을 권장.
