package kartingRM.Backend.Controllers;

import kartingRM.Backend.Entities.ReservationDetailsEntity;
import kartingRM.Backend.Services.ReservationDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservation-details")
@CrossOrigin("*")
public class ReservationDetailsController {

    @Autowired
    private ReservationDetailsService reservationDetailsService;

    @GetMapping("/")
    @PreAuthorize("hasRole('HOTELRM_ADMIN')")
    public List<ReservationDetailsEntity> getAllReservationDetails() {
        return reservationDetailsService.getAllReservationDetails();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('HOTELRM_ADMIN')")
    public ReservationDetailsEntity getReservationDetailById(@PathVariable("id") Long id) {
        return reservationDetailsService.getReservationDetailById(id);
    }

    @PostMapping("/")
    @PreAuthorize("hasRole('HOTELRM_ADMIN')")
    public ReservationDetailsEntity addReservationDetail(@RequestBody ReservationDetailsEntity reserveDetail) {
        return reservationDetailsService.saveReservationDetail(reserveDetail);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HOTELRM_ADMIN')")
    public ReservationDetailsEntity updateReservationDetail(@PathVariable("id") Long id, @RequestBody ReservationDetailsEntity updatedDetail) {
        return reservationDetailsService.updateReservationDetail(id, updatedDetail);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HOTELRM_ADMIN')")
    public void deleteReservationDetail(@PathVariable("id") Long id) {
        reservationDetailsService.deleteReservationDetail(id);
    }
}
