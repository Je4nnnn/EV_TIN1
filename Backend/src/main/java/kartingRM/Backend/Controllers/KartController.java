package kartingRM.Backend.Controllers;

import kartingRM.Backend.Entities.KartEntity;
import kartingRM.Backend.Services.KartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/karts")
@CrossOrigin("*")
public class KartController {

    @Autowired
    private KartService kartService;

    @GetMapping("/")
    public List<KartEntity> getAllKarts() {
        return kartService.getAllKarts();
    }

    @GetMapping("/{id}")
    public KartEntity getKartById(@PathVariable("id") long id) {
        return kartService.getKartById(id);
    }

    @PostMapping("/")
    public KartEntity addKart(@RequestBody KartEntity kart) {
        return kartService.saveKart(kart);
    }
}