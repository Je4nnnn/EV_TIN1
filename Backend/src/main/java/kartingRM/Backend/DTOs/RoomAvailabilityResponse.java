package kartingRM.Backend.DTOs;

import java.time.LocalDate;

public record RoomAvailabilityResponse(
        Long id,
        String roomNumber,
        String type,
        String operationalStatus,
        String occupancyStatus,
        String reservationCode,
        LocalDate reservedFrom,
        LocalDate reservedUntil
) {
}
