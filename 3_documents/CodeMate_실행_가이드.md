# CodeMate 실행 가이드

CodeMate 서버 실행, API 테스트, 데이터베이스 및 운영 환경 문서의 통합 목차이다.

## 빠른 시작

### H2 로컬 실행

```powershell
cd C:\KOSTA_Projects\3_CodeMate
.\mvnw.cmd spring-boot:run
```

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- H2 Console: http://localhost:8080/h2-console

### Docker 통합 실행

```powershell
docker compose up --build -d
docker compose ps
```

- Swagger UI: http://localhost:8081/swagger-ui/index.html
- MySQL: `localhost:3307`

### AWS 운영 확인

- 운영 Base URL: `https://polar-bear.o-r.kr`
- Health Check: `https://polar-bear.o-r.kr/actuator/health`
- 회원가입 예시: `POST https://polar-bear.o-r.kr/api/users/signup`

운영 `prod` 프로필은 Swagger UI를 비활성화하므로 Postman으로 API를 테스트한다.

## 상세 가이드

- [로컬 서버 실행](guides/LOCAL_SERVER.md): H2, 서버, Swagger, H2 Console
- [API 테스트](guides/API_TEST.md): Swagger/Postman, JWT, Study CRUD, 참여 상태
- [데이터베이스 테스트](guides/DATABASE.md): MySQL, Flyway, Testcontainers, 자동화 테스트
- [Docker 실행](guides/DOCKER.md): Docker Compose 실행과 데이터 영속성
- [운영 환경과 CI](guides/PROD_AND_CI.md): prod 프로필, 환경변수, GitHub Actions
- [AWS 배포](AWS_DEPLOYMENT.md): EC2, Docker Hub, CD, ALB, ACM, 도메인과 HTTPS

## 권장 확인 순서

1. 로컬 H2 환경에서 서버와 Swagger가 열리는지 확인한다.
2. 회원가입·로그인 후 JWT를 등록하고 핵심 API를 테스트한다.
3. 자동화 테스트와 Flyway 마이그레이션을 검증한다.
4. Docker Compose 환경에서 MySQL 저장과 재시작 후 영속성을 확인한다.
5. 운영 환경변수와 CI 결과를 확인한다.
6. GitHub Actions CD와 EC2 컨테이너 상태를 확인한다.
7. HTTPS Health Check와 Postman 운영 API를 확인한다.
