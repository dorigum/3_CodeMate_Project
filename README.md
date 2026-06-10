# 🦄 CodeMate

[![CI](https://github.com/dorigum/3_CodeMate_Project/actions/workflows/ci.yml/badge.svg)](https://github.com/dorigum/3_CodeMate_Project/actions/workflows/ci.yml)

**개발자를 위한 스터디(장기) 및 모각코(단기 번개) 모집·참여 관리 백엔드 API 서버입니다.** 단순한 게시판 CRUD를 넘어 **"상태 중심의 비즈니스 로직(대기/승인/거절)"** 과 **"동시성 제어를 통한 안정적인 정원 관리"** 를 핵심 목표로 두고 설계 및 개발되었습니다.

---

## 🚀 1. 운영 및 배포 정보
- **운영 도메인:** `https://polar-bear.o-r.kr`
- **Health Check:** `https://polar-bear.o-r.kr/actuator/health` (ALB Target Group: `Healthy`)
- **CI/CD 인프라:** GitHub Actions ➡️ Docker Hub ➡️ AWS EC2 (Docker Compose) ➡️ ALB / ACM (HTTPS)
- **상세 배포 문서:** [AWS 배포 가이드](3_documents/info/AWS_DEPLOYMENT.md) | [시스템 아키텍처](3_documents/info/ARCHITECTURE.md)

---

## 🛠 2. 핵심 기술 스택 및 구조
- **Backend:** Java 17, Spring Boot 4.0.6, Spring Security, Spring Data JPA, Flyway
- **Database:** MySQL 8.4 (운영/통합 테스트), H2 (로컬 개발)
- **DevOps & Test:** Docker, Docker Compose, GitHub Actions, Testcontainers

---

## ✨ 3. 핵심 비즈니스 포인트

### ① 정원 관리 및 동시성 제어 (Concurrency Control)
- 사용자의 스터디 참여 신청 및 방장의 승인/거절 시스템 구현.
- **비관적 락(`SELECT ... FOR UPDATE`)** 을 적용하여 정원 마감 직전 동시 요청이 발생하더라도 데이터 정합성을 보장하며 정원을 정확히 카운트함.
- 정원 도달 시 `CLOSED` 상태 자동 전환 및 참여자 탈퇴 시 `RECRUITING` 상태 자동 복원 로직 자가 치유(Self-healing) 구조 설계.

### ② 보안 및 JWT 운영 고도화
- **Refresh Token 회전(RTR, Refresh Token Rotation)** 방식을 도입하고 SHA-256 해시 형태로 DB 동기화하여 토큰 탈취 위험 최소화.
- 로그아웃 및 비밀번호 변경 시 기존 발급된 JWT를 즉시 무효화하는 Blacklist 메커니즘 구현.

### ③ 다조건 동적 검색 및 페이징
- 키워드, 카테고리(STUDY/MOGAKCO), 모집 상태, 진행 방식, 지역, 기술 스택 등 총 6가지 조건을 자유롭게 조합하여 조회할 수 있는 최적화된 동적 쿼리 및 페이징 구현.
- 무분별한 조회를 방지하기 위해 JPA 지연 로딩(Lazy Loading) 및 성능 최적화 적용.

---

## 🧪 4. 테스트 및 품질 관리
- **도메인 단위 테스트:** 핵심 도메인 비즈니스 로직에 대한 철저한 단위 테스트 격리 수행.
- **MySQL 통합 테스트:** H2 데이터베이스와의 환경 격차를 줄이기 위해 **Testcontainers**를 도입, 실제 운영 환경과 동일한 MySQL 8.4 환경에서 컨테이너 기반 API 통합 테스트 자동화 수행.

---

## 📂 5. 프로젝트 상세 문서 (상세 내용 확인)

CodeMate 프로젝트는 모든 설계 및 트러블슈팅 과정을 도큐멘테이션하여 관리하고 있습니다. 아래 링크에서 상세 내용을 확인하실 수 있습니다.

- 📝 [PROJECT OVERVIEW (초기 목표 및 MVP)](3_documents/info/PROJECT_OVERVIEW.md)
- 📋 [PROJECT SPECIFICATION (요구사항 및 API 규칙)](3_documents/info/PROJECT_SPECIFICATION.md)
- 📐 [DATABASE DESIGN (ERD 및 테이블 정의)](3_documents/info/DATABASE_DESIGN.md)
- 🏗 [SYSTEM ARCHITECTURE (시스템 흐름도 및 시퀀스 다이어그램)](3_documents/info/ARCHITECTURE.md)
- 🚨 [TROUBLESHOOTING (인프라 및 비즈니스 에러 해결 색인)](3_documents/TROUBLESHOOTING.md)
- 🎯 [RETROSPECTIVE (6일간의 개발 회고 및 개선 방향)](3_documents/info/RETROSPECTIVE.md)

---
*Updated at_2026.06.10*
