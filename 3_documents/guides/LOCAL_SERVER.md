# CodeMate 로컬 서버 실행

H2 기본 환경과 로컬 서버, Swagger, H2 Console 확인 방법을 정리한다.

[실행 가이드 목차](../CodeMate_실행_가이드.md)로 돌아가기

## 서버 실행

### 프로젝트 경로 이동

```powershell
cd C:\KOSTA_Projects\3_CodeMate
```

### 서버 실행

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
set CODEMATE_DB_PASSWORD=your-local-password

mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

MySQL 프로필을 실행하기 전에 `codemate` 데이터베이스와 접속 계정을 생성해야 한다.
`CODEMATE_DB_PASSWORD`는 설정 파일에 저장하지 않고 반드시 환경변수로 전달한다.

PowerShell의 `$env:변수명="값"` 문법을 cmd에서 실행하면 파일 이름 또는 디렉터리 이름 구문 오류가 발생한다.
cmd에서는 `set 변수명=값` 문법을 사용한다.

### 서버 주소

```text
http://localhost:8080
```

### Swagger UI

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

## H2 Console 확인

H2 Console은 기본 `h2` 프로필에서만 사용할 수 있다. `mysql` 프로필에서는 비활성화된다.

### 접속 URL

```text
{{baseUrl}}/h2-console
```

### 접속 정보

| 항목 | 값 |
|---|---|
| JDBC URL | `jdbc:h2:mem:codemate` |
| User Name | `sa` |
| Password | 비워두기 |

### 주요 테이블

- `users`
- `study`
- `study_members`
- `tech_stacks`
- `study_tech_stacks`

---
