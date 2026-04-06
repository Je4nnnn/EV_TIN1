package kartingRM.Backend.Services;

import kartingRM.Backend.Entities.RoomEntity;
import kartingRM.Backend.Repositories.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    public List<RoomEntity> getAllRooms() {
        return roomRepository.findAll();
    }

    public RoomEntity getRoomById(long id) { return roomRepository.findById(id).get(); }

    public RoomEntity saveRoom(RoomEntity room) {
        return roomRepository.save(room);
    }
}