package kartingRM.Backend.DTOs;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SalesReportRow(
        LocalDateTime operationDate,
        String clientName,
        String packageName,
        Integer passengerCount,
        Double reservationTotal,
        Double paidAmount,
        String status,
        LocalDate travelStartDate
) {
}

