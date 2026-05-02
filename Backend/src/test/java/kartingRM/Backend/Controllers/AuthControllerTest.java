package kartingRM.Backend.Controllers;

import kartingRM.Backend.DTOs.AuthenticatedUserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthControllerTest {

    private final AuthController authController = new AuthController();

    @Test
    void me_returnsAuthenticatedUserResponse() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("subject-1")
                .claim("preferred_username", "hotelrm-admin")
                .claim("name", "HotelRM Admin")
                .claim("email", "admin@hotelrm.test")
                .build();

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, List.of(
                new SimpleGrantedAuthority("ROLE_HOTELRM_ADMIN"),
                new SimpleGrantedAuthority("SCOPE_profile")
        ));

        ResponseEntity<AuthenticatedUserResponse> response = authController.me(authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("subject-1", response.getBody().subject());
        assertEquals("hotelrm-admin", response.getBody().username());
        assertEquals(List.of("ROLE_HOTELRM_ADMIN", "SCOPE_profile"), response.getBody().authorities());
    }
}
