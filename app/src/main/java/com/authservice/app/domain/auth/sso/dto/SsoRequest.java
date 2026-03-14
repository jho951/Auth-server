package com.authservice.app.domain.auth.sso.dto;

import jakarta.validation.constraints.NotBlank;

public class SsoRequest {

	public static class ExchangeRequest {
		@NotBlank
		private String ticket;

		public String getTicket() {
			return ticket;
		}

		public void setTicket(String ticket) {
			this.ticket = ticket;
		}
	}
}
