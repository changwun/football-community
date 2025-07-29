# 축구 팬을 위한 커뮤니티 공간: KickBoard
⚽football-community-project

## 📝 소개
축구 팬들이 자유롭게 소통하고 정보를 공유하며, 용병 경기를 모집할 수 있는 커뮤니티 플랫폼입니다. 회원 가입 및 로그인, 게시판 기능, 관리자 페이지, 그리고 축구 경기 일정 등을 제공합니다.

## 📦 주요 기능
회원 관리 : 사용자 및 관리자 회원가입, 로그인, 권한별 접근 제어(Spring Security 기반)
게시판 시스템 : 일반 게시글 및 댓글/대 댓글 기능, 파일(이미지) 첨부, 게시글 목록/상세 조회
용병 모집 게시판 : 지역별 용병 모집 및 댓글 기능
관리자 페이지 : 회원 관리(가입 승인, 블랙리스트,회원 목록 등)
축구 일정 연동 : 외부 API를 통해 최신 축구 일정을 자동으로 받아와 공지(비동기 스케줄링)
콘텐츠 조회 : 로그인 여부와 관계없이 게시판 목록 조회 가능
스포츠 이슈 : 외부 API를 통해 스포츠 뉴스 기사 확인 가능.


## 🔧 기술 스택
Backend
- **Language**: Java 17
- **Framework**: Spring Boot 3.5.4
- **Security**: Spring Security + JWT 
- **JPA**: ORM(Object-Relational Mapping)
데이터베이스
- **DB**: MySQL
- **Cache**: Redis
API연동
- **API Client**: RestTemplate / WebClient
- **Scheduler**: @Scheduled
(비동기 스케줄링)
- **크롤링**: Jsoup (스포츠 뉴스 기사)
- **문서화**: Swagger 3.0
- **빌드 툴**: Gradle

## ⚙️ 개발 환경 (Development Environment)
- **IDE**: IntelliJ IDEA
- **JDK**: OpenJDK 17
- **Database Tool**: MySQL Workbench
- **API Test Tool**: Postman

## ERD



## Trouble Shooting
???
