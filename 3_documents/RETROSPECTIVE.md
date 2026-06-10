# CodeMate 프로젝트 회고

이 문서는 핵심 기능 구현부터 Docker, CI/CD, AWS HTTPS 배포와 운영 점검까지 진행하며 확인한 내용을 정리한다.

## 프로젝트 요약

- 개발 기간: 2026-06-05~2026-06.10
- 목표: 스터디 모집과 참여 관리 핵심 흐름을 갖춘 Spring Boot 백엔드 API 구현
- 현재 단계: 핵심 기능·테스트·Docker·CI/CD·AWS HTTPS 배포 완료

## 배운 점

1. JWT를 단순 발급하는 것과 로그아웃·재발급·기존 토큰 무효화까지 운영하는 것은 별개의 문제였다.
2. 참여 승인처럼 정원이 걸린 기능은 일반적인 CRUD를 넘어 동시성 제어가 필요했다.
3. JPA `ddl-auto`에 의존하지 않고 Flyway로 스키마 변경 이력을 관리하면 환경 간 차이를 줄일 수 있었다.
4. Swagger 예시도 실제 서버 요청 형식과 함께 자동화 테스트해야 문서와 구현의 불일치를 줄일 수 있었다.
5. H2 테스트만으로는 MySQL 전용 SQL과 제약 차이를 확인하기 어려워 Testcontainers가 유용했다.
6. ALB는 단순히 EC2 앞에 배치하는 것만으로 끝나지 않고 Listener, Target Group 포트, Health Check 경로와 보안 그룹이 모두 일치해야 했다.
7. GitHub Hosted Runner를 사용한 SSH 배포는 Runner IP가 고정되지 않아 네트워크 접근 방식을 별도로 설계해야 했다.
8. Docker named volume을 분리하면 애플리케이션 컨테이너 교체와 데이터 생명주기를 분리할 수 있었다.

## 잘된 점

1. 인증·인가 실패를 포함한 공통 오류 응답 형식을 일관되게 구성했다.
2. 권한, 동시 승인, 참여 상태 전이와 JWT 수명주기를 자동화 테스트로 검증했다.
3. 로컬 H2, MySQL, Docker, prod 프로필을 분리해 실행 목적을 명확히 했다.
4. CI에서 Maven 검증과 Docker 이미지 빌드를 연속으로 확인하도록 구성했다.
5. 개발 로그와 실행 가이드를 날짜별·주제별 문서로 분리했다.
6. GitHub Actions에서 Docker 이미지를 Build·Push하고 EC2에서 교체하는 CD 흐름을 완성했다.
7. ALB와 ACM을 이용해 운영 API를 도메인 기반 HTTPS로 제공했다.
8. Postman 요청과 컨테이너 재시작으로 실제 API와 데이터 영속성을 검증했다.

## 개선할 점

1. 초기에는 계획과 완료 내역이 하나의 문서에 섞여 현재 상태를 파악하기 어려웠다.
2. 기능 구현을 먼저 진행해 ERD, 요구사항, 아키텍처 문서가 뒤늦게 작성됐다.
3. 통합 테스트에 비해 독립적인 도메인 단위 테스트를 늦게 추가했다.
4. 목록 응답에서 기술 스택을 조회하는 쿼리 수를 측정하고 N+1 가능성을 개선할 필요가 있다.
5. 모집 글의 진행·종료 상태 전환과 모집 마감 시간은 아직 구현되지 않았다.
6. GitHub Hosted Runner 배포를 위해 SSH 포트를 넓게 허용한 실습 구성을 운영 수준으로 개선해야 한다.
7. 애플리케이션과 MySQL을 단일 EC2에서 실행하므로 장애 격리와 백업 전략이 부족하다.
8. 배포 성공 여부는 Health Check로 확인하지만 실패 시 자동 Rollback은 아직 없다.

## 배포 결과

1. AWS EC2에서 Docker Compose로 Spring Boot와 MySQL 8.4를 실행했다.
2. Docker Hub의 `dorigum/codemate` 이미지를 GitHub Actions CD에서 Build·Push했다.
3. GitHub Actions가 운영 Compose와 환경변수를 EC2로 전송하고 애플리케이션 컨테이너를 교체하도록 구성했다.
4. MySQL은 외부 포트를 열지 않고 Docker 내부 네트워크와 named volume을 사용했다.
5. Flyway V1부터 V3까지 운영 MySQL에 적용하고 Hibernate Schema Validation 성공을 확인했다.
6. ALB Target Group을 EC2 `8080`에 연결하고 `/actuator/health`로 상태를 확인했다.
7. ACM 인증서와 `polar-bear.o-r.kr` 도메인을 연결해 HTTPS를 적용했다.
8. `https://polar-bear.o-r.kr/actuator/health`에서 `UP`을 확인했다.
9. Postman에서 운영 회원가입 API가 정상 동작하는 것을 확인했다.
10. 컨테이너 재시작 후 기존 API 데이터가 유지되는 것을 확인했다.

상세 구성과 트러블슈팅은 [AWS 배포 문서](AWS_DEPLOYMENT.md)에 정리했다.

## 향후 확장 계획

1. 모집 글 진행·종료 상태 전환
2. 모집 마감 시간과 스케줄러
3. 알림과 댓글 기능
4. 관리자 운영 기능
5. 쿼리 최적화와 부하 테스트
6. CloudWatch 로그·지표와 운영 알림
7. CD 실패 자동 Rollback과 Smoke Test
8. Terraform 기반 인프라 코드화
