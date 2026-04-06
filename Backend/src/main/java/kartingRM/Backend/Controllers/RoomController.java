package kartingRM.Backend.Controllers;

import kartingRM.Backend.Entities.RoomEntity;
import kartingRM.Backend.Services.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
@CrossOrigin("*")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping("/")
    public List<RoomEntity> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/{id}")
    public RoomEntity getRoomById(@PathVariable("id") long id) {
        return roomService.getRoomById(id);
    }

    @PostMapping("/")
    public RoomEntity addRoom(@RequestBody RoomEntity room) {
        return roomService.saveRoom(room);
    }
}