package no.vegvesen.ixn.serviceprovider;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info=@Info
                (title = "Onboard API",
                description = "Description...",
                version = "1.0"))
public class SwaggerConfig {

}
