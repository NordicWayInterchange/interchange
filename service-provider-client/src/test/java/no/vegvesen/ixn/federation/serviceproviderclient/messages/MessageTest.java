package no.vegvesen.ixn.federation.serviceproviderclient.messages;

import no.vegvesen.ixn.federation.serviceproviderrestclient.messages.*;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

public class MessageTest {

    private final ObjectMapper mapper = JsonMapper.builder().build();

    @Test
    public void testDenmMessage() throws JacksonException {
        Message denmMessage = new DenmMessage(
                "This is my message",
                "king_olav",
                "NO00000",
                "NO00000-DENM",
                "NO",
                "DENM:1.2.2",
                "service",
                "1.8.0",
                124,
                213,
                ",12004,",
                1,
                1,
                6,
                61
        );

        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(denmMessage));
    }

    @Test
    public void testDatexMessage() throws JacksonException {
        Message datexMessage = new DatexMessage(
                "This is my message",
                "king_olav",
                "NO00000",
                "NO00000-DATEX",
                "NO",
                "DATEX2:3.2",
                "service",
                "1.8.0",
                124,
                213,
                ",12004,",
                1,
                1,
                "SituationPublication",
                "RoadWorks",
                "publisherName"
        );

        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(datexMessage));
    }

    @Test
    public void testIvimMessage() throws JacksonException {
        Message ivimMessage = new IvimMessage(
                "This is my message",
                "king_olav",
                "NO00000",
                "NO00000-IVIM",
                "NO",
                "IVIM",
                "service",
                "1.8.0",
                124,
                213,
                ",12004,",
                1,
                1,
                ",1,",
                ",557,",
                ",giv,"
        );

        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ivimMessage));
    }

    @Test
    public void testSpatemMessage() throws JacksonException {
        Message spatemMessage = new SpatemMessage(
                "This is my message",
                "king_olav",
                "NO00000",
                "NO00000-SPATEM",
                "NO",
                "SPATEM",
                "service",
                "1.8.0",
                124,
                213,
                ",12004,",
                1,
                1,
                ",5-57,",
                ",name1,"
        );

        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(spatemMessage));
    }

    @Test
    public void testMapemMessage() throws JacksonException {
        Message mapemMessage = new MapemMessage(
                "This is my message",
                "king_olav",
                "NO00000",
                "NO00000-MAPEM",
                "NO",
                "MAPEM",
                "service",
                "1.8.0",
                124,
                213,
                ",12004,",
                1,
                1,
                ",5-57,",
                ",name1,"
        );

        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapemMessage));
    }

    @Test
    public void testSremMessage() throws JacksonException {
        Message sremMessage = new SremMessage(
                "This is my message",
                "king_olav",
                "NO00000",
                "NO00000-SREM",
                "NO",
                "SREM",
                "service",
                "1.8.0",
                124,
                213,
                ",12004,",
                1,
                1,
                ",5-57,"
        );

        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(sremMessage));
    }

    @Test
    public void testSsemMessage() throws JacksonException {
        Message ssemMessage = new SsemMessage(
                "This is my message",
                "king_olav",
                "NO00000",
                "NO00000-SSEM",
                "NO",
                "SSEM",
                "service",
                "1.8.0",
                124,
                213,
                ",12004,",
                1,
                1,
                ",5-57,"
        );

        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ssemMessage));
    }

    @Test
    public void testCamMessage() throws JacksonException {
        Message camMessage = new CamMessage(
                "This is my message",
                "king_olav",
                "NO00000",
                "NO00000-CAM",
                "NO",
                "CAM",
                "service",
                "1.8.0",
                124,
                213,
                ",12004,",
                1,
                1,
                7,
                5
        );

        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(camMessage));
    }

    @Test
    public void testJsonMessages() throws JacksonException {
        Message denmMessage = new DenmMessage(
                "This is my message",
                "king_olav",
                "NO00000",
                "NO00000-pub-1",
                "NO",
                "DENM:1.2.2",
                "service",
                "1.8.0",
                124,
                213,
                ",12004,",
                1,
                1,
                6,
                61
        );

        Message datexMessage = new DatexMessage(
                "This is my message",
                "king_olav",
                "NO00000",
                "NO00000-DATEX",
                "NO",
                "DATEX2:3.2",
                "service",
                "1.8.0",
                124,
                213,
                ",12004,",
                1,
                1,
                "SituationPublication",
                "RoadWorks",
                "publishername"
        );

        Message ivimMessage = new IvimMessage(
                "This is my message",
                "king_olav",
                "NO00000",
                "NO00000-IVIM",
                "NO",
                "IVIM",
                "service",
                "1.8.0",
                124,
                213,
                ",12004,",
                1,
                1,
                ",1,",
                ",557,",
                ",giv,"
        );

        Message spatemMessage = new SpatemMessage(
                "This is my message",
                "king_olav",
                "NO00000",
                "NO00000-SPATEM",
                "NO",
                "SPATEM",
                "service",
                "1.8.0",
                124,
                213,
                ",12004,",
                1,
                1,
                ",5-57,",
                ",name1,"
        );

        Message mapemMessage = new MapemMessage(
                "This is my message",
                "king_olav",
                "NO00000",
                "NO00000-MAPEM",
                "NO",
                "MAPEM",
                "service",
                "1.8.0",
                124,
                213,
                ",12004,",
                1,
                1,
                ",5-57,",
                ",name1,"
        );

        Message sremMessage = new SremMessage(
                "This is my message",
                "king_olav",
                "NO00000",
                "NO00000-SREM",
                "NO",
                "SREM",
                "service",
                "1.8.0",
                124,
                213,
                ",12004,",
                1,
                1,
                ",5-57,"
        );

        Message ssemMessage = new SsemMessage(
                "This is my message",
                "king_olav",
                "NO00000",
                "NO00000-SSEM",
                "NO",
                "SSEM",
                "service",
                "1.8.0",
                124,
                213,
                ",12004,",
                1,
                1,
                ",5-57,"
        );

        Message camMessage = new CamMessage(
                "This is my message",
                "king_olav",
                "NO00000",
                "NO00000-CAM",
                "NO",
                "CAM",
                "service",
                "1.8.0",
                124,
                213,
                ",12004,",
                1,
                1,
                7,
                5
        );

        Messages messages = new Messages(Arrays.asList(
                denmMessage,
                datexMessage,
                ivimMessage,
                spatemMessage,
                mapemMessage,
                ssemMessage,
                sremMessage,
                camMessage
        ));

        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(messages));
    }

    @Test
    public void jsonToMessageObject() throws IOException {
        File jsonfile = Path.of("src","test","resources","messages_king_olav.json").toFile();
        Messages messages = mapper.readValue(jsonfile, Messages.class);

        for (Message message : messages.getMessages()) {
            System.out.println(message.getMessageType());
        }
    }
}
