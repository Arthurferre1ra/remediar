package br.com.remediar.domain.repository;

import br.com.remediar.domain.enums.DonationFlowStatus;
import br.com.remediar.domain.model.DonationMatch;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DonationMatchRepository extends JpaRepository<DonationMatch, Long> {

    boolean existsByMedicationIdAndStatusIn(Long medicationId, Collection<DonationFlowStatus> statuses);

    List<DonationMatch> findByStatusAndDeliveryDeadlineBefore(DonationFlowStatus status, LocalDate date);

    @Query("""
            select d from DonationMatch d
            join fetch d.medication m
            join fetch d.institution i
            where d.id = :id
            """)
    Optional<DonationMatch> findDetailedById(@Param("id") Long id);

    @Query("""
            select d from DonationMatch d
            join fetch d.medication m
            join fetch d.institution i
            where d.institution.id = :institutionId
              and (:medicationName is null or lower(m.commercialName) like lower(concat('%', :medicationName, '%')))
              and (:lotNumber is null or lower(m.lotNumber) like lower(concat('%', :lotNumber, '%')))
              and (:deliveryDate is null or d.deliveryDeadline = :deliveryDate)
            order by d.matchedAt desc
            """)
    List<DonationMatch> searchForInstitution(
            @Param("institutionId") Long institutionId,
            @Param("medicationName") String medicationName,
            @Param("lotNumber") String lotNumber,
            @Param("deliveryDate") LocalDate deliveryDate
    );
}
