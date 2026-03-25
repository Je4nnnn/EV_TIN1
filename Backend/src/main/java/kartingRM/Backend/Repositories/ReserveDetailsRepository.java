package kartingRM.Backend.Repositories;

import kartingRM.Backend.Entities.ReserveDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReserveDetailsRepository extends JpaRepository<ReserveDetailsEntity, Long> {
}