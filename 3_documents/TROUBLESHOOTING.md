# CodeMate 트러블슈팅

개발 과정에서 발생한 주요 문제와 해결 기록을 날짜별 개발 로그에서 빠르게 찾기 위한 색인이다.

## Postman 참여 승인 테스트 트러블슈팅

- 발생일: 2026-06-07
- 상세 기록: [2026-06-07 개발 로그](project-log/2026-06-07.md)

## Swagger 스터디 목록 조회 401 오류 트러블슈팅

- 발생일: 2026-06-08
- 상세 기록: [2026-06-08 개발 로그](project-log/2026-06-08.md)

## GitHub Actions CI 테스트 실패 트러블슈팅

- 발생일: 2026-06-09
- 상세 기록: [2026-06-09 개발 로그](project-log/2026-06-09.md)

## AWS Docker 및 CD 배포 트러블슈팅

- 발생일: 2026-06-10
- 주요 문제: Docker Socket 권한, Docker Hub 이미지 경로, 컨테이너 이름 충돌, GitHub Hosted Runner SSH 접근
- 상세 기록: [2026-06-10 개발 로그](project-log/2026-06-10.md)
- 재현 가능한 배포 절차: [AWS 배포 문서](AWS_DEPLOYMENT.md)

## ALB, Target Group 및 HTTPS 트러블슈팅

- 발생일: 2026-06-10
- 주요 문제: 잘못된 Target 포트로 인한 502, Listener 미연결로 인한 `Unused`, Health Check 경로 오류로 인한 401
- 해결 결과: Target Group `8080`, `/actuator/health`, ALB 보안 그룹 연결 후 `Healthy`
- 상세 기록: [2026-06-10 개발 로그](project-log/2026-06-10.md)

## 테스트 및 운영 점검

- 로컬·Docker·MySQL 실행 문제는 [CodeMate 실행 가이드](CodeMate_실행_가이드.md)에서 확인한다.
- AWS, 도메인, HTTPS와 CD 문제는 [AWS 배포 문서](AWS_DEPLOYMENT.md)에서 확인한다.
- 새로운 장애 기록은 해당 날짜의 개발 로그에 먼저 작성하고 이 문서에 링크를 추가한다.
