package kartingRM.backend.security;

import kartingRM.Backend.Security.KeycloakJwtRolesConverter;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class KeycloakJwtRolesConverterTest {

    @Test
    void convert_givenRealmAndClientRoles_whenConverted_thenMapsThemToSpringAuthorities() {
        Jwt jwt = new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "none"),
                Map.of(
                        "sub", "123",
                        "scope", "profile email",
                        "realm_access", Map.of("roles", java.util.List.of("hotelrm_admin")),
                        "resource_access", Map.of(
                                "hotelrm-frontend", Map.of("roles", java.util.List.of("hotelrm_manager"))
                        )
                )
        );

        Collection<GrantedAuthority> authorities = new KeycloakJwtRolesConverter("hotelrm-frontend").convert(jwt);

        assertTrue(authorities.stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_HOTELRM_ADMIN")));
        assertTrue(authorities.stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_HOTELRM_MANAGER")));
        assertTrue(authorities.stream().anyMatch(authority -> authority.getAuthority().equals("SCOPE_profile")));
    }
}
