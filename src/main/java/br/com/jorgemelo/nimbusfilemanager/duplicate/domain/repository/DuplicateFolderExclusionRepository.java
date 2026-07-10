package br.com.jorgemelo.nimbusfilemanager.duplicate.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.model.DuplicateFolderExclusion;

public interface DuplicateFolderExclusionRepository extends JpaRepository<DuplicateFolderExclusion, Long> {

	boolean existsByFolderPath(String folderPath);

	@Query("SELECT e.folderPath FROM DuplicateFolderExclusion e")
	List<String> findAllFolderPaths();
}