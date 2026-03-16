package com.authservice.app.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.authservice.app.common.aop.logging.context.MDCManager;

@Component
public class RequestMdcFilter extends OncePerRequestFilter {

	private static final String REQUEST_ID_HEADER = "X-Request-Id";
	private static final String MDC_REQUEST_ID = "request_id";
	private static final String MDC_HTTP_METHOD = "http_method";
	private static final String MDC_REQUEST_URI = "request_uri";
	private static final String MDC_SERVICE_NAME = "service_name";
	private static final String SERVICE_NAME = "auth-service";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		String requestId = resolveRequestId(request);
		response.setHeader(REQUEST_ID_HEADER, requestId);

		MDCManager.put(MDC_REQUEST_ID, requestId);
		MDCManager.put(MDC_HTTP_METHOD, request.getMethod());
		MDCManager.put(MDC_REQUEST_URI, request.getRequestURI());
		MDCManager.put(MDC_SERVICE_NAME, SERVICE_NAME);

		try {
			filterChain.doFilter(request, response);
		} finally {
			MDCManager.remove(MDC_REQUEST_ID);
			MDCManager.remove(MDC_HTTP_METHOD);
			MDCManager.remove(MDC_REQUEST_URI);
			MDCManager.remove(MDC_SERVICE_NAME);
		}
	}

	private String resolveRequestId(HttpServletRequest request) {
		String requestId = request.getHeader(REQUEST_ID_HEADER);
		if (requestId == null || requestId.isBlank()) {
			return UUID.randomUUID().toString();
		}
		return requestId;
	}
}
