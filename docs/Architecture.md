Auth Service Architecture
Overview

이 문서는 MSA 기반 에디터 서비스에서 Auth Service의 역할과 인증 구조를 정의한다.

Auth Service는 시스템 전체에서 중앙 인증 서버(SSO Provider) 역할을 하며 다음 세 가지 영역의 인증을 담당한다.

1. 소개 페이지 (Landing / Intro)
2. 서비스 페이지 (Editor Service)
3. 관리자 페이지 (Admin Console)

현재 인증 방식은 GitHub OAuth2 기반 소셜 로그인이며, 로그인 성공 후 SSO Ticket + Session 기반 인증 구조를 사용한다.

System Architecture

전체 MSA 서비스 구성은 다음과 같다.

Auth Service
User Service
Document Service
Block Service
Permission Service

각 서비스의 역할은 다음과 같다.

Service	Responsibility
Auth Service	인증 처리, SSO 세션 관리
User Service	사용자 정보 및 역할 관리
Document Service	문서 메타데이터 관리
Block Service	문서 내부 블록 콘텐츠 관리
Permission Service	문서 및 서비스 접근 권한 관리

Auth Service는 Authentication만 담당하며 Authorization은 Permission Service가 담당한다.

Authentication Model

현재 인증 모델은 다음 구조로 동작한다.

GitHub OAuth2
│
▼
Auth Service
│
▼
SSO Ticket + Session
│
▼
Client Applications

즉,

External Identity Provider
→ Internal Authentication
→ SSO Session

흐름으로 작동한다.

Authentication Targets

Auth Service는 다음 세 가지 영역의 인증을 담당한다.

1 Intro Page

서비스 소개 및 로그인 시작 페이지

역할

서비스 소개

로그인 시작

OAuth2 인증 리디렉션

User
│
▼
Intro Page
│
▼
Auth Service
2 Service Page

실제 에디터 서비스가 제공되는 영역

기능

문서 생성

문서 편집

블록 관리

접근 시 Auth Service의 인증 상태를 검증한다.

Client
│
▼
Editor Service
│
▼
Auth Service (Session Validation)
3 Admin Page

서비스 관리용 관리자 콘솔

관리 기능

사용자 관리

서비스 관리

시스템 설정

로그 조회

관리자 접근 시 추가적인 권한 검증이 필요하다.

Admin Client
│
▼
Admin Console
│
▼
Auth Service
│
▼
Permission Service
Authentication Flow
Login Flow

GitHub OAuth2 기반 로그인 과정

User
│
▼
Intro Page
│
▼
Auth Service
│
▼
GitHub OAuth2 Authorization
│
▼
GitHub Callback
│
▼
Auth Service
│
├ verify OAuth identity
├ map internal user
├ create session
└ issue SSO ticket
│
▼
Client
Session Structure

로그인 성공 시 다음 구조가 생성된다.

Browser
└ Session Cookie

Auth Service
├ Session Store
└ Ticket Store

세션은 중앙 저장소(Redis 등)에 저장될 수 있다.

SSO Mechanism

SSO 구조는 다음과 같이 작동한다.

User Login
│
▼
Auth Service
│
▼
SSO Session Created
│
├ Intro Page
├ Service Page
└ Admin Page

사용자는 한 번 로그인하면 세 서비스에 대해 재로그인 없이 접근 가능하다.

Authentication vs Authorization

Auth Service는 Authentication만 수행한다.

Authentication = Who are you
Authorization  = What can you do

역할 분리는 다음과 같다.

Responsibility	Service
Authentication	Auth Service
User Profile	User Service
Permission	Permission Service
Internal User Mapping

GitHub OAuth2 인증 후 내부 사용자로 매핑이 필요하다.

GitHub Identity
│
▼
Auth Service
│
▼
User Service
│
▼
Internal User

즉,

GitHub Account ≠ Internal User
Admin Access Control

관리자 페이지 접근은 다음 절차를 거친다.

Admin Access
│
▼
Auth Service (Authentication)
│
▼
User Service (Role 확인)
│
▼
Permission Service (Admin 권한 확인)
Security Considerations
Role Separation

다음 역할은 반드시 분리해야 한다.

Authentication
Authorization
User Profile
Admin Access Protection

관리자 페이지는 일반 서비스보다 높은 보안 정책을 적용해야 한다.

예시

짧은 세션 TTL

재인증 요구

접근 로그 기록

IP 제한

MFA 확장 가능

External Identity Isolation

GitHub OAuth2 계정이 내부 관리자 권한을 자동으로 가지면 안 된다.

반드시 다음 단계를 거쳐야 한다.

GitHub OAuth
│
▼
Internal User Mapping
│
▼
Role Verification
│
▼
Permission Validation
Responsibilities of Auth Service

Auth Service의 책임 범위

Responsibilities

OAuth2 로그인 처리

SSO 세션 생성

티켓 발급

세션 검증

로그인 상태 확인 API 제공

Non Responsibilities

Auth Service가 담당하지 않는 영역

사용자 프로필 관리

문서 데이터 관리

블록 데이터 관리

서비스 권한 정책

Future Extensions

향후 다음 확장이 가능하다.

Additional Identity Providers

Google OAuth2

Apple Login

Enterprise SAML

OIDC Providers

Security Enhancements

MFA

Device Trust

Session Risk Analysis

Summary

Auth Service는 MSA 환경에서 다음 역할을 수행한다.

Central Authentication Provider

핵심 책임

OAuth2 Login
SSO Session
Ticket Issuing
Authentication Validation

시스템 구조

Auth Service
User Service
Document Service
Block Service
Permission Service

인증 대상

Intro Page
Service Page
Admin Page

필요하면 다음 단계 문서도 만들어 줄 수 있습니다.

예를 들어

02_auth_sequence.md (SSO 인증 시퀀스 다이어그램)

03_auth_api_spec.md (Auth API 설계)

04_auth_db_schema.md (Auth DB 구조)

05_auth_ticket_design.md (SSO 티켓 설계)

특히 AI-agent 협업용이라면 다음 문서가 가장 중요합니다

auth-api-spec.md
auth-sequence.md
auth-state-machine.md

원하시면 AI-agent 협업에 최적화된 Auth Service 문서 구조도 같이 설계해 드리겠습니다.