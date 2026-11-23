package no.vegvesen.ixn.napcore;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:swagger-version.properties")
@OpenAPIDefinition(info = @Info
        (title = "Napcore API",
                version = "${swagger.version}",
                license = @License(url = "https://github.com/NordicWayInterchange/interchange/blob/federation-master/license.md", name = "MIT License"),
                description = "<a href=\"https://github.com/NordicWayInterchange\">Github</a>"))
public class SwaggerConfig {

    @Bean
    public OpenApiCustomizer openApiCustomizer(){
        return openApi -> {

        };
    }
}
