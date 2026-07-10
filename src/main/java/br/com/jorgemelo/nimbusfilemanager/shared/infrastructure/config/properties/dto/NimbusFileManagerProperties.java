package br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config.properties.dto;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "nimbus-file-manager")
public record NimbusFileManagerProperties(

		String workspace, List<String> workspaceFolders, String defaultLayout,

		Tools tools, Inventory inventory, Api api, Metadata metadata, Duplicates duplicates, Security security,
		@DefaultValue Email email) {
}