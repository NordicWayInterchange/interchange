package no.vegvesen.ixn.federation.server;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info=@Info(title = "Neighbour API",
        version = "1.0",
        license = @License(url = "https://github.com/NordicWayInterchange/interchange/blob/federation-master/license.md", name = "MIT License"),
        description = "https://github.com/NordicWayInterchange"))
public class SwaggerConfig {

}
