package br.com.jorgemelo.nimbusfilemanager.security.infrastructure.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.com.jorgemelo.nimbusfilemanager.security.application.UserAccessLogService;

@Controller
public class AccessLogWebController {

	private final UserAccessLogService userAccessLogService;

	public AccessLogWebController(UserAccessLogService userAccessLogService) {
		this.userAccessLogService = userAccessLogService;
	}

	@GetMapping("/app/accesses")
	public String accesses(@RequestParam(required = false) String email, Model model) {
		String normalizedEmail = email == null ? "" : email.trim();

		boolean searched = !normalizedEmail.isBlank();

		model.addAttribute("email", normalizedEmail);
		model.addAttribute("searched", searched);
		model.addAttribute("accessLogs", searched ? userAccessLogService.findByEmail(normalizedEmail) : List.of());

		return "app/accesses";
	}
}