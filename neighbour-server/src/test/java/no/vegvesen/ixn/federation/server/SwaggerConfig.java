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
        contact = @Contact(name = "", email = "email@email.no"),
        license = @License(url = "https://www.apache.org/licenses/LICENSE-2.0", name = "Apache 2.0"),
        description = "Description????"))
public class SwaggerConfig {

}
