package com.authservice.app.domain.auth.sso.model;

public record SsoTargetPage(SsoPageType pageType, String redirectUri) {
}
