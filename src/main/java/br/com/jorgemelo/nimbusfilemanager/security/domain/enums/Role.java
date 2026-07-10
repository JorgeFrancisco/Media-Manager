package br.com.jorgemelo.nimbusfilemanager.security.domain.enums;

/**
 * Application role of an {@code app_user} (Etapa 6 / 6.2), replacing the former
 * free {@code String}. Persisted by name and enforced by a DB CHECK constraint;
 * Spring Security authorities are derived as {@code "ROLE_" + name()}.
 */
public enum Role {

	ADMIN, USER
}