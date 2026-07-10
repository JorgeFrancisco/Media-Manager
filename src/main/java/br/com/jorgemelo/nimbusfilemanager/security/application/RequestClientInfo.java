package br.com.jorgemelo.nimbusfilemanager.security.application;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Web-boundary helper that extracts the client IP and user-agent from an
 * {@link HttpServletRequest} exactly once, so the domain services
 * ({@code UserAccessLogService}, {@code AccountLockService}) receive plain
 * strings and never depend on the servlet API. The security web handlers and
 * auth controllers build one of these at the boundary and forward the
 * primitives inward.
 */
public record RequestClientInfo(String ipAddress, String userAgent) {

	public static RequestClientInfo from(HttpServletRequest request) {
		if (request == null) {
			return new RequestClientInfo(null, null);
		}

		return new RequestClientInfo(ipAddress(request), request.getHeader("User-Agent"));
	}

	private static String ipAddress(HttpServletRequest request) {
		String forwardedFor = request.getHeader("X-Forwarded-For");

		if (forwardedFor != null && !forwardedFor.isBlank()) {
			return normalizeLoopback(forwardedFor.split(",")[0].trim());
		}

		return normalizeLoopback(request.getRemoteAddr());
	}

	/**
	 * When the client and server are the same machine (eg. accessing via
	 * {@code localhost}), {@code getRemoteAddr()} returns the fully-expanded IPv6
	 * loopback address ({@code 0:0:0:0:0:0:0:1}) rather than the more familiar
	 * {@code 127.0.0.1} - normalize both IPv6 loopback spellings so the access log
	 * stays readable.
	 */
	private static String normalizeLoopback(String address) {
		if ("0:0:0:0:0:0:0:1".equals(address) || "::1".equals(address)) {
			return "127.0.0.1";
		}

		return address;
	}
}