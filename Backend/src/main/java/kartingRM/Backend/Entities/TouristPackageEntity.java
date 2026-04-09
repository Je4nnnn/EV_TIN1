package kartingRM.Backend.Entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tourist_packages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TouristPackageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String packageName;

    @Column(nullable = false, length = 2000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "tourist_package_destinations", joinColumns = @JoinColumn(name = "package_id"))
    @Column(name = "destination_name", nullable = false)
    private List<String> destinations = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "tourist_package_activities", joinColumns = @JoinColumn(name = "package_id"))
    @Column(name = "activity_name", nullable = false)
    private List<String> activities = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "tourist_package_extra_services", joinColumns = @JoinColumn(name = "package_id"))
    @Column(name = "service_name", nullable = false)
    private List<String> extraServices = new ArrayList<>();

    @Column(nullable = false)
    private Integer daysCount;

    @Column(nullable = false)
    private Integer nightsCount;

    @Column(nullable = false, length = 30)
    private String roomType;

    @Column(nullable = false)
    private Boolean transferIncluded;

    @Column(nullable = false)
    private Boolean automobileServiceIncluded;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer availableSlots;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(nullable = false)
    private Boolean available;

    @Column(nullable = false)
    private Integer maxGuests;

    private LocalDate availableFrom;

    private LocalDate availableUntil;
}
