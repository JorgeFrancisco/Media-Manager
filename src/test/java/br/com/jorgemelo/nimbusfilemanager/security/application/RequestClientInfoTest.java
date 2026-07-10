package br.com.jorgemelo.nimbusfilemanager.security.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class RequestClientInfoTest {

	@Test
	void fromShouldPreferFirstForwardedForEntryAndReadUserAgent() {
		MockHttpServletRequest request = new MockHttpServletRequest();

		request.setRemoteAddr("127.0.0.1");
		request.addHeader("X-Forwarded-For", "10.0.0.1, 10.0.0.2");
		request.addHeader("User-Agent", "JUnit");

		RequestClientInfo client = RequestClientInfo.from(request);

		Assertions.assertThat(client.ipAddress()).isEqualTo("10.0.0.1");
		Assertions.assertThat(client.userAgent()).isEqualTo("JUnit");
	}

	@Test
	void fromShouldFallBackToRemoteAddrWhenNoForwardedForHeader() {
		MockHttpServletRequest request = new MockHttpServletRequest();

		request.setRemoteAddr("203.0.113.5");

		RequestClientInfo client = RequestClientInfo.from(request);

		Assertions.assertThat(client.ipAddress()).isEqualTo("203.0.113.5");
		Assertions.assertThat(client.userAgent()).isNull();
	}

	@Test
	void fromShouldNormalizeExpandedIpv6LoopbackRemoteAddrToIpv4() {
		MockHttpServletRequest request = new MockHttpServletRequest();

		request.setRemoteAddr("0:0:0:0:0:0:0:1");

		Assertions.assertThat(RequestClientInfo.from(request).ipAddress()).isEqualTo("127.0.0.1");
	}

	@Test
	void fromShouldNormalizeCompressedIpv6LoopbackFromForwardedForHeader() {
		MockHttpServletRequest request = new MockHttpServletRequest();

		request.setRemoteAddr("203.0.113.5");
		request.addHeader("X-Forwarded-For", "::1");

		Assertions.assertThat(RequestClientInfo.from(request).ipAddress()).isEqualTo("127.0.0.1");
	}

	@Test
	void fromShouldTolerateMissingRequest() {
		RequestClientInfo client = RequestClientInfo.from(null);

		Assertions.assertThat(client.ipAddress()).isNull();
		Assertions.assertThat(client.userAgent()).isNull();
	}
}