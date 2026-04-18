package kartingRM.Backend.DTOs;

import java.util.List;

public record AuthenticatedUserResponse(
        String subject,
        String username,
        String name,
        String email,
        List<String> authorities
) {
}
