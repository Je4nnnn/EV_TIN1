package kartingRM.Backend.repositories;

import kartingRM.Backend.Entities.RoomEntity;
import kartingRM.Backend.Repositories.RoomRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void findByRoomNumber_givenExistingRoom_returnsMatch() {
        RoomEntity room = new RoomEntity(null, "S001", "Simple", "AVAILABLE");
        roomRepository.save(room);

        Optional<RoomEntity> found = roomRepository.findByRoomNumber("S001");

        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo("Simple");
    }

    @Test
    void findByTypeIgnoreCaseOrderByRoomNumberAsc_ordersRoomsByNumber() {
        roomRepository.save(new RoomEntity(null, "S002", "Simple", "AVAILABLE"));
        roomRepository.save(new RoomEntity(null, "S001", "Simple", "AVAILABLE"));
        roomRepository.save(new RoomEntity(null, "D001", "Double", "AVAILABLE"));

        List<RoomEntity> rooms = roomRepository.findByTypeIgnoreCaseOrderByRoomNumberAsc("simple");

        assertThat(rooms).hasSize(2);
        assertThat(rooms).extracting(RoomEntity::getRoomNumber).containsExactly("S001", "S002");
    }
}
