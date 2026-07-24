package br.com.jorgemelo.nimbusfilemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config.WorkspaceBootstrapListener;
import br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config.properties.BoundaryDatasetProperties;
import br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config.properties.InventoryWatchProperties;
import br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config.properties.LocationRebuildProperties;
import br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config.properties.UsnJournalProperties;
import br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config.properties.VideoSimilarityProperties;
import br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config.properties.dto.NimbusFileManagerProperties;
import br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config.properties.dto.ProcessingProperties;

@SpringBootApplication
@EnableConfigurationProperties({ NimbusFileManagerProperties.class, BoundaryDatasetProperties.class,
		ProcessingProperties.class, InventoryWatchProperties.class, LocationRebuildProperties.class,
		UsnJournalProperties.class, VideoSimilarityProperties.class })
public class NimbusFileManagerApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(NimbusFileManagerApplication.class);

		application.addListeners(new WorkspaceBootstrapListener());

		application.run(args);
	}
}