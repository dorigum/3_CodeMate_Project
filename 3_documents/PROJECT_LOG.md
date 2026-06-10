# CodeMate Project Log

CodeMate의 개발 과정과 주요 의사결정을 날짜별로 정리한 문서 목차이다.

## 핵심 문서

- [프로젝트 개요](info/PROJECT_OVERVIEW.md)
- [프로젝트 명세](info/PROJECT_SPECIFICATION.md)
- [시스템 아키텍처](info/ARCHITECTURE.md)
- [데이터베이스 설계](info/DATABASE_DESIGN.md)
- [트러블슈팅](TROUBLESHOOTING.md)
- [CodeMate 실행 가이드](guides/CodeMate_실행_가이드.md)
- [프로젝트 회고](info/RETROSPECTIVE.md)
- [AWS 배포](info/AWS_DEPLOYMENT.md)

## 날짜별 개발 기록

- [2026-06-10](project-log/2026-06-10.md): 참여 신청 취소 및 스터디 탈퇴, 프로젝트 설계 문서 및 도메인 단위 테스트, 운영 **Docker·GitHub Actions CD·AWS EC2·ALB·ACM·도메인 HTTPS 배포와 운영 API 검증**
- [2026-06-09](project-log/2026-06-09.md): **Swagger/OpenAPI** 및 공통 예외 처리 보강, 핵심 기능 자동화 테스트 및 테스트 구조 정리, 운영용 prod 프로필 및 보안 환경 구성, GitHub Actions CI 구성, GitHub Actions CI 테스트 실패 트러블슈팅, 스터디 모집 수동 마감 기능, Testcontainers 기반 MySQL 통합 테스트, 회원 기능 보강, JWT 인증 개선
- [2026-06-08](project-log/2026-06-08.md): Swagger 스터디 목록 조회 401 오류 트러블슈팅, 참여 신청 조회 및 재신청 기능 보강, H2/MySQL 프로필 분리, Docker 실행 환경 구성, **Flyway 기반 DB 마이그레이션** 도입
- [2026-06-07](project-log/2026-06-07.md): Study 복합 검색 필터 구현, 동시 승인 모집 정원 초과 방지, Swagger/OpenAPI 문서화, Postman 참여 승인 테스트 트러블슈팅
- [2026-06-05](project-log/2026-06-05.md): Study CRUD API 1차 구현, 로그인 및 JWT 인증 1차 구현, **Study API JWT** 권한 구조 적용, StudyMember 참여 신청 API 구현, StudyMember 승인/거절 API 구현, StudyMember 신청 목록 조회 API 구현, 인증/인가 실패 응답 **ApiResponse** 형식 통일, Study와 TechStack 연결 로직 구현

## 작성 기준

1. 구현·수정 내역은 작업 날짜의 문서에 번호형 목록으로 기록한다.
2. 오류 원인과 해결 과정은 날짜별 문서에 기록하고 트러블슈팅 색인에도 연결한다.
3. 실행 방법이 달라지면 기능 설명보다 실행 가이드를 먼저 갱신한다.
