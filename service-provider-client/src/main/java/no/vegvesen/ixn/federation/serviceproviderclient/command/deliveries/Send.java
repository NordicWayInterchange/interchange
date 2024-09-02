package no.vegvesen.ixn.federation.serviceproviderclient.command.deliveries;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.MessageBuilder;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.federation.serviceproviderclient.messages.*;
import no.vegvesen.ixn.serviceprovider.model.*;

import static picocli.CommandLine.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static no.vegvesen.ixn.federation.api.v1_0.Constants.*;

@Command(name="send", description = "")
public class Send implements Callable<Integer> {

    @ParentCommand
    DeliveriesCommand parentCommand;

    @Option(names = {"-m", "--message"}, description = "The message json file", required = true)
    File messageFile;

    @ArgGroup(exclusive = true, multiplicity = "1")
    DeliveriesOption option;

    @Override
    public Integer call() throws Exception {

        ServiceProviderClient client = parentCommand.getParent().createClient();
        String deliveryId;
        if(option.file != null){
            ObjectMapper mapper = new ObjectMapper();
            AddDeliveriesRequest request = mapper.readValue(option.file, AddDeliveriesRequest.class);
            AddDeliveriesResponse response = client.addDeliveries(request);
            deliveryId = response.getDeliveries().stream().findFirst().get().getId();
        }
        else{
            AddDeliveriesResponse response = client.addDeliveries(new AddDeliveriesRequest(client.getUser(), Set.of(new SelectorApi(option.selector))));
            deliveryId = response.getDeliveries().stream().findFirst().get().getId();
        }
        GetDeliveryResponse delivery = client.getDelivery(deliveryId);

        while(!delivery.getStatus().equals(DeliveryStatus.CREATED)){
            if(!delivery.getStatus().equals(DeliveryStatus.REQUESTED)){
                throw new Exception("Delivery status is not valid");
            }
            delivery = client.getDelivery(deliveryId);
            TimeUnit.SECONDS.sleep(2);
        }
        System.out.println("Delivery created successfully, waiting for qpid to set up queue");
        TimeUnit.SECONDS.sleep(3);
        String queueName = delivery.getEndpoints().stream().findFirst().get().getTarget();
        String url = "amqps://" + delivery.getEndpoints().stream().findFirst().get().getHost();

        System.out.printf("Sending message from file %s%n",messageFile);
        ObjectMapper mapper = new ObjectMapper();
        Messages messages = mapper.readValue(messageFile, Messages.class);
        try (Source source = new Source(url, queueName, parentCommand.getParent().createSSLContext())) {
            source.start();
            for (Message message : messages.getMessages()) {
                MessageBuilder messageBuilder = source.createMessageBuilder();
                switch (message){
                    case DenmMessage ignored -> {
                        messageBuilder
                                .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                                .messageType(DENM)
                                .causeCode(((DenmMessage) message).getCauseCode())
                                .subCauseCode(((DenmMessage) message).getSubCauseCode());
                    }
                    case DatexMessage ignored -> {
                        messageBuilder
                                .textMessage(message.getMessageText())
                                .messageType(DATEX_2)
                                .publicationType(((DatexMessage) message).getPublicationType())
                                .publicationSubType(((DatexMessage) message).getPublicationSubType())
                                .publisherName(((DatexMessage) message).getPublisherName());
                    }
                    case IvimMessage ignored -> {
                        messageBuilder
                                .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                                .messageType(IVIM)
                                .iviType(((IvimMessage) message).getIviType())
                                .pictogramCategoryCode(((IvimMessage) message).getPictogramCategoryCode())
                                .iviContainer(((IvimMessage) message).getIviContainer());
                    }
                    case SpatemMessage ignored -> {
                        messageBuilder
                                .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                                .messageType(SPATEM)
                                .id(((SpatemMessage) message).getId())
                                .name(((SpatemMessage) message).getName());
                    }
                    case MapemMessage ignored -> {
                        messageBuilder
                                .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                                .messageType(MAPEM)
                                .id(((MapemMessage) message).getId())
                                .name(((MapemMessage) message).getName());
                    }
                    case SsemMessage ignored -> {
                        messageBuilder
                                .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                                .messageType(SSEM)
                                .id(((SsemMessage) message).getId());
                    }
                    case SremMessage ignored -> {
                        messageBuilder
                                .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                                .messageType(SREM)
                                .id(((SremMessage) message).getId());
                    }
                    case CamMessage ignored -> {
                        messageBuilder
                                .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                                .messageType(CAM)
                                .stationType(((CamMessage) message).getStationType())
                                .vehicleRole(((CamMessage) message).getVehicleRole());
                    }

                    default ->  throw new Exception("Message is not of valid messagetype");
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

    private static class DeliveriesOption{
        @Option(names = {"-f", "--file"}, required = true, description = "The deliveries json file")
        File file;

        @Option(names = {"-s", "--selector"}, required = true, description = "The delivery selector")
        String selector;
    }
}
