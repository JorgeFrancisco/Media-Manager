package br.com.jorgemelo.nimbusfilemanager.settings.application.dto;

public record AppSettingDefinition(String key, String defaultValue, String valueType, String description) {
}