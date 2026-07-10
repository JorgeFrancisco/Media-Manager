package br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Backs the idle-timeout modal's "Continuar conectado" button
 * (pages/idle-timeout.js): any authenticated request already resets the servlet
 * container's session-last-accessed clock before this method runs, so all it
 * needs to do is exist behind authentication and return quickly.
 */
@RestController
public class SessionWebController {

	@GetMapping("/app/session/keep-alive")
	public Map<String, Boolean> keepAlive() {
		return Map.of("ok", true);
	}
}