package no.vegvesen.ixn.serviceprovider;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import no.vegvesen.ixn.serviceprovider.model.*;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@OpenAPIDefinition(info = @Info
        (title = "Onboard API",
                version = "1.0",
                license = @License(url = "https://github.com/NordicWayInterchange/interchange/blob/federation-master/license.md", name = "MIT License"),
                description = "<a href=\"https://github.com/NordicWayInterchange\">Github</a>"))
public class SwaggerConfig {
    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {

            Map<String, Schema> schemas = openApi.getComponents().getSchemas();

            // To remove 'ApplicationApi' from 'oneOf' in CapabilitySplitApi
            ComposedSchema application = (ComposedSchema) schemas.get("CapabilitySplitApi").getProperties().get("application");
            application.getOneOf().remove(0);


            // Add schemas for response objects
            openApi.getComponents().addSchemas("AddSubscriptionsResponse", ModelConverters.getInstance().readAllAsResolvedSchema(AddSubscriptionsResponse.class).schema);
            openApi.getComponents().addSchemas("ListSubscriptionsResponse", ModelConverters.getInstance().readAllAsResolvedSchema(ListSubscriptionsResponse.class).schema);
            openApi.getComponents().addSchemas("GetSubscriptionResponse", ModelConverters.getInstance().readAllAsResolvedSchema(GetSubscriptionResponse.class).schema);
            openApi.getComponents().addSchemas("ListPrivateChannelsResponse", ModelConverters.getInstance().readAllAsResolvedSchema(ListPrivateChannelsResponse.class).schema);
            openApi.getComponents().addSchemas("AddPrivateChannelResponse", ModelConverters.getInstance().readAllAsResolvedSchema(AddPrivateChannelResponse.class).schema);
            openApi.getComponents().addSchemas("GetPrivateChannelResponse", ModelConverters.getInstance().readAllAsResolvedSchema(GetPrivateChannelResponse.class).schema);
            openApi.getComponents().addSchemas("ListPeerPrivateChannels", ModelConverters.getInstance().readAllAsResolvedSchema(ListPeerPrivateChannels.class).schema);
            openApi.getComponents().addSchemas("ListDeliveriesResponse", ModelConverters.getInstance().readAllAsResolvedSchema(ListDeliveriesResponse.class).schema);
            openApi.getComponents().addSchemas("AddDeliveriesResponse", ModelConverters.getInstance().readAllAsResolvedSchema(AddDeliveriesResponse.class).schema);
            openApi.getComponents().addSchemas("GetDeliveryResponse", ModelConverters.getInstance().readAllAsResolvedSchema(GetDeliveryResponse.class).schema);
            openApi.getComponents().addSchemas("ListCapabilitiesResponse", ModelConverters.getInstance().readAllAsResolvedSchema(GetDeliveryResponse.class).schema);
            openApi.getComponents().addSchemas("AddCapabilitiesResponse", ModelConverters.getInstance().readAllAsResolvedSchema(AddCapabilitiesResponse.class).schema);
            openApi.getComponents().addSchemas("FetchMatchingCapabilitiesResponse", ModelConverters.getInstance().readAllAsResolvedSchema(FetchMatchingCapabilitiesResponse.class).schema);
            openApi.getComponents().addSchemas("PeerPrivateChannelApi", ModelConverters.getInstance().readAllAsResolvedSchema(PeerPrivateChannelApi.class).schema);
            openApi.getComponents().addSchemas("Delivery", ModelConverters.getInstance().readAllAsResolvedSchema(Delivery.class).schema);
            openApi.getComponents().addSchemas("LocalActorCapability", ModelConverters.getInstance().readAllAsResolvedSchema(LocalActorCapability.class).schema);
            openApi.getComponents().addSchemas("DeliveryEndpoint", ModelConverters.getInstance().readAllAsResolvedSchema(DeliveryEndpoint.class).schema);
            openApi.getComponents().addSchemas("LocalActorSubscription", ModelConverters.getInstance().readAllAsResolvedSchema(LocalActorSubscription.class).schema);
            openApi.getComponents().addSchemas("LocalEndpointApi", ModelConverters.getInstance().readAllAsResolvedSchema(LocalEndpointApi.class).schema);
            openApi.getComponents().addSchemas("DeliveryEndpoint", ModelConverters.getInstance().readAllAsResolvedSchema(DeliveryEndpoint.class).schema);
            openApi.getComponents().addSchemas("PrivateChannelResponseApi", ModelConverters.getInstance().readAllAsResolvedSchema(PrivateChannelResponseApi.class).schema);
            openApi.getComponents().addSchemas("PrivateChannelEndpointApi", ModelConverters.getInstance().readAllAsResolvedSchema(PrivateChannelEndpointApi.class).schema);



            // AddPrivateChannelResponse gets an additional list 'privateChannelApis' in addition to the expected 'privateChannels'. This is a workaround, why this happens should be explored
            schemas.get("AddPrivateChannelResponse").getProperties().remove("privateChannelApis");

            // 'consumerCommonName' is not required when adding a subscription.
            schemas.get("AddSubscription").setRequired(List.of("selector"));
            schemas.get("AddSubscription").description("consumerCommonName is not required");



        };
    }
}
