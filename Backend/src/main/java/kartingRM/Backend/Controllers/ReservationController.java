package kartingRM.Backend.Controllers;

import kartingRM.Backend.Entities.ReservationDetailsEntity;
import kartingRM.Backend.Entities.ReservationEntity;
import kartingRM.Backend.Services.ReservationDetailsService;
import kartingRM.Backend.Services.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/reservations")
@CrossOrigin("*")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationDetailsService reservationDetailsService;

    @PostMapping("/confirmar")
    public ResponseEntity<?> confirmarReserva(@RequestBody ReservationEntity reserva) {
        if (reserva.getDetails() == null || reserva.getDetails().isEmpty()) {
            return ResponseEntity.badRequest().body("La reserva debe incluir al menos un detalle.");
        }

        for (ReservationDetailsEntity detalle : reserva.getDetails()) {
            if (detalle.getUserId() == null) {
                return ResponseEntity.badRequest().body("Cada detalle debe incluir un userId válido.");
            }
        }

        if (reserva.getCheckInDate() == null) {
            return ResponseEntity.badRequest().body("La reserva debe incluir una fecha de check-in.");
        }

        if (reserva.getCheckOutDate() == null) {
            return ResponseEntity.badRequest().body("La reserva debe incluir una fecha de check-out.");
        }

        if (reserva.getCheckOutDate().isBefore(reserva.getCheckInDate()) ||
            reserva.getCheckOutDate().isEqual(reserva.getCheckInDate())) {
            return ResponseEntity.badRequest().body("La fecha de check-out debe ser posterior a la de check-in.");
        }

        if (reserva.getStayType() == null || reserva.getStayType().isEmpty()) {
            return ResponseEntity.badRequest().body("Debe especificar el tipo de estancia (Mañana, Noche o Completo).");
        }

        try {
            ReservationEntity savedReserva = reservationService.saveReservation(reserva);
            return ResponseEntity.ok(savedReserva);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al confirmar la reserva: " + e.getMessage());
        }
    }

    @GetMapping("/")
    public List<Map<String, String>> getAllReservations() {
        return reservationService.getAllReservations().stream().map(reserve -> {
            Map<String, String> formattedReserve = new HashMap<>();
            formattedReserve.put("checkInDate", reserve.getCheckInDate() != null ? reserve.getCheckInDate().toString() : "");
            formattedReserve.put("checkOutDate", reserve.getCheckOutDate() != null ? reserve.getCheckOutDate().toString() : "");
            formattedReserve.put("stayType", reserve.getStayType() != null ? reserve.getStayType() : "");
            formattedReserve.put("roomType", reserve.getRoomType() != null ? reserve.getRoomType() : "");
            formattedReserve.put("reservationCode", reserve.getReservationCode() != null ? reserve.getReservationCode() : "");
            return formattedReserve;
        }).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ReservationEntity getReservationById(@PathVariable("id") Long id) {
        return reservationService.getReservationById(id);
    }

    @PostMapping("/")
    public ReservationEntity addReservation(@RequestBody ReservationEntity reserve) {
        return reservationService.saveReservation(reserve);
    }

    @PutMapping("/{id}")
    public ReservationEntity updateReservation(@PathVariable("id") Long id, @RequestBody ReservationEntity reserve) {
        return reservationService.updateReservation(id, reserve);
    }

    @DeleteMapping("/{id}")
    public void deleteReservation(@PathVariable("id") Long id) {
        reservationService.deleteReservation(id);
    }

    @GetMapping("/reporte/tipo-habitacion")
    public ResponseEntity<Map<String, Map<String, Double>>> getReportePorTipoHabitacion(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        Map<String, Map<String, Double>> reporte = reservationService.getReporteIngresosPorVueltasOTiempo(fechaInicio, fechaFin);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/reporte/cantidad-personas")
    public ResponseEntity<Map<String, Map<String, Double>>> getReportePorCantidadPersonas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        Map<String, Map<String, Double>> reporte = reservationService.getReporteIngresosPorCantidadDePersonas(fechaInicio, fechaFin);
        return ResponseEntity.ok(reporte);
    }
}