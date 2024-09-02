package no.vegvesen.ixn.federation.serviceproviderclient.command.jms;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.MessageBuilder;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.federation.api.v1_0.capability.*;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.federation.serviceproviderclient.command.deliveries.AddDeliveries;
import no.vegvesen.ixn.federation.serviceproviderclient.messages.*;
import no.vegvesen.ixn.serviceprovider.model.*;

import static picocli.CommandLine.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static no.vegvesen.ixn.federation.api.v1_0.Constants.*;

@Command(name = "send", description = "Sending a message from JSON")
public class SendMessage implements Callable<Integer> {

    @Parameters(paramLabel = "QUEUE", description = "The queueName to connect to")
    private String queueName;

    @Option(names = {"-f", "--filename"}, description = "The message json file", required = true)
    File messageFile;

    @Option(names = {"-s", "--selector"}, description = "selector", required = true)
    String selector;

    @Option(names={"-c", "--capabilities"}, description = "capabilities", required = false)
    File CapabilitiesFile;

    @ParentCommand
    MessagesCommand parentCommand;

    @Override
    public Integer call() throws Exception {
        System.out.printf("Sending message from file %s%n",messageFile);

        ServiceProviderClient client = parentCommand.getParent().createClient();
        client.addServiceProviderDeliveries(new AddDeliveriesRequest(client.getUser(), Set.of(new SelectorApi(selector))));
        client.addCapability(getCapabilitiesRequest());

        String deliveryId = client.listServiceProviderDeliveries().getDeliveries().stream().filter(d -> d.getSelector().equals(selector)).findFirst().get().getId();

        GetDeliveryResponse delivery = client.getDelivery(deliveryId);
        while(!delivery.getStatus().equals(DeliveryStatus.CREATED)){
            delivery = client.getDelivery(deliveryId);
        }
        String url = delivery.getEndpoints().stream().findFirst().get().getTarget();

        ObjectMapper mapper = new ObjectMapper();
        Messages messages = mapper.readValue(messageFile, Messages.class);
        try (Source source = new Source(url, queueName, parentCommand.createContext())) {
            source.start();
            for (Message message : messages.getMessages()) {
                MessageBuilder messageBuilder = source.createMessageBuilder();
                if (message instanceof DenmMessage) {
                    messageBuilder
                            .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                            .messageType(DENM)
                            .causeCode(((DenmMessage) message).getCauseCode())
                            .subCauseCode(((DenmMessage) message).getSubCauseCode());
                } else if (message instanceof DatexMessage) {
                    messageBuilder
                            .textMessage(message.getMessageText())
                            .messageType(DATEX_2)
                            .publicationType(((DatexMessage) message).getPublicationType())
                            .publicationSubType(((DatexMessage) message).getPublicationSubType())
                            .publisherName(((DatexMessage) message).getPublisherName());
                } else if (message instanceof IvimMessage) {
                    messageBuilder
                            .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                            .messageType(IVIM)
                            .iviType(((IvimMessage) message).getIviType())
                            .pictogramCategoryCode(((IvimMessage) message).getPictogramCategoryCode())
                            .iviContainer(((IvimMessage) message).getIviContainer());
                } else if (message instanceof SpatemMessage) {
                    messageBuilder
                            .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                            .messageType(SPATEM)
                            .id(((SpatemMessage) message).getId())
                            .name(((SpatemMessage) message).getName());
                } else if (message instanceof MapemMessage) {
                    messageBuilder
                            .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                            .messageType(MAPEM)
                            .id(((MapemMessage) message).getId())
                            .name(((MapemMessage) message).getName());
                } else if (message instanceof SsemMessage) {
                    messageBuilder
                            .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                            .messageType(SSEM)
                            .id(((SsemMessage) message).getId());
                } else if (message instanceof SremMessage) {
                    messageBuilder
                            .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                            .messageType(SREM)
                            .id(((SremMessage) message).getId());
                } else if (message instanceof CamMessage) {
                    messageBuilder
                            .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                            .messageType(CAM)
                            .stationType(((CamMessage) message).getStationType())
                            .vehicleRole(((CamMessage) message).getVehicleRole());
                } else {
                    throw new Exception("Message is not of valid messagetype");
                }
                messageBuilder
                        .userId(message.getUserId())
                        .publisherId(message.getPublisherId())
                        .publicationId(message.getPublicationId())
                        .originatingCountry(message.getOriginatingCountry())
                        .protocolVersion(message.getProtocolVersion())
                        .longitude(message.getLongitude())
                        .latitude(message.getLatitude())
                        .quadTreeTiles(message.getQuadTree())
                        .shardId(message.getShardId())
                        .shardCount(message.getShardCount())
                        .baselineVersion(message.getBaselineVersion())
                        .serviceType(message.getServiceType());
                source.send(messageBuilder.build());

            }
        }
        return 0;
    }

    public AddCapabilitiesRequest getCapabilitiesRequest() {
        AddCapabilitiesRequest capabilitiesRequest = new AddCapabilitiesRequest();
        String publisherId = "bouvet";
        String originatingCountry="NO";
        List<String> quadTree = List.of("12");
        List<String> messageTypes = List.of("DATEX2", "DENM", "CAM", "SSEM", "SREM", "IVIM", "MAPEM", "SPATEM");
        for(int i = 0; i < 8; i++){
            CapabilityApi capability = new CapabilityApi();
            if(messageTypes.get(i).equals("DATEX2")){
                capability.setApplication(
                        new DatexApplicationApi(publisherId, publisherId + ":" +i, originatingCountry, messageTypes.get(i)+":1", quadTree, "warning", "bouvet")
                );
            }
            else if(messageTypes.get(i).equals("DENM")){
                capability.setApplication(
                        new DenmApplicationApi(publisherId, publisherId + ":" + i, originatingCountry, messageTypes.get(i)+":1", quadTree, List.of(6))
                );
            }
            else{
                capability.setApplication(
                        new ApplicationApi(messageTypes.get(i), publisherId, publisherId+":"+i, originatingCountry, messageTypes.get(i)+":1", quadTree));

            }
            capability.setMetadata(new MetadataApi());
            capabilitiesRequest.getCapabilities().add(capability);
        }
        return capabilitiesRequest;
    }
}
