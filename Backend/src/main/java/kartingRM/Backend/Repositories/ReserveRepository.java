package kartingRM.Backend.Repositories;

import kartingRM.Backend.Entities.ReserveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReserveRepository extends JpaRepository<ReserveEntity, Long> {
}