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
    @PreAuthorize("@authorizationRules.hasAdmin(authentication)")
    public List<ReservationDetailsEntity> getAllReservationDetails() {
        return reservationDetailsService.getAllReservationDetails();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@authorizationRules.hasAdmin(authentication)")
    public ReservationDetailsEntity getReservationDetailById(@PathVariable("id") Long id) {
        return reservationDetailsService.getReservationDetailById(id);
    }

    @PostMapping("/")
    @PreAuthorize("@authorizationRules.hasAdmin(authentication)")
    public ReservationDetailsEntity addReservationDetail(@RequestBody ReservationDetailsEntity reserveDetail) {
        return reservationDetailsService.saveReservationDetail(reserveDetail);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@authorizationRules.hasAdmin(authentication)")
    public ReservationDetailsEntity updateReservationDetail(@PathVariable("id") Long id, @RequestBody ReservationDetailsEntity updatedDetail) {
        return reservationDetailsService.updateReservationDetail(id, updatedDetail);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@authorizationRules.hasAdmin(authentication)")
    public void deleteReservationDetail(@PathVariable("id") Long id) {
        reservationDetailsService.deleteReservationDetail(id);
    }
}
