package br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WellKnownWebController {

	@GetMapping("/.well-known/appspecific/com.chrome.devtools.json")
	public ResponseEntity<Void> chromeDevtools() {
		return ResponseEntity.noContent().build();
	}
}