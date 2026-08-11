package no.vegvesen.ixn.federation.qpid;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.type.CollectionType;
import tools.jackson.databind.type.TypeFactory;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
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
        Exchange exchange = result.getFirst();
        assertThat(exchange.getId()).isEqualTo("3396d263-1767-4658-a192-83e35772395e");
        assertThat(exchange.getName()).isEqualTo("d5bb43db-90e5-4700-ae58-a3c40aeba250");
        assertThat(exchange.getType()).isEqualTo("headers");
        assertThat(exchange.isDurable()).isTrue();

    }

    @Test
    public void readExchangeFromJson() throws IOException {
        String object = """
                {
                    "id": "f2242367-1ba7-4bfc-8122-c55cc729cdb2",
                    "name": "cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab",
                    "type": "headers",
                    "desiredState": "ACTIVE",
                    "state": "ACTIVE",
                    "durable": true,
                    "lifetimePolicy": "PERMANENT",
                    "bindings": [
                      {
                        "bindingKey": "cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab",
                        "destination": "bi-queue",
                        "arguments": {
                          "x-filter-jms-selector": "(quadTree like '%,12002031%' OR quadTree like '%,1200201%' OR quadTree like '%,10223310%' OR quadTree like '%,12001022%' OR quadTree like '%,12001020%' OR quadTree like '%,1200013%' OR quadTree like '%,12002300%' OR quadTree like '%,1022303%' OR quadTree like '%,12002302%' OR quadTree like '%,12001200%' OR quadTree like '%,1200212%' OR quadTree like '%,120030012%' OR quadTree like '%,12001202%' OR quadTree like '%,1200210%' OR quadTree like '%,120030010%' OR quadTree like '%,120030222%' OR quadTree like '%,120030220%' OR quadTree like '%,1200023%' OR quadTree like '%,12001032%' OR quadTree like '%,1023211%' OR quadTree like '%,1023213000%' OR quadTree like '%,102302%' OR quadTree like '%,12003002%' OR quadTree like '%,12003000%' OR quadTree like '%,102232%' OR quadTree like '%,12002033%' OR quadTree like '%,102231%' OR quadTree like '%,1022133%' OR quadTree like '%,12001023%' OR quadTree like '%,12003020%' OR quadTree like '%,12001021%' OR quadTree like '%,12002301%' OR quadTree like '%,12002303%' OR quadTree like '%,120012100%' OR quadTree like '%,12001030%' OR quadTree like '%,1200100%' OR quadTree like '%,12002211%' OR quadTree like '%,12001201%' OR quadTree like '%,102322011%' OR quadTree like '%,12001222%' OR quadTree like '%,1200213%' OR quadTree like '%,12001220%' OR quadTree like '%,120003%' OR quadTree like '%,1200211%' OR quadTree like '%,12001012%' OR quadTree like '%,12002310%' OR quadTree like '%,1022330%' OR quadTree like '%,102320%' OR quadTree like '%,1023212%' OR quadTree like '%,12001010%' OR quadTree like '%,1023210%' OR quadTree like '%,102303%') AND (publisherId = 'NO00000') AND (messageType = 'DENM') AND (originatingCountry = 'NO') AND (causeCode = '6' OR causeCode = '8' OR causeCode = '2' OR causeCode = '4' OR causeCode = '18' OR causeCode = '12' OR causeCode = '22' OR causeCode = '10' OR causeCode = '20' OR causeCode = '16' OR causeCode = '14' OR causeCode = '24' OR causeCode = '7' OR causeCode = '9' OR causeCode = '3' OR causeCode = '5' OR causeCode = '19' OR causeCode = '1' OR causeCode = '17' OR causeCode = '23' OR causeCode = '11' OR causeCode = '21' OR causeCode = '15' OR causeCode = '25' OR causeCode = '13') AND (protocolVersion = 'DENM:1.2.1')"
                        },
                        "name": "cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab",
                        "type": "binding"
                      },
                      {
                        "bindingKey": "cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab",
                        "destination": "sub-0bce6c1d-7867-456e-9b2b-4cc91b13f0e7",
                        "arguments": {
                          "x-filter-jms-selector": "(messageType = 'DENM' AND originatingCountry = 'NO' AND protocolVersion = 'DENM:1.2.1' AND publisherId = 'NO00000') AND (publicationId = 'NO00000:d8641a5c') AND (quadTree like '%,1022133%' OR quadTree like '%,1022330%' OR quadTree like '%,102320%' OR quadTree like '%,12003020%' OR quadTree like '%,120030010%' OR quadTree like '%,120012100%' OR quadTree like '%,120030012%' OR quadTree like '%,1023213000%' OR quadTree like '%,1022303%' OR quadTree like '%,12001030%' OR quadTree like '%,12001032%' OR quadTree like '%,1200201%' OR quadTree like '%,12002310%' OR quadTree like '%,12001023%' OR quadTree like '%,12001220%' OR quadTree like '%,12001022%' OR quadTree like '%,12003002%' OR quadTree like '%,12001222%' OR quadTree like '%,120030222%' OR quadTree like '%,120030220%' OR quadTree like '%,12003000%' OR quadTree like '%,12002031%' OR quadTree like '%,12002033%' OR quadTree like '%,12001021%' OR quadTree like '%,12001020%' OR quadTree like '%,1200212%' OR quadTree like '%,1200213%' OR quadTree like '%,1200210%' OR quadTree like '%,1200211%' OR quadTree like '%,1200013%' OR quadTree like '%,12002303%' OR quadTree like '%,120003%' OR quadTree like '%,12002302%' OR quadTree like '%,12001012%' OR quadTree like '%,12002301%' OR quadTree like '%,12002300%' OR quadTree like '%,102322011%' OR quadTree like '%,1023211%' OR quadTree like '%,1023212%' OR quadTree like '%,1023210%' OR quadTree like '%,102231%' OR quadTree like '%,102232%' OR quadTree like '%,12001010%' OR quadTree like '%,1200100%' OR quadTree like '%,1200023%' OR quadTree like '%,12001202%' OR quadTree like '%,10223310%' OR quadTree like '%,12002211%' OR quadTree like '%,12001201%' OR quadTree like '%,12001200%' OR quadTree like '%,102303%' OR quadTree like '%,102302%')"
                        },
                        "name": "cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab",
                        "type": "binding"
                      }
                    ],
                    "durableBindings": [
                      {
                        "bindingKey": "cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab",
                        "destination": "bi-queue",
                        "arguments": {
                          "x-filter-jms-selector": "(quadTree like '%,12002031%' OR quadTree like '%,1200201%' OR quadTree like '%,10223310%' OR quadTree like '%,12001022%' OR quadTree like '%,12001020%' OR quadTree like '%,1200013%' OR quadTree like '%,12002300%' OR quadTree like '%,1022303%' OR quadTree like '%,12002302%' OR quadTree like '%,12001200%' OR quadTree like '%,1200212%' OR quadTree like '%,120030012%' OR quadTree like '%,12001202%' OR quadTree like '%,1200210%' OR quadTree like '%,120030010%' OR quadTree like '%,120030222%' OR quadTree like '%,120030220%' OR quadTree like '%,1200023%' OR quadTree like '%,12001032%' OR quadTree like '%,1023211%' OR quadTree like '%,1023213000%' OR quadTree like '%,102302%' OR quadTree like '%,12003002%' OR quadTree like '%,12003000%' OR quadTree like '%,102232%' OR quadTree like '%,12002033%' OR quadTree like '%,102231%' OR quadTree like '%,1022133%' OR quadTree like '%,12001023%' OR quadTree like '%,12003020%' OR quadTree like '%,12001021%' OR quadTree like '%,12002301%' OR quadTree like '%,12002303%' OR quadTree like '%,120012100%' OR quadTree like '%,12001030%' OR quadTree like '%,1200100%' OR quadTree like '%,12002211%' OR quadTree like '%,12001201%' OR quadTree like '%,102322011%' OR quadTree like '%,12001222%' OR quadTree like '%,1200213%' OR quadTree like '%,12001220%' OR quadTree like '%,120003%' OR quadTree like '%,1200211%' OR quadTree like '%,12001012%' OR quadTree like '%,12002310%' OR quadTree like '%,1022330%' OR quadTree like '%,102320%' OR quadTree like '%,1023212%' OR quadTree like '%,12001010%' OR quadTree like '%,1023210%' OR quadTree like '%,102303%') AND (publisherId = 'NO00000') AND (messageType = 'DENM') AND (originatingCountry = 'NO') AND (causeCode = '6' OR causeCode = '8' OR causeCode = '2' OR causeCode = '4' OR causeCode = '18' OR causeCode = '12' OR causeCode = '22' OR causeCode = '10' OR causeCode = '20' OR causeCode = '16' OR causeCode = '14' OR causeCode = '24' OR causeCode = '7' OR causeCode = '9' OR causeCode = '3' OR causeCode = '5' OR causeCode = '19' OR causeCode = '1' OR causeCode = '17' OR causeCode = '23' OR causeCode = '11' OR causeCode = '21' OR causeCode = '15' OR causeCode = '25' OR causeCode = '13') AND (protocolVersion = 'DENM:1.2.1')"
                        },
                        "name": "cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab",
                        "type": "binding"
                      },
                      {
                        "bindingKey": "cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab",
                        "destination": "sub-0bce6c1d-7867-456e-9b2b-4cc91b13f0e7",
                        "arguments": {
                          "x-filter-jms-selector": "(messageType = 'DENM' AND originatingCountry = 'NO' AND protocolVersion = 'DENM:1.2.1' AND publisherId = 'NO00000') AND (publicationId = 'NO00000:d8641a5c') AND (quadTree like '%,1022133%' OR quadTree like '%,1022330%' OR quadTree like '%,102320%' OR quadTree like '%,12003020%' OR quadTree like '%,120030010%' OR quadTree like '%,120012100%' OR quadTree like '%,120030012%' OR quadTree like '%,1023213000%' OR quadTree like '%,1022303%' OR quadTree like '%,12001030%' OR quadTree like '%,12001032%' OR quadTree like '%,1200201%' OR quadTree like '%,12002310%' OR quadTree like '%,12001023%' OR quadTree like '%,12001220%' OR quadTree like '%,12001022%' OR quadTree like '%,12003002%' OR quadTree like '%,12001222%' OR quadTree like '%,120030222%' OR quadTree like '%,120030220%' OR quadTree like '%,12003000%' OR quadTree like '%,12002031%' OR quadTree like '%,12002033%' OR quadTree like '%,12001021%' OR quadTree like '%,12001020%' OR quadTree like '%,1200212%' OR quadTree like '%,1200213%' OR quadTree like '%,1200210%' OR quadTree like '%,1200211%' OR quadTree like '%,1200013%' OR quadTree like '%,12002303%' OR quadTree like '%,120003%' OR quadTree like '%,12002302%' OR quadTree like '%,12001012%' OR quadTree like '%,12002301%' OR quadTree like '%,12002300%' OR quadTree like '%,102322011%' OR quadTree like '%,1023211%' OR quadTree like '%,1023212%' OR quadTree like '%,1023210%' OR quadTree like '%,102231%' OR quadTree like '%,102232%' OR quadTree like '%,12001010%' OR quadTree like '%,1200100%' OR quadTree like '%,1200023%' OR quadTree like '%,12001202%' OR quadTree like '%,10223310%' OR quadTree like '%,12002211%' OR quadTree like '%,12001201%' OR quadTree like '%,12001200%' OR quadTree like '%,102303%' OR quadTree like '%,102302%')"
                        },
                        "name": "cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab",
                        "type": "binding"
                      }
                    ],
                    "lastOpenedTime": 1683778082266,
                    "unroutableMessageBehaviour": "DISCARD",
                    "lastUpdatedBy": "bouvet.pilotinterchange.eu",
                    "lastUpdatedTime": 1683792055258,
                    "createdBy": "bouvet.pilotinterchange.eu",
                    "createdTime": 1683026850137,
                    "statistics": {
                      "bindingCount": 2,
                      "bytesDropped": 0,
                      "bytesIn": 0,
                      "messagesDropped": 0,
                      "messagesIn": 0
                    }
                  }""";

        ObjectMapper mapper = new ObjectMapper();
        Exchange result = mapper.readValue(object, Exchange.class);
        assertThat(result.getName()).isEqualTo("cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab");
        assertThat(result.getBindings().size()).isEqualTo(2);
    }

    @Test
    public void testJsonWithNameOnly() throws JacksonException {
        Exchange exchange = new Exchange("test-exchange");
        String result = new ObjectMapper().writeValueAsString(exchange);
        assertThat(result).contains("name");
        assertThat(result).contains("durable");
        assertThat(result).contains("type");
        assertThat(result).contains("bindings"); //TODO are bindings really needed? Test in qpidClientIT
        assertThat(result).doesNotContain("id");
    }


    @Test
    public void testJsonWithOneBinding() throws JacksonException {
        Exchange exchange = new Exchange("test-exchange", Collections.singletonList(
                new Binding("my-binding-key", "my-destination", new Filter("a = b"))
        ));
        String result = new ObjectMapper().writeValueAsString(exchange);
        assertThat(result).contains("name");
        assertThat(result).contains("durable");
        assertThat(result).contains("type");
        assertThat(result).contains("bindings");
        assertThat(result).doesNotContain("id");
        assertThat(result).contains("my-binding-key");
    }

    @Test
    public void testExchangeWithAlternativeBinding() throws JacksonException {
        String exhangeJson = """
{
  "id" : "d936db17-2e68-4514-ab30-e84365a5498a",
  "name" : "my-exchange",
  "type" : "direct",
  "desiredState" : "ACTIVE",
  "state" : "ACTIVE",
  "durable" : true,
  "lifetimePolicy" : "PERMANENT",
  "alternateBinding" : {
    "destination" : "my-dlq"
  },
  "bindings" : [ ],
  "durableBindings" : [ ],
  "lastOpenedTime" : 1751539609111,
  "unroutableMessageBehaviour" : "DISCARD",
  "lastUpdatedBy" : "routing_configurer",
  "lastUpdatedTime" : 1751539609108,
  "createdBy" : "routing_configurer",
  "createdTime" : 1751539609108,
  "statistics" : {
    "bindingCount" : 0,
    "bytesDropped" : 0,
    "bytesIn" : 0,
    "messagesDropped" : 0,
    "messagesIn" : 0,
    "producerCount" : 0
  }
}
                """;
        ObjectMapper mapper = new ObjectMapper();
        Exchange result = mapper.readValue(exhangeJson, Exchange.class);
        assertThat(result.getName()).isEqualTo("my-exchange");
        assertThat(result.getAlternateBinding()).isNotNull();
        assertThat(result.getAlternateBinding().destination()).isEqualTo("my-dlq");
    }

}
