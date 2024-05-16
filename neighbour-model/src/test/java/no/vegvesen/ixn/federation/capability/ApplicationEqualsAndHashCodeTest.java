package no.vegvesen.ixn.federation.capability;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitiesSplitApi;
import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.capability.*;
import no.vegvesen.ixn.federation.transformer.CapabilitiesTransformer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationEqualsAndHashCodeTest {

    String inputJson = """
            {
  "name" : "neighbourUpdateCaps",
  "version" : "1.0",
  "capabilities" : [ {
    "application" : {
      "messageType" : "DENM",
      "publisherId" : "NO00000",
      "publicationId" : "NO00000-DENM",
      "originatingCountry" : "NO",
      "protocolVersion" : "DENM:1.2.2",
      "quadTree" : [ "12004" ],
      "causeCode" : [ 6 ]
    },
    "metadata" : {
      "shardCount" : 1,
      "infoUrl" : "info.com",
      "redirectPolicy" : "OPTIONAL",
      "maxBandwidth" : 0,
      "maxMessageRate" : 0,
      "repetitionInterval" : 0
    }
  }, {
    "application" : {
      "messageType" : "SSEM",
      "publisherId" : "NO00000",
      "publicationId" : "NO00000-SSEM",
      "originatingCountry" : "NO",
      "protocolVersion" : "SSEM",
      "quadTree" : [ "12004" ]
    },
    "metadata" : {
      "shardCount" : 1,
      "infoUrl" : "info.com",
      "redirectPolicy" : "OPTIONAL",
      "maxBandwidth" : 0,
      "maxMessageRate" : 0,
      "repetitionInterval" : 0
    }
  }, {
    "application" : {
      "messageType" : "SPATEM",
      "publisherId" : "NO00000",
      "publicationId" : "NO00000-SPATEM",
      "originatingCountry" : "NO",
      "protocolVersion" : "SPATEM",
      "quadTree" : [ "12004" ]
    },
    "metadata" : {
      "shardCount" : 1,
      "infoUrl" : "info.com",
      "redirectPolicy" : "OPTIONAL",
      "maxBandwidth" : 0,
      "maxMessageRate" : 0,
      "repetitionInterval" : 0
    }
  }, {
    "application" : {
      "messageType" : "MAPEM",
      "publisherId" : "NO00000",
      "publicationId" : "NO00000-MAPEM",
      "originatingCountry" : "NO",
      "protocolVersion" : "MAPEM",
      "quadTree" : [ "12004" ]
    },
    "metadata" : {
      "shardCount" : 1,
      "infoUrl" : "info.com",
      "redirectPolicy" : "OPTIONAL",
      "maxBandwidth" : 0,
      "maxMessageRate" : 0,
      "repetitionInterval" : 0
    }
  },  {
    "application" : {
      "messageType" : "IVIM",
      "publisherId" : "NO00000",
      "publicationId" : "NO00000-IVIM",
      "originatingCountry" : "NO",
      "protocolVersion" : "IVI:1.2",
      "quadTree" : [ "12004" ]
    },
    "metadata" : {
      "shardCount" : 1,
      "infoUrl" : "info.com",
      "redirectPolicy" : "OPTIONAL",
      "maxBandwidth" : 0,
      "maxMessageRate" : 0,
      "repetitionInterval" : 0
    }
  }, {
    "application" : {
      "messageType" : "CAM",
      "publisherId" : "NO00000",
      "publicationId" : "NO00000-CAM",
      "originatingCountry" : "NO",
      "protocolVersion" : "CAM",
      "quadTree" : [ "12004" ]
    },
    "metadata" : {
      "shardCount" : 1,
      "infoUrl" : "info.com",
      "redirectPolicy" : "OPTIONAL",
      "maxBandwidth" : 0,
      "maxMessageRate" : 0,
      "repetitionInterval" : 0
    }
  } ]
}
            """;

    @Test
    public void testCam() {
        CapabilitySplit cap = new CapabilitySplit(
                1,
                new CamApplication(
                        "pub-1",
                        "pub-1-1",
                        "no",
                        "1.0",
                        List.of()
                ),
                new Metadata(

                )
        );
        CapabilitySplit cap2 = new CapabilitySplit(
                new CamApplication(
                        "pub2",
                        "pub-1-1",
                        "no",
                        "1.0",
                        List.of()
                ),
                new Metadata()
        );
        CapabilitySplit cap3 = new CapabilitySplit(
                new CamApplication(
                        "pub-1",
                        "pub-1-1",
                        "no",
                        "1.0",
                        List.of()
                ),
                new Metadata()
        );
        cap3.setStatus(CapabilityStatus.TEAR_DOWN);

        assertThat(cap).isNotEqualTo(cap2);
        assertThat(cap).isEqualTo(cap3);
        assertThat(cap3).isEqualTo(cap);
        assertThat(cap.hashCode()).isEqualTo(cap3.hashCode());
    }

    @Test
    public void testDatex() {
        CapabilitySplit cap1   = new CapabilitySplit(
                1,
                new DatexApplication(
                        "pub-123",
                        "pub-123-123",
                        "NO",
                        "1.0",
                        List.of("123"),
                        "pubType",
                        "publishername"
                ),
                new Metadata()
        );

        CapabilitySplit cap2   = new CapabilitySplit(
                new DatexApplication(
                        "pub-124",
                        "pub-124-123",
                        "NO",
                        "1.0",
                        List.of("122"),
                        "pubType",
                        "publishername"
                ),
                new Metadata()
        );
        CapabilitySplit cap3   = new CapabilitySplit(
                new DatexApplication(
                        "pub-123",
                        "pub-123-123",
                        "NO",
                        "1.0",
                        List.of("123"),
                        "pubType",
                        "publishername"
                ),
                new Metadata()
        );
        assertThat(cap1).isNotEqualTo(cap2);
        assertThat(cap1).isEqualTo(cap3);
        assertThat(cap3).isEqualTo(cap1);
        assertThat(cap1.hashCode()).isEqualTo(cap3.hashCode());
    }

    @Test
    public void testDenm() {
        CapabilitySplit cap1   = new CapabilitySplit(
                1,
                new DenmApplication(
                        1,
                        "pub-123",
                        "pub-123-123",
                        "NO",
                        "1.0",
                        List.of("123,213"),
                        List.of(2,3)
                ),
                new Metadata()
        );
        CapabilitySplit cap2   = new CapabilitySplit(
                1,
                new DenmApplication(
                        1,
                        "pub-123",
                        "pub-123-123",
                        "NO",
                        "1.0",
                        List.of("123,213"),
                        List.of(3)
                ),
                new Metadata()
        );
        CapabilitySplit cap3   = new CapabilitySplit(
                new DenmApplication(
                        "pub-123",
                        "pub-123-123",
                        "NO",
                        "1.0",
                        List.of("123,213"),
                        List.of(2,3)
                ),
                new Metadata()
        );
        assertThat(cap1).isNotEqualTo(cap2);
        assertThat(cap1).isEqualTo(cap3);
        assertThat(cap3).isEqualTo(cap1);
        assertThat(cap1.hashCode()).isEqualTo(cap3.hashCode());

    }

    @Test
    public void testFromJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        CapabilitiesSplitApi capabilitySplitApi = mapper.readValue(inputJson, CapabilitiesSplitApi.class);
        CapabilitiesTransformer transformer = new CapabilitiesTransformer();
        Capabilities capabilities = transformer.capabilitiesApiToCapabilities(capabilitySplitApi);
        AtomicInteger i = new AtomicInteger(0);
        capabilities.getCapabilities().forEach(capability -> capability.setId(i.incrementAndGet()));
        Capabilities capabilities2 = transformer.capabilitiesApiToCapabilities(capabilitySplitApi);
        assertThat(capabilities.getCapabilities()).isEqualTo(capabilities2.getCapabilities());
        int size = capabilities.getCapabilities().size();
        capabilities.replaceCapabilities(capabilities2.getCapabilities());
        assertThat(capabilities.getCapabilities()).hasSize(size);


    }
}
