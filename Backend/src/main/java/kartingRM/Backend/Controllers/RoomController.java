package kartingRM.Backend.Controllers;

import kartingRM.Backend.DTOs.RoomAvailabilityResponse;
import kartingRM.Backend.Entities.RoomEntity;
import kartingRM.Backend.Services.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
@CrossOrigin("*")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping
    public ResponseEntity<List<RoomEntity>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomEntity> getRoomById(@PathVariable("id") long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @GetMapping("/overview")
    public ResponseEntity<List<RoomAvailabilityResponse>> getRoomOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate
    ) {
        return ResponseEntity.ok(roomService.getRoomAvailabilityOverview(referenceDate));
    }

    @GetMapping("/available")
    public ResponseEntity<List<RoomEntity>> getAvailableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam String roomType,
            @RequestParam String stayType
    ) {
        return ResponseEntity.ok(roomService.getAvailableRooms(checkInDate, checkOutDate, roomType, stayType));
    }

    @PostMapping
    public ResponseEntity<RoomEntity> addRoom(@RequestBody RoomEntity room) {
        return ResponseEntity.ok(roomService.saveRoom(room));
    }
}
