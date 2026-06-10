# CodeMate 운영 환경과 CI/CD

운영용 프로필, 보안 환경변수와 GitHub Actions CI/CD 사용 방법을 정리한다.

[실행 가이드 목차](CodeMate_실행_가이드.md)로 돌아가기

## 운영용 prod 프로필

### 개발 프로필과 운영 프로필

1. `h2`
   - 기본 로컬 실행과 자동화 테스트.
   - H2 Console 및 Swagger 사용 가능.
2. `mysql`
   - 로컬 MySQL 및 Docker 개발 환경.
   - Swagger 사용 가능.
   - 기존 개발 DB 전환을 위한 Flyway baseline 허용.
3. `prod`
   - 운영 MySQL 배포 환경.
   - Swagger, OpenAPI JSON, H2 Console 비활성화.
   - SQL 출력과 상세 오류 정보 비활성화.
   - Flyway 자동 baseline 비활성화.

### 필수 환경변수

```text
SPRING_PROFILES_ACTIVE=prod
CODEMATE_DB_HOST=운영 DB 주소
CODEMATE_DB_PORT=3306
CODEMATE_DB_NAME=codemate
CODEMATE_DB_USERNAME=운영 DB 계정
CODEMATE_DB_PASSWORD=운영 DB 비밀번호
CODEMATE_DB_USE_SSL=true
CODEMATE_JWT_SECRET=충분히 긴 Base64 JWT Secret
```

`prod` 프로필에는 DB 주소, DB 이름, 계정, 비밀번호, JWT Secret의 개발용 기본값이 없다. 하나라도 누락하면 서버가 정상 기동하지 않아 잘못된 설정으로 운영되는 것을 방지한다.

### PowerShell 실행

```powershell
$env:CODEMATE_DB_HOST="운영 DB 주소"
$env:CODEMATE_DB_PORT="3306"
$env:CODEMATE_DB_NAME="codemate"
$env:CODEMATE_DB_USERNAME="운영 DB 계정"
$env:CODEMATE_DB_PASSWORD="운영 DB 비밀번호"
$env:CODEMATE_DB_USE_SSL="true"
$env:CODEMATE_JWT_SECRET="Base64 JWT Secret"

.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=prod"
```

### Docker Compose 실행

`.env`에서 다음 값을 변경한다.

```text
SPRING_PROFILES_ACTIVE=prod
CODEMATE_DB_USE_SSL=false
```

현재 Compose 내부 MySQL은 로컬 네트워크에서 실행되므로 SSL 점검용이 아니라면 `false`를 사용한다. RDS 등 TLS를 지원하는 외부 운영 DB는 `true`를 사용한다.

```powershell
docker compose up --build -d
docker compose ps
docker compose logs --tail 100 app
```

### 정상 확인 항목

1. `/actuator/health`는 `{"status":"UP"}` 반환.
2. `/swagger-ui/index.html`은 제공되지 않음.
3. `/v3/api-docs`는 제공되지 않음.
4. 시작 로그에 Hibernate SQL이 출력되지 않음.
5. Flyway가 기존 이력을 검증하고 필요한 신규 migration만 적용.
6. API 오류 응답에 Stack Trace와 내부 예외 내용이 포함되지 않음.

### Flyway 주의사항

1. `prod`는 `baseline-on-migrate=false`이다.
2. 기존 테이블만 있고 `flyway_schema_history`가 없는 DB에는 바로 연결하지 않는다.
3. 운영 DB 배포 전에 백업과 Flyway 이력을 확인한다.
4. 이미 적용된 migration 파일은 수정하지 않는다.
5. 스키마 변경은 다음 버전 SQL 파일로 추가한다.

### 배포 전 보안 점검

1. `.env`를 Git에 포함하지 않는다.
2. `.env.example`의 예시 비밀번호를 운영에서 사용하지 않는다.
3. 개발용 JWT Secret을 운영에서 재사용하지 않는다.
4. DB 포트는 외부 전체 공개 대신 애플리케이션 서버 또는 허용된 IP만 접근하도록 제한한다.
5. 애플리케이션 외부 연결은 HTTPS를 사용한다.
6. 운영 비밀값은 서버 환경변수 또는 AWS Secrets Manager 같은 비밀 저장소에서 관리한다.

