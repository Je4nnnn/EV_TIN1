package kartingRM.Backend.Repositories;

import kartingRM.Backend.Entities.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
    List<ReservationEntity> findByCancelledFalse();
    List<ReservationEntity> findByRoomIdAndCancelledFalse(Long roomId);
    List<ReservationEntity> findByRoomIdInAndCancelledFalse(List<Long> roomIds);
    List<ReservationEntity> findByClienteRutIgnoreCaseAndCancelledFalse(String rut);
}
