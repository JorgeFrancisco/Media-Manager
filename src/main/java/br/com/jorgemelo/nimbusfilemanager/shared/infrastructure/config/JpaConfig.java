package br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import br.com.jorgemelo.nimbusfilemanager.NimbusFileManagerApplication;

@Configuration
@EnableJpaRepositories(basePackageClasses = NimbusFileManagerApplication.class)
public class JpaConfig {
}