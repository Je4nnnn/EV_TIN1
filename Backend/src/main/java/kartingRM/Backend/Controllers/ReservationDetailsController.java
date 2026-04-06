package kartingRM.Backend.Controllers;

import kartingRM.Backend.Entities.ReservationDetailsEntity;
import kartingRM.Backend.Services.ReservationDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservation-details")
@CrossOrigin("*")
public class ReservationDetailsController {

    @Autowired
    private ReservationDetailsService reservationDetailsService;

    @GetMapping("/")
    public List<ReservationDetailsEntity> getAllReservationDetails() {
        return reservationDetailsService.getAllReservationDetails();
    }

    @GetMapping("/{id}")
    public ReservationDetailsEntity getReservationDetailById(@PathVariable("id") Long id) {
        return reservationDetailsService.getReservationDetailById(id);
    }

    @PostMapping("/")
    public ReservationDetailsEntity addReservationDetail(@RequestBody ReservationDetailsEntity reserveDetail) {
        return reservationDetailsService.saveReservationDetail(reserveDetail);
    }

    @PutMapping("/{id}")
    public ReservationDetailsEntity updateReservationDetail(@PathVariable("id") Long id, @RequestBody ReservationDetailsEntity updatedDetail) {
        return reservationDetailsService.updateReservationDetail(id, updatedDetail);
    }

    @DeleteMapping("/{id}")
    public void deleteReservationDetail(@PathVariable("id") Long id) {
        reservationDetailsService.deleteReservationDetail(id);
    }
}