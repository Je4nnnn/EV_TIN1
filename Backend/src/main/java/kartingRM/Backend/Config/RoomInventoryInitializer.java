package kartingRM.Backend.Config;

import kartingRM.Backend.Services.RoomService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoomInventoryInitializer {

    @Bean
    CommandLineRunner initializeRoomInventory(RoomService roomService) {
        return args -> roomService.ensureDefaultInventory();
    }
}
