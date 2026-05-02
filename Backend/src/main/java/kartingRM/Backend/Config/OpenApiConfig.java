package kartingRM.Backend.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI travelAgencyOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("TravelAgency API")
                        .description("Documentacion de la API para la plataforma web TravelAgency.")
                        .version("v1")
                        .contact(new Contact().name("TravelAgency Team"))
                        .license(new License().name("Uso academico")));
    }
}
