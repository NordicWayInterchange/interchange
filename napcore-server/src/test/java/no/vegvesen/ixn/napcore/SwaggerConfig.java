package no.vegvesen.ixn.napcore;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info
        (title = "Napcore API",
                version = "1.0",
                license = @License(url = "https://github.com/NordicWayInterchange/interchange/blob/federation-master/license.md", name = "MIT License"),
                description = "<a href=\"https://github.com/NordicWayInterchange\">Github</a>"))
public class SwaggerConfig {

    @Bean
    public OpenApiCustomizer openApiCustomizer(){
        return openApi -> {

        };
    }
}
