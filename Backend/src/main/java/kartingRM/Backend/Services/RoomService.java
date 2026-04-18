package kartingRM.Backend.Services;

import kartingRM.Backend.DTOs.RoomAvailabilityResponse;
import kartingRM.Backend.Entities.RoomEntity;
import kartingRM.Backend.Entities.ReservationEntity;
import kartingRM.Backend.Exceptions.BusinessException;
import kartingRM.Backend.Exceptions.ResourceNotFoundException;
import kartingRM.Backend.Repositories.ReservationRepository;
import kartingRM.Backend.Repositories.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Collections;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private static final int CLEANING_BUFFER_HOURS = 2;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    public List<RoomEntity> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<RoomAvailabilityResponse> getRoomAvailabilityOverview(LocalDate referenceDate) {
        LocalDate targetDate = referenceDate != null ? referenceDate : LocalDate.now();
        List<RoomEntity> rooms = roomRepository.findAll();
        Map<Long, List<ReservationEntity>> reservationsByRoomId = getReservationsByRoomId(rooms);

        return rooms.stream()
                .sorted(Comparator.comparing(RoomEntity::getRoomNumber, String.CASE_INSENSITIVE_ORDER))
                .map(room -> buildRoomAvailability(room, targetDate, reservationsByRoomId.getOrDefault(room.getId(), Collections.emptyList())))
                .toList();
    }

    public RoomEntity getRoomById(long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habitacion no encontrada con ID: " + id));
    }

    public RoomEntity saveRoom(RoomEntity room) {
        validateRoom(room);

        roomRepository.findByRoomNumber(room.getRoomNumber())
                .ifPresent(existing -> {
                    throw new BusinessException("Ya existe una habitacion con numero " + room.getRoomNumber() + ".");
                });

        return roomRepository.save(room);
    }

    public List<RoomEntity> getAvailableRooms(
            LocalDate checkInDate,
            LocalDate checkOutDate,
            String roomType,
            String stayType
    ) {
        // CRITICO: define disponibilidad real de habitaciones y evita solapamientos de reservas.
        if (checkInDate == null || checkOutDate == null) {
            throw new BusinessException("Debe indicar check-in y check-out para consultar disponibilidad.");
        }

        if (checkOutDate.isBefore(checkInDate)) {
            throw new BusinessException("La fecha de check-out no puede ser anterior al check-in.");
        }

        if (roomType == null || roomType.isBlank()) {
            throw new BusinessException("Debe indicar el tipo de habitacion.");
        }

        if (stayType == null || stayType.isBlank()) {
            throw new BusinessException("Debe indicar el tipo de estancia.");
        }

        List<RoomEntity> roomsByType = roomRepository.findByTypeIgnoreCaseOrderByRoomNumberAsc(roomType);
        List<RoomEntity> availableRooms = new ArrayList<>();
        Map<Long, List<ReservationEntity>> reservationsByRoomId = getReservationsByRoomId(roomsByType);

        for (RoomEntity room : roomsByType) {
            if (!isRoomOperational(room)) {
                continue;
            }

            boolean overlaps = reservationsByRoomId.getOrDefault(room.getId(), Collections.emptyList()).stream()
                    .anyMatch(reservation -> overlaps(reservation, checkInDate, checkOutDate, stayType));

            if (!overlaps) {
                availableRooms.add(room);
            }
        }

        return availableRooms;
    }

    public RoomEntity getAvailableRoomById(
            Long roomId,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            String roomType,
            String stayType
    ) {
        RoomEntity room = getRoomById(roomId);

        if (!room.getType().equalsIgnoreCase(roomType)) {
            throw new BusinessException("La habitacion seleccionada no corresponde al tipo solicitado.");
        }

        boolean available = getAvailableRooms(checkInDate, checkOutDate, roomType, stayType).stream()
                .anyMatch(candidate -> candidate.getId().equals(roomId));

        if (!available) {
            throw new BusinessException("La habitacion seleccionada ya no esta disponible para ese rango.");
        }

        return room;
    }

    public void ensureDefaultInventory() {
        ensureInventoryForType("Simple", "S", 50);
        ensureInventoryForType("Double", "D", 50);
        ensureInventoryForType("Suite", "SU", 10);
    }

    private void ensureInventoryForType(String roomType, String prefix, int totalRequired) {
        List<RoomEntity> existingRooms = roomRepository.findByTypeIgnoreCaseOrderByRoomNumberAsc(roomType);
        int currentCount = existingRooms.size();

        for (int index = currentCount + 1; index <= totalRequired; index++) {
            RoomEntity room = new RoomEntity();
            room.setRoomNumber(prefix + String.format(Locale.ROOT, "%03d", index));
            room.setType(roomType);
            room.setStatus("AVAILABLE");
            roomRepository.save(room);
        }
    }

    private boolean isRoomOperational(RoomEntity room) {
        return room.getStatus() != null && !room.getStatus().equalsIgnoreCase("OUT_OF_SERVICE");
    }

    private boolean overlaps(
            ReservationEntity reservation,
            LocalDate requestedCheckIn,
            LocalDate requestedCheckOut,
            String requestedStayType
    ) {
        if (reservation.getCheckInDate() == null || reservation.getCheckOutDate() == null || reservation.getStayType() == null) {
            return false;
        }

        LocalDateTime requestedStart = resolveReservationStart(requestedCheckIn, requestedStayType);
        LocalDateTime requestedEnd = resolveReservationEnd(requestedCheckOut, requestedStayType)
                .plusHours(CLEANING_BUFFER_HOURS);

        LocalDateTime existingStart = resolveReservationStart(reservation.getCheckInDate(), reservation.getStayType());
        LocalDateTime existingEnd = resolveReservationEnd(reservation.getCheckOutDate(), reservation.getStayType())
                .plusHours(CLEANING_BUFFER_HOURS);

        return requestedStart.isBefore(existingEnd) && requestedEnd.isAfter(existingStart);
    }

    private LocalDateTime resolveReservationStart(LocalDate date, String stayType) {
        String normalizedStayType = normalizeStayType(stayType);

        return switch (normalizedStayType) {
            case "manana" -> LocalDateTime.of(date, LocalTime.of(8, 30));
            case "noche" -> LocalDateTime.of(date, LocalTime.of(18, 30));
            case "completo" -> LocalDateTime.of(date, LocalTime.of(8, 30));
            default -> LocalDateTime.of(date, LocalTime.of(12, 0));
        };
    }

    private LocalDateTime resolveReservationEnd(LocalDate date, String stayType) {
        String normalizedStayType = normalizeStayType(stayType);

        return switch (normalizedStayType) {
            case "manana" -> LocalDateTime.of(date, LocalTime.of(18, 30));
            case "noche" -> LocalDateTime.of(date, LocalTime.of(8, 30));
            case "completo" -> LocalDateTime.of(date, LocalTime.of(8, 30));
            default -> LocalDateTime.of(date, LocalTime.of(12, 0));
        };
    }

    private String normalizeStayType(String stayType) {
        if (stayType == null) {
            return "";
        }

        return Normalizer.normalize(stayType, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private RoomAvailabilityResponse buildRoomAvailability(
            RoomEntity room,
            LocalDate targetDate,
            List<ReservationEntity> roomReservations
    ) {
        Optional<ReservationEntity> currentReservation = roomReservations.stream()
                .filter(reservation -> reservation.getCheckInDate() != null && reservation.getCheckOutDate() != null)
                .filter(reservation -> !targetDate.isBefore(reservation.getCheckInDate())
                        && !targetDate.isAfter(reservation.getCheckOutDate()))
                .min(Comparator.comparing(ReservationEntity::getCheckInDate));

        if (currentReservation.isPresent()) {
            ReservationEntity reservation = currentReservation.get();
            return new RoomAvailabilityResponse(
                    room.getId(),
                    room.getRoomNumber(),
                    room.getType(),
                    room.getStatus(),
                    "OCCUPIED",
                    reservation.getReservationCode(),
                    reservation.getCheckInDate(),
                    reservation.getCheckOutDate()
            );
        }

        Optional<ReservationEntity> nextReservation = roomReservations.stream()
                .filter(reservation -> reservation.getCheckInDate() != null)
                .filter(reservation -> reservation.getCheckInDate().isAfter(targetDate))
                .min(Comparator.comparing(ReservationEntity::getCheckInDate));

        if (nextReservation.isPresent()) {
            ReservationEntity reservation = nextReservation.get();
            return new RoomAvailabilityResponse(
                    room.getId(),
                    room.getRoomNumber(),
                    room.getType(),
                    room.getStatus(),
                    "RESERVED",
                    reservation.getReservationCode(),
                    reservation.getCheckInDate(),
                    reservation.getCheckOutDate()
            );
        }

        return new RoomAvailabilityResponse(
                room.getId(),
                room.getRoomNumber(),
                room.getType(),
                room.getStatus(),
                "AVAILABLE",
                null,
                null,
                null
        );
    }

    private Map<Long, List<ReservationEntity>> getReservationsByRoomId(List<RoomEntity> rooms) {
        List<Long> roomIds = rooms.stream()
                .map(RoomEntity::getId)
                .filter(id -> id != null)
                .toList();

        if (roomIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return reservationRepository.findByRoomIdInAndCancelledFalse(roomIds).stream()
                .filter(reservation -> reservation.getRoomId() != null)
                .collect(Collectors.groupingBy(ReservationEntity::getRoomId, Collectors.mapping(Function.identity(), Collectors.toList())));
    }

    private void validateRoom(RoomEntity room) {
        if (room == null) {
            throw new BusinessException("Debe enviar una habitacion valida.");
        }

        if (room.getRoomNumber() == null || room.getRoomNumber().isBlank()) {
            throw new BusinessException("El numero de habitacion es obligatorio.");
        }

        if (room.getType() == null || room.getType().isBlank()) {
            throw new BusinessException("El tipo de habitacion es obligatorio.");
        }

        if (room.getStatus() == null || room.getStatus().isBlank()) {
            throw new BusinessException("El estado de la habitacion es obligatorio.");
        }

        room.setRoomNumber(room.getRoomNumber().trim());
        room.setType(room.getType().trim());
        room.setStatus(room.getStatus().trim());
    }
}
