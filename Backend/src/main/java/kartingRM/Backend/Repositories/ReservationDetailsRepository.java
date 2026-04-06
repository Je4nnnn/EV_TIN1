package kartingRM.Backend.Repositories;

import kartingRM.Backend.Entities.ReservationDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationDetailsRepository extends JpaRepository<ReservationDetailsEntity, Long> {
}