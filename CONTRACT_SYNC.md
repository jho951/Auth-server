# Contract Sync (Auth-server)

- Contract Source: https://github.com/jho951/contract
- Service SoT Branch: `main`
- Contract Role: Authentication/session/token owner
- Responsibility Split: auth-service authenticates, authz-service owns capability truth, user-service owns visibility/privacy

## Required Links
- Common README: https://github.com/jho951/contract/blob/main/contracts/common/README.md
- Routing: https://github.com/jho951/contract/blob/main/contracts/common/routing.md
- Security: https://github.com/jho951/contract/blob/main/contracts/common/security.md
- Env: https://github.com/jho951/contract/blob/main/contracts/common/env.md
- Auth README: https://github.com/jho951/contract/blob/main/contracts/auth/README.md
- Auth v2: https://github.com/jho951/contract/blob/main/contracts/auth/v2.md
- Auth OpenAPI v2: https://github.com/jho951/contract/blob/main/contracts/openapi/auth-service.v2.yaml
- Authz README: https://github.com/jho951/contract/blob/main/contracts/authz/README.md
- Authz v2: https://github.com/jho951/contract/blob/main/contracts/authz/v2.md
- Authz OpenAPI v2: https://github.com/jho951/contract/blob/main/contracts/openapi/authz-service.v2.yaml
- User OpenAPI: https://github.com/jho951/contract/blob/main/contracts/openapi/user-service.v1.yaml
- User Visibility: https://github.com/jho951/contract/blob/main/contracts/user/visibility.md

## Sync Checklist
- [ ] `USER_SERVICE_BASE_URL` uses service DNS (`http://user-service:8082`)
- [ ] internal JWT claims (`iss/aud/scope`) match contract
- [ ] auth/security dependency set stays aligned with `platform-security` published modules and version matrix
- [ ] `SecurityContextResolver` is provided explicitly through `PlatformSecurityContextResolvers.hybrid(...)` or another production resolver
- [ ] SSO provider and MFA step-up behavior match `contracts/auth/v2.md`
- [ ] authz snapshots/claims emitted by auth align with `contracts/authz/v2.md`
- [ ] user provisioning uses contract-defined endpoints
- [ ] visibility/privacy policy remains owned by user-service, not auth-service
- [ ] gateway-facing auth behavior documented by contract
