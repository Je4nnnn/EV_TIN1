package kartingRM.Backend.Controllers;

import kartingRM.Backend.DTOs.ApiMessageResponse;
import kartingRM.Backend.DTOs.PackageRankingRow;
import kartingRM.Backend.DTOs.SalesReportRow;
import kartingRM.Backend.Entities.ReservationEntity;
import kartingRM.Backend.Services.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reservations")
@CrossOrigin("*")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @GetMapping
    @PreAuthorize("hasRole('HOTELRM_ADMIN')")
    public ResponseEntity<List<ReservationEntity>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('HOTELRM_ADMIN')")
    public ResponseEntity<ReservationEntity> getReservationById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReservationEntity> addReservation(@RequestBody ReservationEntity reserve) {
        return ResponseEntity.ok(reservationService.saveReservation(reserve));
    }

    @PostMapping("/confirmar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReservationEntity> confirmarReserva(@RequestBody ReservationEntity reserve) {
        return ResponseEntity.ok(reservationService.saveReservation(reserve));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HOTELRM_ADMIN')")
    public ResponseEntity<ReservationEntity> updateReservation(
            @PathVariable("id") Long id,
            @RequestBody ReservationEntity reserve
    ) {
        return ResponseEntity.ok(reservationService.updateReservation(id, reserve));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HOTELRM_ADMIN')")
    public ResponseEntity<ApiMessageResponse> deleteReservation(@PathVariable("id") Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.ok(new ApiMessageResponse("Reserva eliminada correctamente."));
    }

    @GetMapping("/reports/room-type")
    @PreAuthorize("hasRole('HOTELRM_ADMIN')")
    public ResponseEntity<Map<String, Map<String, Double>>> getReportePorTipoHabitacion(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(
                reservationService.getReporteIngresosPorVueltasOTiempo(fechaInicio, fechaFin)
        );
    }

    @GetMapping("/reports/guest-count")
    @PreAuthorize("hasRole('HOTELRM_ADMIN')")
    public ResponseEntity<Map<String, Map<String, Double>>> getReportePorCantidadPersonas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(
                reservationService.getReporteIngresosPorCantidadDePersonas(fechaInicio, fechaFin)
        );
    }

    @GetMapping("/reports/sales")
    @PreAuthorize("hasRole('HOTELRM_ADMIN')")
    public ResponseEntity<List<SalesReportRow>> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(reservationService.getSalesReport(fechaInicio, fechaFin));
    }

    @GetMapping("/reports/package-ranking")
    @PreAuthorize("hasRole('HOTELRM_ADMIN')")
    public ResponseEntity<List<PackageRankingRow>> getPackageRankingReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(reservationService.getPackageRankingReport(fechaInicio, fechaFin));
    }
}
