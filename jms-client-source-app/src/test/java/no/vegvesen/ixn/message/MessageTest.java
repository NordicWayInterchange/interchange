package no.vegvesen.ixn.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class MessageTest {

    @Test
    public void testDenmMessage() throws JsonProcessingException {
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

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(denmMessage));
    }

    @Test
    public void testDatexMessage() throws JsonProcessingException {
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
                "RoadWorks"
        );

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(datexMessage));
    }

    @Test
    public void testIvimMessage() throws JsonProcessingException {
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

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ivimMessage));
    }

    @Test
    public void testSpatemMessage() throws JsonProcessingException {
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

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(spatemMessage));
    }

    @Test
    public void testMapemMessage() throws JsonProcessingException {
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

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapemMessage));
    }

    @Test
    public void testSremMessage() throws JsonProcessingException {
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

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(sremMessage));
    }

    @Test
    public void testSsemMessage() throws JsonProcessingException {
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

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ssemMessage));
    }

    @Test
    public void testCamMessage() throws JsonProcessingException {
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

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(camMessage));
    }

    @Test
    public void testJsonMessages() throws JsonProcessingException {
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
                "RoadWorks"
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

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(messages));
    }

    @Test
    public void jsonToMessageObject() throws IOException {
        File jsonfile = Path.of("messages_king_olav.json").toFile();
        ObjectMapper mapper = new ObjectMapper();
        Messages messages = mapper.readValue(jsonfile, Messages.class);

        for (Message message : messages.getMessages()) {
            System.out.println(message.getMessageType());
        }
    }
}
