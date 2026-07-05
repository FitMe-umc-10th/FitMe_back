# FitMe Backend ⚙️

> 대학생 맞춤형 장학금 · 공모전 매칭 서비스 — 백엔드

## 📌 프로젝트 개요

- 관심사·직무 기반 개인화 공고 추천 (장학금, 공모전, 대외활동)

- 지원 공고 진행 상태 및 이력 관리

- 공고별 메모 및 회고 기록

- 마감일 중심 알림 기능 제공

- 스펙/활동 이력 대시보드 시각화

- 탐색 비용 최소화를 통한 핵심 기회 제공

## ERD
<img width="1008" height="614" alt="image" src="https://github.com/user-attachments/assets/1d89dc22-8caa-4c96-8ea8-72d9e513863b" />

## 🛠️ 기술 스택

| 구분             | 기술 |
|----------------|------|
| Framework      | Spring Boot |
| Language       | Java |
| DB             | MySQL |
| CI/CD          | 추가 예정 |
| Infra & DevOps | EC2, S3 |
| External API | 한국장학재단 |
| Collaboration/Tools | Swagger, Notion |

## 프로젝트 구조 (DDD)
```
└── java/com/umc/fitme/
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
    │   └── user                    # 사용자 도메인
    ├── global                      # 전역 공통 설정 및 예외 처리
    └── FitmeApplication.java
```

## 🍆 컨벤션 
https://difficult-grass-c0f.notion.site/e2751793c00a82a993b801e2c81ba654?source=copy_link

## 팀원 정보
| 이름  | 역할      | 깃헙주소 |
|-----|---------| ---| 
| 장문경 | Backend : 찜 & 온보딩 api 구현 | 깃헙주소 |
| 김태리 | Backend : 마이페이지 api 구현      | 깃헙주소 |
| 육도연 | Backend : 탐색 api 구현      | 깃헙주소 |
| 홍진우 | Backend : 이력 api 구현     | 깃헙주소 |
| 김강민 | Backend : 탐색 & 로그인 api 구현, 개발 환경 세팅     | 깃헙주소 |
