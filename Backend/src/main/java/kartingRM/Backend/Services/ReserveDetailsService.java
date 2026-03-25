package kartingRM.Backend.Services;

import kartingRM.Backend.Entities.ReserveDetailsEntity;
import kartingRM.Backend.Repositories.ReserveDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReserveDetailsService {

    @Autowired
    private ReserveDetailsRepository reserveDetailsRepository;

    public List<ReserveDetailsEntity> getAllReserveDetails() {
        return reserveDetailsRepository.findAll();
    }

    public ReserveDetailsEntity getReserveDetailById(Long id) {
        Optional<ReserveDetailsEntity> optionalDetail = reserveDetailsRepository.findById(id);
        if (optionalDetail.isPresent()) {
            return optionalDetail.get();
        } else {
            throw new RuntimeException("Detalle de reserva no encontrado con ID: " + id);
        }
    }

    public ReserveDetailsEntity saveReserveDetail(ReserveDetailsEntity reserveDetail) {
        return reserveDetailsRepository.save(reserveDetail);
    }
    
    public ReserveDetailsEntity updateReserveDetail(Long id, ReserveDetailsEntity updatedDetail) {
        ReserveDetailsEntity existingDetail = getReserveDetailById(id);
        existingDetail.setMemberName(updatedDetail.getMemberName());
        existingDetail.setDateBirthday(updatedDetail.getDateBirthday());
        existingDetail.setDiscount(updatedDetail.getDiscount());
        return reserveDetailsRepository.save(existingDetail);
    }

    public void deleteReserveDetail(Long id) {
        reserveDetailsRepository.deleteById(id);
    }
}