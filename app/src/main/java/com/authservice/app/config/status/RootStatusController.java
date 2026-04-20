package com.authservice.app.config.status;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootStatusController {

	@GetMapping("/")
	public Map<String, String> status() {
		return Map.of(
			"service", "auth-service",
			"status", "UP"
		);
	}
}
