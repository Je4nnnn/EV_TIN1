package kartingRM.Backend.repositories;

import kartingRM.Backend.Entities.TouristPackageEntity;
import kartingRM.Backend.Repositories.TouristPackageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TouristPackageRepositoryTest {

    @Autowired
    private TouristPackageRepository touristPackageRepository;

    @Test
    void findByAvailableTrueOrderByPackageNameAsc_returnsOnlyAvailablePackagesOrdered() {
        touristPackageRepository.save(buildPackage("Zafiro", true, "AVAILABLE"));
        touristPackageRepository.save(buildPackage("Andes", true, "AVAILABLE"));
        touristPackageRepository.save(buildPackage("Bosque", false, "UNAVAILABLE"));

        List<TouristPackageEntity> found = touristPackageRepository.findByAvailableTrueOrderByPackageNameAsc();

        assertThat(found).hasSize(2);
        assertThat(found).extracting(TouristPackageEntity::getPackageName).containsExactly("Andes", "Zafiro");
    }

    private TouristPackageEntity buildPackage(String name, boolean available, String status) {
        TouristPackageEntity touristPackage = new TouristPackageEntity();
        touristPackage.setPackageName(name);
        touristPackage.setDescription("Descripcion");
        touristPackage.setDaysCount(3);
        touristPackage.setNightsCount(2);
        touristPackage.setRoomType("Suite");
        touristPackage.setTransferIncluded(true);
        touristPackage.setAutomobileServiceIncluded(false);
        touristPackage.setPrice(250000.0);
        touristPackage.setAvailableSlots(5);
        touristPackage.setStatus(status);
        touristPackage.setAvailable(available);
        touristPackage.setMaxGuests(4);
        return touristPackage;
    }
}
