package br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config.properties.dto;

import org.springframework.boot.context.properties.bind.DefaultValue;

public record Email(@DefaultValue Gmail gmail) {
}