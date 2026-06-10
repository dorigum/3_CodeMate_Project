# CodeMate Docker 실행

Docker Compose 기반 애플리케이션과 MySQL 실행 및 점검 방법을 정리한다.

[실행 가이드 목차](CodeMate_실행_가이드.md)로 돌아가기

## Docker Compose 서버 테스트

### 사전 준비

1. Docker Desktop을 실행한다.
2. 프로젝트 루트에서 Docker 명령이 동작하는지 확인한다.

```powershell
docker --version
docker compose version
```

### 환경변수 파일 생성

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

### 컨테이너 실행

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

### 실행 상태 확인

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

### 로그 확인

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

### 컨테이너 종료와 재실행

컨테이너 종료:

```powershell
docker compose down
```

MySQL 볼륨은 유지되므로 다시 실행해도 데이터가 남는다.

```powershell
docker compose up -d
```

### MySQL 데이터 초기화

아래 명령은 컨테이너와 MySQL 데이터 볼륨을 함께 삭제한다.

```powershell
docker compose down -v
```

---
