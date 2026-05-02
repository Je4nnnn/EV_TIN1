package kartingRM.Backend.Config;

import kartingRM.Backend.Entities.ReservationDetailsEntity;
import kartingRM.Backend.Entities.ReservationEntity;
import kartingRM.Backend.Entities.RoomEntity;
import kartingRM.Backend.Entities.TouristPackageEntity;
import kartingRM.Backend.Entities.UserEntity;
import kartingRM.Backend.Repositories.ReservationRepository;
import kartingRM.Backend.Repositories.RoomRepository;
import kartingRM.Backend.Repositories.TouristPackageRepository;
import kartingRM.Backend.Repositories.UserRepository;
import kartingRM.Backend.Services.ReservationService;
import kartingRM.Backend.Services.RoomService;
import kartingRM.Backend.Services.TouristPackageService;
import kartingRM.Backend.Services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConditionalOnProperty(name = "app.demo.seed.enabled", havingValue = "true")
public class DemoDataInitializer {

    @Bean
    CommandLineRunner initializeDemoData(
            RoomService roomService,
            UserService userService,
            ReservationService reservationService,
            TouristPackageService touristPackageService,
            RoomRepository roomRepository,
            UserRepository userRepository,
            ReservationRepository reservationRepository,
            TouristPackageRepository touristPackageRepository
    ) {
        return args -> {
            roomService.ensureDefaultInventory();

            Map<String, UserEntity> demoUsers = ensureDemoUsers(userService, userRepository);
            ensureDemoPackages(touristPackageService, touristPackageRepository);
            ensureDemoReservations(reservationService, roomRepository, reservationRepository, demoUsers);
        };
    }

    private Map<String, UserEntity> ensureDemoUsers(UserService userService, UserRepository userRepository) {
        List<UserEntity> defaultUsers = List.of(
                buildUser("11111111K", "Camila Herrera", "camila@travelagency.cl", "+56911111111", LocalDate.of(1994, 6, 12)),
                buildUser("22222222K", "Diego Mella", "diego@travelagency.cl", "+56922222222", LocalDate.of(1989, 9, 3)),
                buildUser("33333333K", "Fernanda Soto", "fernanda@travelagency.cl", "+56933333333", LocalDate.of(1996, 1, 20)),
                buildUser("44444444K", "Matias Rojas", "matias@travelagency.cl", "+56944444444", LocalDate.of(1992, 11, 8)),
                buildUser("55555555K", "Paula Diaz", "paula@travelagency.cl", "+56955555555", LocalDate.of(1987, 4, 18)),
                buildUser("66666666K", "Javiera Pino", "javiera@travelagency.cl", "+56966666666", LocalDate.of(1998, 7, 27))
        );

        Map<String, UserEntity> demoUsers = new LinkedHashMap<>();
        for (UserEntity candidate : defaultUsers) {
            UserEntity persistedUser = userRepository.findByRut(candidate.getRut())
                    .orElseGet(() -> userService.saveUser(candidate));
            demoUsers.put(persistedUser.getRut(), persistedUser);
        }

        return demoUsers;
    }

    private void ensureDemoPackages(
            TouristPackageService touristPackageService,
            TouristPackageRepository touristPackageRepository
    ) {
        if (touristPackageRepository.count() > 0) {
            return;
        }

        LocalDate today = LocalDate.now();

        touristPackageService.createPackage(buildPackage(
                "Escapada costera",
                "Plan de fin de semana con traslado, desayuno y actividades suaves para parejas o viajeros solos.",
                List.of("Valparaiso", "Vina del Mar"),
                List.of("City tour", "Cena de bienvenida"),
                List.of("Desayuno buffet", "Late check-out"),
                3,
                2,
                "Double",
                true,
                false,
                189900.0,
                6,
                2,
                today.plusDays(10),
                today.plusDays(12)
        ));

        touristPackageService.createPackage(buildPackage(
                "Ruta termal premium",
                "Experiencia de descanso con suite, traslado y actividades de spa para grupos pequenos.",
                List.of("Termas de Chillan"),
                List.of("Circuito spa", "Tour gastronomico"),
                List.of("Traslado aeropuerto", "Masaje de relajacion"),
                4,
                3,
                "Suite",
                true,
                true,
                349900.0,
                4,
                4,
                today.plusDays(20),
                today.plusDays(23)
        ));
    }

