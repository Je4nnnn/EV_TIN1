package kartingRM.Backend.DTOs;

public record PackageRankingRow(
        Long packageId,
        String packageName,
        Long reservationsCount,
        Long passengerCount,
        Double totalAmount
) {
}

