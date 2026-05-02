package kartingRM.Backend.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TouristPackageControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private TouristPackageService touristPackageService;

    @InjectMocks
    private TouristPackageController touristPackageController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(touristPackageController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAllPackages_returnsAllPackages() throws Exception {
        when(touristPackageService.getAllPackages()).thenReturn(List.of(touristPackage(1L, "Patagonia")));

        mockMvc.perform(get("/api/v1/tourist-packages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].packageName").value("Patagonia"));
    }

    @Test
    void getAllPackages_availableOnlyUsesAvailableService() throws Exception {
        when(touristPackageService.getAvailablePackages()).thenReturn(List.of(touristPackage(2L, "Atacama")));

        mockMvc.perform(get("/api/v1/tourist-packages").param("availableOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].packageName").value("Atacama"));
    }

    @Test
    void getPackageById_returnsPackage() throws Exception {
        when(touristPackageService.getPackageById(3L)).thenReturn(touristPackage(3L, "Lagos del Sur"));

        mockMvc.perform(get("/api/v1/tourist-packages/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void searchPackages_returnsFilteredPackages() throws Exception {
        when(touristPackageService.searchAvailablePackages(
                eq("Atacama"),
                eq(LocalDate.of(2026, 6, 1)),
                eq(LocalDate.of(2026, 6, 30)),
                eq(100000.0),
                eq(500000.0),
                eq(2),
                eq(5),
                eq("AVENTURA")
        )).thenReturn(List.of(touristPackage(8L, "Atacama aventura")));

        mockMvc.perform(get("/api/v1/tourist-packages/search")
                        .param("destination", "Atacama")
                        .param("startDate", "2026-06-01")
                        .param("endDate", "2026-06-30")
                        .param("minPrice", "100000")
                        .param("maxPrice", "500000")
                        .param("minDays", "2")
                        .param("maxDays", "5")
                        .param("travelType", "AVENTURA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].packageName").value("Atacama aventura"));
    }

    @Test
    void createPackage_returnsSavedPackage() throws Exception {
        when(touristPackageService.createPackage(any(TouristPackageEntity.class))).thenReturn(touristPackage(4L, "Aventura"));

        mockMvc.perform(post("/api/v1/tourist-packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(touristPackage(null, "Aventura"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    void updatePackage_returnsUpdatedPackage() throws Exception {
        when(touristPackageService.updatePackage(eq(5L), any(TouristPackageEntity.class))).thenReturn(touristPackage(5L, "Ruta del Vino"));

        mockMvc.perform(put("/api/v1/tourist-packages/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(touristPackage(null, "Ruta del Vino"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.packageName").value("Ruta del Vino"));
    }

    @Test
    void updateAvailability_returnsUpdatedAvailability() throws Exception {
        TouristPackageEntity result = touristPackage(6L, "Costa");
        result.setAvailable(false);
        when(touristPackageService.updateAvailability(6L, false)).thenReturn(result);

        mockMvc.perform(patch("/api/v1/tourist-packages/6/availability").param("available", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void deletePackage_returnsConfirmationMessage() throws Exception {
        doNothing().when(touristPackageService).deletePackage(7L);

        mockMvc.perform(delete("/api/v1/tourist-packages/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Paquete turistico eliminado correctamente."));
    }

    private TouristPackageEntity touristPackage(Long id, String name) {
        TouristPackageEntity touristPackage = new TouristPackageEntity();
        touristPackage.setId(id);
        touristPackage.setPackageName(name);
        touristPackage.setDescription("Descripcion");
        touristPackage.setDestinations(List.of("Destino"));
        touristPackage.setActivities(List.of("Actividad"));
        touristPackage.setExtraServices(List.of("Seguro"));
        touristPackage.setDaysCount(3);
        touristPackage.setNightsCount(2);
        touristPackage.setRoomType("Suite");
        touristPackage.setTransferIncluded(true);
        touristPackage.setAutomobileServiceIncluded(false);
        touristPackage.setPrice(199000.0);
        touristPackage.setAvailableSlots(5);
        touristPackage.setStatus("AVAILABLE");
        touristPackage.setAvailable(true);
        touristPackage.setMaxGuests(4);
        touristPackage.setAvailableFrom(LocalDate.of(2026, 4, 1));
        touristPackage.setAvailableUntil(LocalDate.of(2026, 5, 1));
        return touristPackage;
    }
}
