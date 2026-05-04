package br.com.remediar.domain.repository;

import br.com.remediar.domain.enums.InstitutionStatus;
import br.com.remediar.domain.model.Institution;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {

    List<Institution> findByStatus(InstitutionStatus status);
}
