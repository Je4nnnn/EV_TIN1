package kartingRM.Backend.Config;

import kartingRM.Backend.Entities.RoomEntity;
import kartingRM.Backend.Entities.UserEntity;
import kartingRM.Backend.Repositories.ReservationRepository;
import kartingRM.Backend.Repositories.RoomRepository;
import kartingRM.Backend.Repositories.TouristPackageRepository;
import kartingRM.Backend.Repositories.UserRepository;
import kartingRM.Backend.Services.ReservationService;
import kartingRM.Backend.Services.RoomService;
import kartingRM.Backend.Services.TouristPackageService;
import kartingRM.Backend.Services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DemoDataInitializerTest {

    @Test
    void initializeDemoData_givenEmptyRepositories_thenSeedsUsersPackagesAndReservations() throws Exception {
        RoomService roomService = mock(RoomService.class);
        UserService userService = mock(UserService.class);
        ReservationService reservationService = mock(ReservationService.class);
        TouristPackageService touristPackageService = mock(TouristPackageService.class);
        RoomRepository roomRepository = mock(RoomRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        TouristPackageRepository touristPackageRepository = mock(TouristPackageRepository.class);
        AtomicLong userIds = new AtomicLong(1L);

        when(userRepository.findByRut(any(String.class))).thenReturn(Optional.empty());
        when(userService.saveUser(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(userIds.getAndIncrement());
            return user;
        });
        when(touristPackageRepository.count()).thenReturn(0L);
        when(reservationRepository.count()).thenReturn(0L);
        when(roomRepository.findByRoomNumber("S001")).thenReturn(Optional.of(room(1L, "S001", "Simple")));
        when(roomRepository.findByRoomNumber("D001")).thenReturn(Optional.of(room(2L, "D001", "Double")));
        when(roomRepository.findByRoomNumber("SU001")).thenReturn(Optional.of(room(3L, "SU001", "Suite")));

        CommandLineRunner runner = new DemoDataInitializer().initializeDemoData(
                roomService,
                userService,
                reservationService,
                touristPackageService,
                roomRepository,
                userRepository,
                reservationRepository,
                touristPackageRepository
        );

        runner.run();

        verify(roomService).ensureDefaultInventory();
        verify(userService, times(6)).saveUser(any(UserEntity.class));
        verify(touristPackageService, times(2)).createPackage(any());
        verify(reservationService, times(3)).saveReservation(any());
    }

    @Test
    void initializeDemoData_givenExistingPackagesAndReservations_thenSkipsPackageAndReservationSeeds() throws Exception {
        RoomService roomService = mock(RoomService.class);
        UserService userService = mock(UserService.class);
        ReservationService reservationService = mock(ReservationService.class);
        TouristPackageService touristPackageService = mock(TouristPackageService.class);
        RoomRepository roomRepository = mock(RoomRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        TouristPackageRepository touristPackageRepository = mock(TouristPackageRepository.class);

        when(userRepository.findByRut(any(String.class))).thenReturn(Optional.of(user(99L, "11111111K")));
        when(touristPackageRepository.count()).thenReturn(2L);
        when(reservationRepository.count()).thenReturn(3L);

        CommandLineRunner runner = new DemoDataInitializer().initializeDemoData(
                roomService,
                userService,
                reservationService,
                touristPackageService,
                roomRepository,
                userRepository,
                reservationRepository,
                touristPackageRepository
        );

        runner.run();

        verify(roomService).ensureDefaultInventory();
        verify(touristPackageService, times(0)).createPackage(any());
        verify(reservationService, times(0)).saveReservation(any());
    }

    private RoomEntity room(Long id, String number, String type) {
        RoomEntity room = new RoomEntity();
        room.setId(id);
        room.setRoomNumber(number);
        room.setType(type);
        room.setStatus("AVAILABLE");
        return room;
    }

    private UserEntity user(Long id, String rut) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setRut(rut);
        user.setName("Demo User");
        return user;
    }
}
