package com.authservice.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class RestAuthHandlers {

	@Component
	public static class EntryPoint implements AuthenticationEntryPoint {
		@Override
		public void commence(HttpServletRequest req, HttpServletResponse res,
			org.springframework.security.core.AuthenticationException e) throws IOException {
			res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			res.setContentType(MediaType.APPLICATION_JSON_VALUE);
			new ObjectMapper().writeValue(res.getOutputStream(), Map.of("error", "Unauthorized"));
		}
	}

	@Component
	public static class Denied implements AccessDeniedHandler {
		@Override
		public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException e) throws IOException {
			res.setStatus(HttpServletResponse.SC_FORBIDDEN);
			res.setContentType(MediaType.APPLICATION_JSON_VALUE);
			new ObjectMapper().writeValue(res.getOutputStream(), Map.of("error", "Forbidden"));
		}
	}
}
