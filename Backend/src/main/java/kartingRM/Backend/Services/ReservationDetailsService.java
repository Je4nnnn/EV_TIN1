package kartingRM.Backend.Services;

import kartingRM.Backend.Entities.ReservationDetailsEntity;
import kartingRM.Backend.Repositories.ReservationDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReservationDetailsService {

    @Autowired
    private ReservationDetailsRepository reservationDetailsRepository;

    public List<ReservationDetailsEntity> getAllReservationDetails() {
        return reservationDetailsRepository.findAll();
    }

    public ReservationDetailsEntity getReservationDetailById(Long id) {
        Optional<ReservationDetailsEntity> optionalDetail = reservationDetailsRepository.findById(id);
        if (optionalDetail.isPresent()) {
            return optionalDetail.get();
        } else {
            throw new RuntimeException("Detalle de reserva no encontrado con ID: " + id);
        }
    }

    public ReservationDetailsEntity saveReservationDetail(ReservationDetailsEntity reserveDetail) {
        return reservationDetailsRepository.save(reserveDetail);
    }

    public ReservationDetailsEntity updateReservationDetail(Long id, ReservationDetailsEntity updatedDetail) {
        ReservationDetailsEntity existingDetail = getReservationDetailById(id);
        existingDetail.setGuestName(updatedDetail.getGuestName());
        existingDetail.setDiscount(updatedDetail.getDiscount());
        existingDetail.setFinalAmount(updatedDetail.getFinalAmount());
        return reservationDetailsRepository.save(existingDetail);
    }

    public void deleteReservationDetail(Long id) {
        reservationDetailsRepository.deleteById(id);
    }
}