---

## GitHub Actions CI

### 실행 조건

1. `main` 브랜치 Push.
2. `main` 브랜치를 대상으로 하는 Pull Request.
3. GitHub Actions 화면의 `Run workflow` 수동 실행.

### Maven Test 작업

1. Ubuntu 최신 GitHub Hosted Runner 사용.
2. Eclipse Temurin Java 17 설정.
3. Maven 의존성 캐시 사용.
4. Maven Wrapper 실행 권한 설정.
5. 다음 명령으로 전체 테스트와 패키징 검증.

```bash
./mvnw --batch-mode --no-transfer-progress verify
```

6. 테스트 실패 시 `target/surefire-reports`를 7일 동안 Artifact로 보관.

### Docker Build 작업

1. Maven Test 성공 후에만 실행.
2. Docker Buildx 사용.
3. 프로젝트 `Dockerfile`로 이미지 빌드.
4. Registry에는 Push하지 않고 빌드 가능 여부만 검증.
5. GitHub Actions Cache로 다음 빌드 시간 단축.

### GitHub에서 결과 확인

1. 원격 저장소의 `Actions` 탭으로 이동.
2. `CI` 워크플로 선택.
3. 실행 내역에서 `Maven Test`와 `Docker Build` 상태 확인.
4. 초록색 체크는 두 작업 모두 성공했다는 의미.
5. 테스트 실패 시 실행 상세 화면의 `Artifacts`에서 Surefire 리포트 다운로드.

### 로컬 사전 검증

```powershell
.\mvnw.cmd verify
docker build -t codemate:local-check .
```

### CI 보안 설정

1. GitHub Token 권한은 Repository Contents 읽기 전용.
2. `.env` 및 운영 Secret을 워크플로에 직접 작성하지 않음.
3. H2 인메모리 DB를 사용하므로 DB 비밀번호 Secret이 필요하지 않음.
4. Docker 이미지는 빌드만 하며 외부 Registry에 Push하지 않음.

---

## GitHub Actions CD

### 실행 조건

1. GitHub Actions의 `Build, Push and Deploy` Workflow를 수동 실행한다.
2. 운영 EC2와 GitHub Secrets가 준비된 상태에서 실행한다.
3. 배포할 Commit을 선택해 Docker 이미지 태그와 실행 코드를 일치시킨다.

### 필요한 Secrets

```text
DOCKERHUB_USERNAME
DOCKERHUB_TOKEN
EC2_HOST
EC2_USERNAME
EC2_SSH_KEY
CODEMATE_DB_PASSWORD
MYSQL_ROOT_PASSWORD
CODEMATE_JWT_SECRET
```

비밀번호, SSH Private Key와 JWT Secret은 문서, Workflow YAML 또는 저장소에 직접 기록하지 않는다.

### CD 작업

1. Docker Buildx로 애플리케이션 이미지를 생성한다.
2. Docker Hub에 Commit SHA와 `latest` 태그를 Push한다.
3. GitHub Secrets로 `.env.prod`를 생성한다.
4. `compose.prod.yaml`과 `.env.prod`를 EC2 `~/codemate`에 전송한다.
5. EC2에서 새 이미지를 Pull한다.
6. MySQL과 named volume은 유지하고 애플리케이션 컨테이너를 교체한다.
7. `/actuator/health`가 `UP`인지 확인한다.

### 배포 완료 확인

1. GitHub Actions의 전체 Job이 성공인지 확인한다.
2. Docker Hub에 SHA와 `latest` 태그가 있는지 확인한다.
3. EC2에서 `docker compose ps`로 컨테이너 상태를 확인한다.
4. `https://polar-bear.o-r.kr/actuator/health`가 `UP`인지 확인한다.
5. Postman에서 운영 API를 실행한다.
6. 애플리케이션 재시작 후 기존 데이터가 유지되는지 확인한다.

AWS 리소스, 보안 그룹, ALB, ACM과 도메인 설정은 [AWS 배포 문서](../info/AWS_DEPLOYMENT.md)를 참고한다.
