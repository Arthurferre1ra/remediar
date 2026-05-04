package br.com.remediar.domain.repository;

import br.com.remediar.domain.model.Medication;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicationRepository extends JpaRepository<Medication, Long> {

    @Query("""
            select m from Medication m
            where (:name is null or lower(m.commercialName) like lower(concat('%', :name, '%')))
              and (:activeIngredient is null or lower(m.activeIngredient) like lower(concat('%', :activeIngredient, '%')))
              and (:lotNumber is null or lower(m.lotNumber) like lower(concat('%', :lotNumber, '%')))
              and (:expiresBefore is null or m.expirationDate <= :expiresBefore)
            order by m.createdAt desc
            """)
    List<Medication> search(
            @Param("name") String name,
            @Param("activeIngredient") String activeIngredient,
            @Param("lotNumber") String lotNumber,
            @Param("expiresBefore") LocalDate expiresBefore
    );
}
