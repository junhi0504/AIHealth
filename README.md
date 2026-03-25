# AI Health

AI Health는 사용자의 체성분 데이터를 기반으로 맞춤 운동을 추천하고  
운동 기록을 관리할 수 있는 헬스 관리 웹 서비스입니다.

사용자는 인바디 정보를 입력하여 개인에게 맞는 운동을 추천받을 수 있으며  
운동 일지를 작성해 자신의 운동 기록을 체계적으로 관리할 수 있습니다.

---

## 프로젝트 소개

헬스를 처음 시작하는 사용자들은 자신의 체성분에 맞는 운동을 찾기 어렵습니다.  
이 문제를 해결하기 위해 체성분 데이터를 기반으로 운동을 추천하고,  
운동 기록을 관리할 수 있는 웹 서비스를 개발했습니다.

주요 기능

- 체성분 데이터 입력
- AI 기반 운동 추천
- 운동 기록 관리
- 커뮤니티 게시판

---

## 프로젝트 정보

- **프로젝트 유형** : 졸업 프로젝트  
- **개발 기간** : 2025.09 ~ 2025.11  
- **개발 인원** : 2명  

---

## 담당 기능

본 프로젝트에서 **메인 페이지와 소셜 로그인 기능을 제외한 대부분의 기능을 구현**했습니다.

### 운동 일지 기능
- 운동 종목 추가
- 세트 / 횟수 기록
- 운동 완료 체크
- 운동 기록 삭제

### 운동 기록 관리
- 날짜별 운동 기록 조회
- 회원별 운동 데이터 관리

### 커뮤니티 게시판
- 게시글 작성
- 댓글 작성
- 게시글 추천 기능

### AI 운동 추천
- OpenAI API 연동
- 체성분 데이터를 기반으로 맞춤 운동 추천 기능 구현

---

## 기술 스택

### Backend
- Java
- Spring Boot
- Spring Security
- Spring Data JPA

### Frontend
- HTML
- CSS
- JavaScript
- Thymeleaf

### Database
- MySQL

### AI
- OpenAI API

### Authentication
- OAuth2
  - Kakao Login
  - Naver Login

---

## 주요 기능

### AI 운동 추천

사용자의 체성분 데이터를 기반으로 AI가 맞춤 운동을 추천합니다.

- 체지방 감소 운동
- 근력 증가 운동
- 주간 운동 루틴 추천

---

### 운동 일지 기능

사용자가 자신의 운동 기록을 관리할 수 있습니다.

- 운동 종목 기록
- 세트 / 횟수 기록
- 운동 완료 체크
- 운동 기록 삭제

---

### 커뮤니티 게시판

사용자들이 운동 정보를 공유할 수 있는 게시판 기능입니다.

- 게시글 작성
- 댓글 작성
- 게시글 추천

---

## 프로젝트 화면

### 메인 화면

<img width="1978" height="1057" alt="image" src="https://github.com/user-attachments/assets/370ca4f6-6ae9-4808-b815-dcfbf74c812e" />

---

### AI 운동 추천

<img width="1019" height="1189" alt="image" src="https://github.com/user-attachments/assets/9ce69282-e1f7-4285-9ad2-e7365de3e1a2" />

<img width="964" height="1129" alt="image" src="https://github.com/user-attachments/assets/4f8cfda8-38fd-4c34-9cc5-a92b6e32eb36" />

<img width="1455" height="1344" alt="image" src="https://github.com/user-attachments/assets/62594025-8f41-4e60-9e07-1500b1d78860" />

---

### 운동 일지

<img width="770" height="1166" alt="image" src="https://github.com/user-attachments/assets/4dd70e64-bcfd-4f6e-9b78-3bf468903431" />

---

### 커뮤니티 게시판

<img width="980" height="1125" alt="image" src="https://github.com/user-attachments/assets/034e156f-1fc5-41c1-badb-499eb0ba3c10" />

<img width="1000" height="1125" alt="image" src="https://github.com/user-attachments/assets/20240adf-6125-41da-b9eb-d04475d246bd" />

---

## 프로젝트 구조

```
AIHealth
 ├─ controller
 ├─ service
 ├─ repository
 ├─ entity
 ├─ dto
 ├─ config
 ├─ templates
 └─ static
```

---

## 실행 방법

### 1. 프로젝트 다운로드

```
git clone https://github.com/junhi0504/AIHealth
cd AIHealth
```

### 2. 서버 실행

Windows

```
gradlew.bat bootRun
```

Mac / Linux

```
./gradlew bootRun
```

---

## 개발자

김준희  
강남대학교 컴퓨터공학과
