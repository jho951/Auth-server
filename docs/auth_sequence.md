# Auth Service Sequence Diagrams

## Overview

이 문서는 Auth Service에서 사용하는 **SSO 인증 흐름**을 정의한다.

현재 인증 방식은 다음과 같다.

GitHub OAuth2  
→ Auth Service  
→ Internal User Mapping  
→ SSO Ticket + Session 발급

---

# 1. Login Sequence (GitHub OAuth2)

## Step-by-step Flow

```text
User
 │
 ▼
Intro Page
 │
 ▼
Auth Service (/auth/login/github)
 │
 ▼
GitHub OAuth Authorization
 │
 ▼
GitHub Login Page
 │
 ▼
User Login
 │
 ▼
GitHub Redirect
 │
 ▼
Auth Service (/auth/oauth/github/callback)
 │
 ├ verify OAuth token
 ├ fetch GitHub user
 ├ find or create internal user
 ├ create session
 └ issue SSO ticket
 │
 ▼
Client Redirect