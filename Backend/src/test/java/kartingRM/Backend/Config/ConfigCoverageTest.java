package kartingRM.Backend.Config;

import io.swagger.v3.oas.models.OpenAPI;
import kartingRM.Backend.Services.RoomService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.CommandLineRunner;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

class ConfigCoverageTest {

    @Test
    void openApiConfig_buildsExpectedMetadata() {
        OpenAPI openAPI = new OpenApiConfig().travelAgencyOpenApi();

        assertEquals("TravelAgency API", openAPI.getInfo().getTitle());
        assertEquals("v1", openAPI.getInfo().getVersion());
        assertEquals("TravelAgency Team", openAPI.getInfo().getContact().getName());
    }

    @Test
    void securityProperties_exposeDefaultsAndAllowOverrides() {
        SecurityProperties properties = new SecurityProperties();

        assertTrue(properties.isEnabled());
        assertEquals("hotelrm_admin", properties.getAdminRole());
        assertEquals("hotelrm", properties.getKeycloak().getRealm());
        assertTrue(properties.getCors().getAllowedOrigins().contains("http://localhost:5173"));

        properties.setEnabled(false);
        properties.setAdminRole("admin");
        properties.getKeycloak().setRealm("demo");
        properties.getKeycloak().setAuthServerUrl("http://localhost:9090");
        properties.getKeycloak().setClientId("demo-frontend");
        properties.getCors().setAllowedOrigins(List.of("http://localhost:3001"));

        assertFalse(properties.isEnabled());
        assertEquals("admin", properties.getAdminRole());
        assertEquals("demo", properties.getKeycloak().getRealm());
        assertEquals("http://localhost:9090", properties.getKeycloak().getAuthServerUrl());
        assertEquals("demo-frontend", properties.getKeycloak().getClientId());
        assertEquals(List.of("http://localhost:3001"), properties.getCors().getAllowedOrigins());
    }

    @Test
    void roomInventoryInitializer_executesRoomInitialization() throws Exception {
        RoomService roomService = Mockito.mock(RoomService.class);
        CommandLineRunner runner = new RoomInventoryInitializer().initializeRoomInventory(roomService);

        runner.run();

        verify(roomService).ensureDefaultInventory();
    }

    @Test
    void securityConfig_buildsCorsConfigurationFromProperties() {
        SecurityProperties properties = new SecurityProperties();
        properties.getCors().setAllowedOrigins(List.of("http://localhost:3001", "http://localhost:5173"));

        CorsConfigurationSource source = new SecurityConfig().corsConfigurationSource(properties);
        CorsConfiguration configuration = ((UrlBasedCorsConfigurationSource) source)
                .getCorsConfiguration(new org.springframework.mock.web.MockHttpServletRequest());

        assertNotNull(configuration);
        assertEquals(List.of("http://localhost:3001", "http://localhost:5173"), configuration.getAllowedOrigins());
        assertTrue(configuration.getAllowedMethods().contains("PATCH"));
        assertEquals(List.of("Authorization"), configuration.getExposedHeaders());
    }
}
