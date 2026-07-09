package sber.deonande.obligation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ObligationRepository extends JpaRepository<Obligation, UUID> {
    
    Optional<Obligation> findByTitleIgnoreCaseAndStatus(String title, Status status);

    @Modifying
    @Query("UPDATE Obligation o SET o.status = 'EXPIRED' WHERE o.status = 'ACTIVE' AND o.recurrence IS NULL AND o.nextPaymentDate < :today")
    void applyLazyExpiry(LocalDate today);

    List<Obligation> findByNextPaymentDateBetweenOrderByNextPaymentDateAsc(LocalDate start, LocalDate end);

    @Query("SELECT o FROM Obligation o WHERE " +
           "(:category IS NULL OR o.category = :category) AND " +
           "(:status IS NULL OR o.status = :status) " +
           "ORDER BY o.nextPaymentDate ASC")
    List<Obligation> findFiltered(@Param("category") Category category, 
                                  @Param("status") Status status);
}
