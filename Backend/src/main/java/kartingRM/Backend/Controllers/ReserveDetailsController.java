package kartingRM.Backend.Controllers;

import kartingRM.Backend.Entities.ReserveDetailsEntity;
import kartingRM.Backend.Services.ReserveDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reserve-details")
@CrossOrigin("*")
public class ReserveDetailsController {

    @Autowired
    private ReserveDetailsService reserveDetailsService;

    @GetMapping("/")
    public List<ReserveDetailsEntity> getAllReserveDetails() {
        return reserveDetailsService.getAllReserveDetails();
    }

    @GetMapping("/{id}")
    public ReserveDetailsEntity getReserveDetailById(@PathVariable("id") Long id) {
        return reserveDetailsService.getReserveDetailById(id);
    }

    @PostMapping("/")
    public ReserveDetailsEntity addReserveDetail(@RequestBody ReserveDetailsEntity reserveDetail) {
        return reserveDetailsService.saveReserveDetail(reserveDetail);
    }

    @PutMapping("/{id}")
    public ReserveDetailsEntity updateReserveDetail(@PathVariable("id") Long id, @RequestBody ReserveDetailsEntity updatedDetail) {
        return reserveDetailsService.updateReserveDetail(id, updatedDetail);
    }

    @DeleteMapping("/{id}")
    public void deleteReserveDetail(@PathVariable("id") Long id) {
        reserveDetailsService.deleteReserveDetail(id);
    }
}