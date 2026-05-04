package br.com.remediar.domain.repository;

import br.com.remediar.domain.model.MedicationBlacklistItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicationBlacklistRepository extends JpaRepository<MedicationBlacklistItem, Long> {

    @Query("""
            select b from MedicationBlacklistItem b
            where b.active = true
              and (
                b.blockedTypeCode = :typeCode
                or (b.activeIngredient is not null and lower(:activeIngredient) like lower(concat('%', b.activeIngredient, '%')))
                or (b.commercialName is not null and lower(:commercialName) like lower(concat('%', b.commercialName, '%')))
              )
            order by b.id asc
            """)
    java.util.List<MedicationBlacklistItem> findBlockingItems(
            @Param("typeCode") Integer typeCode,
            @Param("activeIngredient") String activeIngredient,
            @Param("commercialName") String commercialName
    );
}
