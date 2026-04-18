package kartingRM.Backend.Security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class KeycloakJwtRolesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final JwtGrantedAuthoritiesConverter defaultAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    private final String clientId;

    public KeycloakJwtRolesConverter(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>(defaultAuthoritiesConverter.convert(jwt));
        extractRealmRoles(jwt).forEach(role -> authorities.add(toAuthority(role)));
        extractClientRoles(jwt).forEach(role -> authorities.add(toAuthority(role)));
        return authorities;
    }

    private List<String> extractRealmRoles(Jwt jwt) {
        Object realmAccess = jwt.getClaims().get("realm_access");
        if (!(realmAccess instanceof Map<?, ?> realmAccessMap)) {
            return List.of();
        }

        Object roles = realmAccessMap.get("roles");
        if (!(roles instanceof Collection<?> values)) {
            return List.of();
        }

        return values.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .toList();
    }

    private List<String> extractClientRoles(Jwt jwt) {
        Object resourceAccess = jwt.getClaims().get("resource_access");
        if (!(resourceAccess instanceof Map<?, ?> resourceAccessMap) || resourceAccessMap.isEmpty()) {
            return List.of();
        }

        if (clientId != null && !clientId.isBlank()) {
            return extractRolesFromResource(resourceAccessMap.get(clientId));
        }

        return resourceAccessMap.values().stream()
                .flatMap(resource -> extractRolesFromResource(resource).stream())
                .distinct()
                .toList();
    }

    private List<String> extractRolesFromResource(Object resource) {
        if (!(resource instanceof Map<?, ?> resourceMap)) {
            return List.of();
        }

        Object roles = resourceMap.get("roles");
        if (!(roles instanceof Collection<?> values)) {
            return List.of();
        }

        return values.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .toList();
    }

    private GrantedAuthority toAuthority(String role) {
        String normalizedRole = role.replace('-', '_').toUpperCase(Locale.ROOT);
        return new SimpleGrantedAuthority("ROLE_" + normalizedRole);
    }
}
