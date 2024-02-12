package no.vegvesen.ixn.federation.server;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import no.vegvesen.ixn.federation.api.v1_0.EndpointApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionPollResponseApi;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Neighbour API",
        version = "1.0",
        license = @License(url = "https://github.com/NordicWayInterchange/interchange/blob/federation-master/license.md", name = "MIT License"),
        description = "https://github.com/NordicWayInterchange"))
public class SwaggerConfig {
    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            Map<String, Schema> schemas = openApi.getComponents().getSchemas();

            ComposedSchema application = (ComposedSchema) schemas.get("CapabilitySplitApi").getProperties().get("application");
            application.getOneOf().remove(0);

            openApi.getComponents().addSchemas("SubscriptionPollResponseApi", ModelConverters.getInstance().readAllAsResolvedSchema(SubscriptionPollResponseApi.class).schema);
            openApi.getComponents().addSchemas("EndpointApi", ModelConverters.getInstance().readAllAsResolvedSchema(EndpointApi.class).schema);

            schemas.get("RequestedSubscriptionApi").setRequired(List.of("selector"));

        };
    }
}
