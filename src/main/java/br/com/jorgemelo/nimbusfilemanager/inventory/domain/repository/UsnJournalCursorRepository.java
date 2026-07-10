package br.com.jorgemelo.nimbusfilemanager.inventory.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.jorgemelo.nimbusfilemanager.inventory.domain.model.UsnJournalCursor;

public interface UsnJournalCursorRepository extends JpaRepository<UsnJournalCursor, Long> {

	Optional<UsnJournalCursor> findByVolumeKey(String volumeKey);
}