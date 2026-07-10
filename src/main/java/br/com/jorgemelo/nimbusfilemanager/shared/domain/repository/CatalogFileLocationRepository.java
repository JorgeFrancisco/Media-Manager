package br.com.jorgemelo.nimbusfilemanager.shared.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.model.CatalogFileLocation;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.repository.projection.FileExplorerLocationProjection;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.repository.projection.MediaLocationReconcileProjection;

public interface CatalogFileLocationRepository extends JpaRepository<CatalogFileLocation, Long> {

	Optional<CatalogFileLocation> findByCatalogFileIdAndCurrentPath(Long catalogFileId, String currentPath);

	@Query("""
			select l.catalogFile.id as catalogFileId,
			       l.catalogFile.publicId as publicId,
			       l.catalogFile.fileName as fileName,
			       l.catalogFile.fileType as fileType,
			       l.catalogFile.sizeBytes as sizeBytes,
			       l.currentPath as currentPath
			from CatalogFileLocation l
			where l.catalogFile.lifecycleStatus = br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.LifecycleStatus.ACTIVE
			  and lower(l.currentFolder) = lower(:currentFolder)
			order by l.catalogFile.fileName asc, l.catalogFile.id asc
			""")
	List<FileExplorerLocationProjection> findActiveByCurrentFolder(
			@Param("currentFolder") String currentFolder);

	@Query("""
			select l.catalogFile.id as catalogFileId,
			       l.catalogFile.fileKey as fileKey,
			       l.currentPath as currentPath
			from CatalogFileLocation l
			where l.catalogFile.lifecycleStatus = br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.LifecycleStatus.ACTIVE
			  and (
			       lower(l.currentPath) = lower(:sourcePath)
			       or lower(l.currentPath) like lower(:descendantPattern) escape '\\'
			  )
			order by l.id
			""")
	Slice<MediaLocationReconcileProjection> findForReconcile(@Param("sourcePath") String sourcePath,
			@Param("descendantPattern") String descendantPattern, Pageable pageable);
}