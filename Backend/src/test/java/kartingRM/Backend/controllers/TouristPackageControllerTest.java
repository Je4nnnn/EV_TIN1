package kartingRM.Backend.controllers;

import kartingRM.Backend.Controllers.TouristPackageController;
import kartingRM.Backend.Entities.TouristPackageEntity;
import kartingRM.Backend.Exceptions.GlobalExceptionHandler;
import kartingRM.Backend.Services.TouristPackageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TouristPackageControllerTest {

    @Mock
    private TouristPackageService touristPackageService;

    @InjectMocks
    private TouristPackageController touristPackageController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(touristPackageController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAllPackages_returnsPackageList() throws Exception {
        when(touristPackageService.getAllPackages()).thenReturn(List.of(buildPackage(1L, "Patagonia")));

        mockMvc.perform(get("/api/v1/tourist-packages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].packageName").value("Patagonia"));
    }

    @Test
    void createPackage_returnsCreatedPackage() throws Exception {
        when(touristPackageService.createPackage(any(TouristPackageEntity.class))).thenReturn(buildPackage(2L, "Lagos"));

        mockMvc.perform(post("/api/v1/tourist-packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "packageName": "Lagos",
                                  "description": "Circuito sur",
                                  "daysCount": 3,
                                  "nightsCount": 2,
                                  "roomType": "Suite",
                                  "transferIncluded": true,
                                  "automobileServiceIncluded": false,
                                  "price": 300000.0,
                                  "availableSlots": 6,
                                  "status": "AVAILABLE",
                                  "available": true,
                                  "maxGuests": 4
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.packageName").value("Lagos"));
    }

    private TouristPackageEntity buildPackage(Long id, String name) {
        TouristPackageEntity touristPackage = new TouristPackageEntity();
        touristPackage.setId(id);
        touristPackage.setPackageName(name);
        touristPackage.setDescription("Descripcion");
        touristPackage.setDaysCount(3);
        touristPackage.setNightsCount(2);
        touristPackage.setRoomType("Suite");
        touristPackage.setTransferIncluded(true);
        touristPackage.setAutomobileServiceIncluded(false);
        touristPackage.setPrice(300000.0);
        touristPackage.setAvailableSlots(6);
        touristPackage.setStatus("AVAILABLE");
        touristPackage.setAvailable(true);
        touristPackage.setMaxGuests(4);
        return touristPackage;
    }
}
