package no.vegvesen.ixn.federation.qpid;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.type.CollectionType;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ExchangeTest {
    @Test
    public void readExchangesFromJsonFile() throws IOException {
        File file = new File("src/test/resources/exchanges.json");
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory typeFactory = mapper.getTypeFactory();

        CollectionType collectionType = typeFactory.constructCollectionType(
                List.class, Exchange.class);

        List<Exchange> result = mapper.readValue(file, collectionType);
        assertThat(result.size()).isEqualTo(4);
    }

    @Test
    public void readExchangeFromJson() throws IOException {
        String object = "{\n" +
                "    \"id\": \"f2242367-1ba7-4bfc-8122-c55cc729cdb2\",\n" +
                "    \"name\": \"cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab\",\n" +
                "    \"type\": \"headers\",\n" +
                "    \"desiredState\": \"ACTIVE\",\n" +
                "    \"state\": \"ACTIVE\",\n" +
                "    \"durable\": true,\n" +
                "    \"lifetimePolicy\": \"PERMANENT\",\n" +
                "    \"bindings\": [\n" +
                "      {\n" +
                "        \"bindingKey\": \"cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab\",\n" +
                "        \"destination\": \"bi-queue\",\n" +
                "        \"arguments\": {\n" +
                "          \"x-filter-jms-selector\": \"(quadTree like '%,12002031%' OR quadTree like '%,1200201%' OR quadTree like '%,10223310%' OR quadTree like '%,12001022%' OR quadTree like '%,12001020%' OR quadTree like '%,1200013%' OR quadTree like '%,12002300%' OR quadTree like '%,1022303%' OR quadTree like '%,12002302%' OR quadTree like '%,12001200%' OR quadTree like '%,1200212%' OR quadTree like '%,120030012%' OR quadTree like '%,12001202%' OR quadTree like '%,1200210%' OR quadTree like '%,120030010%' OR quadTree like '%,120030222%' OR quadTree like '%,120030220%' OR quadTree like '%,1200023%' OR quadTree like '%,12001032%' OR quadTree like '%,1023211%' OR quadTree like '%,1023213000%' OR quadTree like '%,102302%' OR quadTree like '%,12003002%' OR quadTree like '%,12003000%' OR quadTree like '%,102232%' OR quadTree like '%,12002033%' OR quadTree like '%,102231%' OR quadTree like '%,1022133%' OR quadTree like '%,12001023%' OR quadTree like '%,12003020%' OR quadTree like '%,12001021%' OR quadTree like '%,12002301%' OR quadTree like '%,12002303%' OR quadTree like '%,120012100%' OR quadTree like '%,12001030%' OR quadTree like '%,1200100%' OR quadTree like '%,12002211%' OR quadTree like '%,12001201%' OR quadTree like '%,102322011%' OR quadTree like '%,12001222%' OR quadTree like '%,1200213%' OR quadTree like '%,12001220%' OR quadTree like '%,120003%' OR quadTree like '%,1200211%' OR quadTree like '%,12001012%' OR quadTree like '%,12002310%' OR quadTree like '%,1022330%' OR quadTree like '%,102320%' OR quadTree like '%,1023212%' OR quadTree like '%,12001010%' OR quadTree like '%,1023210%' OR quadTree like '%,102303%') AND (publisherId = 'NO00000') AND (messageType = 'DENM') AND (originatingCountry = 'NO') AND (causeCode = '6' OR causeCode = '8' OR causeCode = '2' OR causeCode = '4' OR causeCode = '18' OR causeCode = '12' OR causeCode = '22' OR causeCode = '10' OR causeCode = '20' OR causeCode = '16' OR causeCode = '14' OR causeCode = '24' OR causeCode = '7' OR causeCode = '9' OR causeCode = '3' OR causeCode = '5' OR causeCode = '19' OR causeCode = '1' OR causeCode = '17' OR causeCode = '23' OR causeCode = '11' OR causeCode = '21' OR causeCode = '15' OR causeCode = '25' OR causeCode = '13') AND (protocolVersion = 'DENM:1.2.1')\"\n" +
                "        },\n" +
                "        \"name\": \"cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab\",\n" +
                "        \"type\": \"binding\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"bindingKey\": \"cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab\",\n" +
                "        \"destination\": \"sub-0bce6c1d-7867-456e-9b2b-4cc91b13f0e7\",\n" +
                "        \"arguments\": {\n" +
                "          \"x-filter-jms-selector\": \"(messageType = 'DENM' AND originatingCountry = 'NO' AND protocolVersion = 'DENM:1.2.1' AND publisherId = 'NO00000') AND (publicationId = 'NO00000:d8641a5c') AND (quadTree like '%,1022133%' OR quadTree like '%,1022330%' OR quadTree like '%,102320%' OR quadTree like '%,12003020%' OR quadTree like '%,120030010%' OR quadTree like '%,120012100%' OR quadTree like '%,120030012%' OR quadTree like '%,1023213000%' OR quadTree like '%,1022303%' OR quadTree like '%,12001030%' OR quadTree like '%,12001032%' OR quadTree like '%,1200201%' OR quadTree like '%,12002310%' OR quadTree like '%,12001023%' OR quadTree like '%,12001220%' OR quadTree like '%,12001022%' OR quadTree like '%,12003002%' OR quadTree like '%,12001222%' OR quadTree like '%,120030222%' OR quadTree like '%,120030220%' OR quadTree like '%,12003000%' OR quadTree like '%,12002031%' OR quadTree like '%,12002033%' OR quadTree like '%,12001021%' OR quadTree like '%,12001020%' OR quadTree like '%,1200212%' OR quadTree like '%,1200213%' OR quadTree like '%,1200210%' OR quadTree like '%,1200211%' OR quadTree like '%,1200013%' OR quadTree like '%,12002303%' OR quadTree like '%,120003%' OR quadTree like '%,12002302%' OR quadTree like '%,12001012%' OR quadTree like '%,12002301%' OR quadTree like '%,12002300%' OR quadTree like '%,102322011%' OR quadTree like '%,1023211%' OR quadTree like '%,1023212%' OR quadTree like '%,1023210%' OR quadTree like '%,102231%' OR quadTree like '%,102232%' OR quadTree like '%,12001010%' OR quadTree like '%,1200100%' OR quadTree like '%,1200023%' OR quadTree like '%,12001202%' OR quadTree like '%,10223310%' OR quadTree like '%,12002211%' OR quadTree like '%,12001201%' OR quadTree like '%,12001200%' OR quadTree like '%,102303%' OR quadTree like '%,102302%')\"\n" +
                "        },\n" +
                "        \"name\": \"cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab\",\n" +
                "        \"type\": \"binding\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"durableBindings\": [\n" +
                "      {\n" +
                "        \"bindingKey\": \"cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab\",\n" +
                "        \"destination\": \"bi-queue\",\n" +
                "        \"arguments\": {\n" +
                "          \"x-filter-jms-selector\": \"(quadTree like '%,12002031%' OR quadTree like '%,1200201%' OR quadTree like '%,10223310%' OR quadTree like '%,12001022%' OR quadTree like '%,12001020%' OR quadTree like '%,1200013%' OR quadTree like '%,12002300%' OR quadTree like '%,1022303%' OR quadTree like '%,12002302%' OR quadTree like '%,12001200%' OR quadTree like '%,1200212%' OR quadTree like '%,120030012%' OR quadTree like '%,12001202%' OR quadTree like '%,1200210%' OR quadTree like '%,120030010%' OR quadTree like '%,120030222%' OR quadTree like '%,120030220%' OR quadTree like '%,1200023%' OR quadTree like '%,12001032%' OR quadTree like '%,1023211%' OR quadTree like '%,1023213000%' OR quadTree like '%,102302%' OR quadTree like '%,12003002%' OR quadTree like '%,12003000%' OR quadTree like '%,102232%' OR quadTree like '%,12002033%' OR quadTree like '%,102231%' OR quadTree like '%,1022133%' OR quadTree like '%,12001023%' OR quadTree like '%,12003020%' OR quadTree like '%,12001021%' OR quadTree like '%,12002301%' OR quadTree like '%,12002303%' OR quadTree like '%,120012100%' OR quadTree like '%,12001030%' OR quadTree like '%,1200100%' OR quadTree like '%,12002211%' OR quadTree like '%,12001201%' OR quadTree like '%,102322011%' OR quadTree like '%,12001222%' OR quadTree like '%,1200213%' OR quadTree like '%,12001220%' OR quadTree like '%,120003%' OR quadTree like '%,1200211%' OR quadTree like '%,12001012%' OR quadTree like '%,12002310%' OR quadTree like '%,1022330%' OR quadTree like '%,102320%' OR quadTree like '%,1023212%' OR quadTree like '%,12001010%' OR quadTree like '%,1023210%' OR quadTree like '%,102303%') AND (publisherId = 'NO00000') AND (messageType = 'DENM') AND (originatingCountry = 'NO') AND (causeCode = '6' OR causeCode = '8' OR causeCode = '2' OR causeCode = '4' OR causeCode = '18' OR causeCode = '12' OR causeCode = '22' OR causeCode = '10' OR causeCode = '20' OR causeCode = '16' OR causeCode = '14' OR causeCode = '24' OR causeCode = '7' OR causeCode = '9' OR causeCode = '3' OR causeCode = '5' OR causeCode = '19' OR causeCode = '1' OR causeCode = '17' OR causeCode = '23' OR causeCode = '11' OR causeCode = '21' OR causeCode = '15' OR causeCode = '25' OR causeCode = '13') AND (protocolVersion = 'DENM:1.2.1')\"\n" +
                "        },\n" +
                "        \"name\": \"cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab\",\n" +
                "        \"type\": \"binding\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"bindingKey\": \"cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab\",\n" +
                "        \"destination\": \"sub-0bce6c1d-7867-456e-9b2b-4cc91b13f0e7\",\n" +
                "        \"arguments\": {\n" +
                "          \"x-filter-jms-selector\": \"(messageType = 'DENM' AND originatingCountry = 'NO' AND protocolVersion = 'DENM:1.2.1' AND publisherId = 'NO00000') AND (publicationId = 'NO00000:d8641a5c') AND (quadTree like '%,1022133%' OR quadTree like '%,1022330%' OR quadTree like '%,102320%' OR quadTree like '%,12003020%' OR quadTree like '%,120030010%' OR quadTree like '%,120012100%' OR quadTree like '%,120030012%' OR quadTree like '%,1023213000%' OR quadTree like '%,1022303%' OR quadTree like '%,12001030%' OR quadTree like '%,12001032%' OR quadTree like '%,1200201%' OR quadTree like '%,12002310%' OR quadTree like '%,12001023%' OR quadTree like '%,12001220%' OR quadTree like '%,12001022%' OR quadTree like '%,12003002%' OR quadTree like '%,12001222%' OR quadTree like '%,120030222%' OR quadTree like '%,120030220%' OR quadTree like '%,12003000%' OR quadTree like '%,12002031%' OR quadTree like '%,12002033%' OR quadTree like '%,12001021%' OR quadTree like '%,12001020%' OR quadTree like '%,1200212%' OR quadTree like '%,1200213%' OR quadTree like '%,1200210%' OR quadTree like '%,1200211%' OR quadTree like '%,1200013%' OR quadTree like '%,12002303%' OR quadTree like '%,120003%' OR quadTree like '%,12002302%' OR quadTree like '%,12001012%' OR quadTree like '%,12002301%' OR quadTree like '%,12002300%' OR quadTree like '%,102322011%' OR quadTree like '%,1023211%' OR quadTree like '%,1023212%' OR quadTree like '%,1023210%' OR quadTree like '%,102231%' OR quadTree like '%,102232%' OR quadTree like '%,12001010%' OR quadTree like '%,1200100%' OR quadTree like '%,1200023%' OR quadTree like '%,12001202%' OR quadTree like '%,10223310%' OR quadTree like '%,12002211%' OR quadTree like '%,12001201%' OR quadTree like '%,12001200%' OR quadTree like '%,102303%' OR quadTree like '%,102302%')\"\n" +
                "        },\n" +
                "        \"name\": \"cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab\",\n" +
                "        \"type\": \"binding\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"lastOpenedTime\": 1683778082266,\n" +
                "    \"unroutableMessageBehaviour\": \"DISCARD\",\n" +
                "    \"lastUpdatedBy\": \"bouvet.pilotinterchange.eu\",\n" +
                "    \"lastUpdatedTime\": 1683792055258,\n" +
                "    \"createdBy\": \"bouvet.pilotinterchange.eu\",\n" +
                "    \"createdTime\": 1683026850137,\n" +
                "    \"statistics\": {\n" +
                "      \"bindingCount\": 2,\n" +
                "      \"bytesDropped\": 0,\n" +
                "      \"bytesIn\": 0,\n" +
                "      \"messagesDropped\": 0,\n" +
                "      \"messagesIn\": 0\n" +
                "    }\n" +
                "  }";

        ObjectMapper mapper = new ObjectMapper();
        Exchange result = mapper.readValue(object, Exchange.class);
        assertThat(result.getName()).isEqualTo("cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab");
        assertThat(result.getBindings().size()).isEqualTo(2);
    }

}
