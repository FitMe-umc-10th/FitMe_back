# 🎓 FitMe Backend

> **대학생 맞춤형 장학금 · 공모전 추천 및 지원 이력 관리 서비스 - 백엔드**

## 📌 프로젝트 소개

**FitMe**는 대학생들이 자신에게 맞는 장학금과 공모전 정보를 효율적으로 탐색하고,
지원 이력과 진행 상태를 한 곳에서 관리할 수 있도록 돕는 서비스입니다.

백엔드에서는 사용자, 공고, 지원 이력, 찜, 알림 등의 핵심 도메인을 관리하며,
REST API를 통해 서비스의 주요 기능을 제공합니다.

## ✨ 주요 기능

- 관심사 기반 장학금 · 공모전 조회 및 추천
- 지원 이력 생성, 조회, 상태 관리
- 지원 이력 메모 관리
- 찜(북마크) 기능
- 마이페이지 및 활동 정보 관리
- 로그인 및 사용자 인증
- 마감일 알림 기능

## 🗂️ ERD

<p align="center">
  <img width="1008" src="https://github.com/user-attachments/assets/1d89dc22-8caa-4c96-8ea8-72d9e513863b">
</p>

## 🛠️ 기술 스택

| 구분                | 기술          |
| ----------------- | ----------- |
| Language          | Java        |
| Framework         | Spring Boot |
| Database          | MySQL       |
| API Documentation | Swagger     |
| Collaboration     | Notion      |
| CI/CD             | 추가 예정    |
| Infra & DevOps    | EC2, S3     |
| External API      | 한국장학재단      |


## 📂 프로젝트 구조

도메인 중심(DDD) 구조를 기반으로 각 도메인을 독립적으로 관리합니다.

```text
src/main/java/umc/fitme/
    ├── domain
    │   ├── interest                # 관심사 도메인
    │   │   ├── controller          # 컨트롤러
    │   │   ├── converter           # 컨버터
    │   │   ├── dto                 # DTO
    │   │   ├── entity              # 엔티티
    │   │   ├── enums               # Enum들
    │   │   ├── exception           # 예외
    │   │   ├── repository          # 레포지토리
    │   │   └── service             # 서비스
    │   ├── notify                  # 알림 도메인
    │   ├── post                    # 공고 도메인
    │   │   ├── scholarship         # 장학금 도메인
    │   │   └── contest             # 공모전 도메인
    │   └── user                    # 사용자 도메인
    ├── global                      # 전역 공통 설정 및 예외 처리
    └── FitmeApplication.java
```

## 📏 Convention
프로젝트 개발 컨벤션은 아래 문서에서 확인할 수 있습니다.

👉 [프로젝트 컨벤션 (Notion)](https://difficult-grass-c0f.notion.site/e2751793c00a82a993b801e2c81ba654?source=copy_link)

## 👥 Team

| 이름 | 담당 | GitHub |
|------|------|--------|
| 장문경 | 찜 · 온보딩 API | [@jangmk05](https://github.com/jangmk05) |
| 김태리 | 마이페이지 API | [@lucky7terry](https://github.com/lucky7terry) |
| 육도연 | 홈 API | [@yookdy](https://github.com/yookdy) |
| 홍진우 | 지원 이력 API | [@j2nooh](https://github.com/j2nooh) |
| 김강민 | 탐색 API · 로그인 · 프로젝트 세팅 | [@kkangmen](https://github.com/kkangmen) |
