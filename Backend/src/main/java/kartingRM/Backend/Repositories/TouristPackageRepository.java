package kartingRM.Backend.Repositories;

import kartingRM.Backend.Entities.TouristPackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TouristPackageRepository extends JpaRepository<TouristPackageEntity, Long> {
    List<TouristPackageEntity> findByAvailableTrueOrderByPackageNameAsc();
    List<TouristPackageEntity> findByStatusIgnoreCaseOrderByPackageNameAsc(String status);
}
