package br.com.jorgemelo.nimbusfilemanager.shared.domain.repository.projection;

public interface MediaLocationReconcileProjection {

	Long getCatalogFileId();

	String getFileKey();

	String getCurrentPath();
}