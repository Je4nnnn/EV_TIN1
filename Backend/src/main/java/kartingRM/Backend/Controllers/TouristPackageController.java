package kartingRM.Backend.Controllers;

import kartingRM.Backend.DTOs.ApiMessageResponse;
import kartingRM.Backend.Entities.TouristPackageEntity;
import kartingRM.Backend.Services.TouristPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tourist-packages")
@CrossOrigin("*")
public class TouristPackageController {

    @Autowired
    private TouristPackageService touristPackageService;

    @GetMapping
    public ResponseEntity<List<TouristPackageEntity>> getAllPackages(
            @RequestParam(value = "availableOnly", required = false, defaultValue = "false") boolean availableOnly
    ) {
        if (availableOnly) {
            return ResponseEntity.ok(touristPackageService.getAvailablePackages());
        }
        return ResponseEntity.ok(touristPackageService.getAllPackages());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TouristPackageEntity> getPackageById(@PathVariable Long id) {
        return ResponseEntity.ok(touristPackageService.getPackageById(id));
    }

    @PostMapping
    public ResponseEntity<TouristPackageEntity> createPackage(@RequestBody TouristPackageEntity touristPackage) {
        return ResponseEntity.ok(touristPackageService.createPackage(touristPackage));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TouristPackageEntity> updatePackage(
            @PathVariable Long id,
            @RequestBody TouristPackageEntity touristPackage
    ) {
        return ResponseEntity.ok(touristPackageService.updatePackage(id, touristPackage));
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<TouristPackageEntity> updateAvailability(
            @PathVariable Long id,
            @RequestParam("available") boolean available
    ) {
        return ResponseEntity.ok(touristPackageService.updateAvailability(id, available));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiMessageResponse> deletePackage(@PathVariable Long id) {
        touristPackageService.deletePackage(id);
        return ResponseEntity.ok(new ApiMessageResponse("Paquete turistico eliminado correctamente."));
    }
}