    private void ensureDemoReservations(
            ReservationService reservationService,
            RoomRepository roomRepository,
            ReservationRepository reservationRepository,
            Map<String, UserEntity> demoUsers
    ) {
        if (reservationRepository.count() > 0) {
            return;
        }

        LocalDate today = LocalDate.now();

        reservationService.saveReservation(buildReservation(
                today,
                today.plusDays(1),
                "Noche",
                requireRoom(roomRepository, "S001"),
                demoUsers.get("11111111K"),
                List.of(demoUsers.get("11111111K"))
        ));

        reservationService.saveReservation(buildReservation(
                today.plusDays(2),
                today.plusDays(4),
                "Completo",
                requireRoom(roomRepository, "D001"),
                demoUsers.get("22222222K"),
                List.of(
                        demoUsers.get("22222222K"),
                        demoUsers.get("33333333K"),
                        demoUsers.get("44444444K")
                )
        ));

        reservationService.saveReservation(buildReservation(
                today.plusDays(5),
                today.plusDays(6),
                "Noche",
                requireRoom(roomRepository, "SU001"),
                demoUsers.get("55555555K"),
                List.of(
                        demoUsers.get("55555555K"),
                        demoUsers.get("66666666K")
                )
        ));
    }

    private UserEntity buildUser(String rut, String name, String email, String phoneNumber, LocalDate dateBirthday) {
        UserEntity user = new UserEntity();
        user.setRut(rut);
        user.setName(name);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setDateBirthday(dateBirthday);
        return user;
    }

    private TouristPackageEntity buildPackage(
            String packageName,
            String description,
            List<String> destinations,
            List<String> activities,
            List<String> extraServices,
            int daysCount,
            int nightsCount,
            String roomType,
            boolean transferIncluded,
            boolean automobileServiceIncluded,
            double price,
            int availableSlots,
            int maxGuests,
            LocalDate availableFrom,
            LocalDate availableUntil
    ) {
        TouristPackageEntity touristPackage = new TouristPackageEntity();
        touristPackage.setPackageName(packageName);
        touristPackage.setDescription(description);
        touristPackage.setDestinations(destinations);
        touristPackage.setActivities(activities);
        touristPackage.setExtraServices(extraServices);
        touristPackage.setDaysCount(daysCount);
        touristPackage.setNightsCount(nightsCount);
        touristPackage.setRoomType(roomType);
        touristPackage.setTransferIncluded(transferIncluded);
        touristPackage.setAutomobileServiceIncluded(automobileServiceIncluded);
        touristPackage.setPrice(price);
        touristPackage.setAvailableSlots(availableSlots);
        touristPackage.setStatus("AVAILABLE");
        touristPackage.setAvailable(true);
        touristPackage.setMaxGuests(maxGuests);
        touristPackage.setAvailableFrom(availableFrom);
        touristPackage.setAvailableUntil(availableUntil);
        return touristPackage;
    }

    private ReservationEntity buildReservation(
            LocalDate checkInDate,
            LocalDate checkOutDate,
            String stayType,
            RoomEntity room,
            UserEntity mainClient,
            List<UserEntity> guests
    ) {
        ReservationEntity reservation = new ReservationEntity();
        reservation.setCheckInDate(checkInDate);
        reservation.setCheckOutDate(checkOutDate);
        reservation.setStayType(stayType);
        reservation.setRoomType(room.getType());
        reservation.setRoomId(room.getId());
        reservation.setCliente(mainClient);
        reservation.setDetails(guests.stream()
                .map(this::buildReservationDetail)
                .toList());
        return reservation;
    }

    private ReservationDetailsEntity buildReservationDetail(UserEntity guest) {
        ReservationDetailsEntity detail = new ReservationDetailsEntity();
        detail.setGuestName(guest.getName());
        detail.setUserId(guest.getId());
        return detail;
    }

    private RoomEntity requireRoom(RoomRepository roomRepository, String roomNumber) {
        return roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new IllegalStateException("No se encontro la habitacion demo " + roomNumber + "."));
    }
}
