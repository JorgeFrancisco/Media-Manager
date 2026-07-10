package br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config.properties.dto;

import org.springframework.boot.context.properties.bind.DefaultValue;

public record Gmail(@DefaultValue("false") boolean enabled, @DefaultValue("") String username,
		@DefaultValue("") String password) {
}