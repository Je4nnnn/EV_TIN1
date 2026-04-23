package kartingRM.Backend.Config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component("authorizationRules")
public class AuthorizationRules {

    private final SecurityProperties securityProperties;

    public AuthorizationRules(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    public boolean hasAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String expectedAuthority = "ROLE_"
                + securityProperties.getAdminRole().replace('-', '_').toUpperCase(Locale.ROOT);

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(expectedAuthority::equals);
    }
}
