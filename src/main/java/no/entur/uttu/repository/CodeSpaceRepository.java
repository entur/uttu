package no.entur.uttu.repository;

import no.entur.uttu.model.Codespace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeSpaceRepository extends JpaRepository<Codespace, Long> {
}
