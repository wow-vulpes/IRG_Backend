package sber.deonande.obligation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ObligationRepository extends JpaRepository<Obligation, UUID> {
    
    Optional<Obligation> findByTitleIgnoreCaseAndStatus(String title, Status status);
}
