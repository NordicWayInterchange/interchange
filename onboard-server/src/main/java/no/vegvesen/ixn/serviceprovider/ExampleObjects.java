package no.vegvesen.ixn.serviceprovider;

public class ExampleObjects {
    public static final String ADDPRIVATECHANNELSREQUEST = """
            {
              "version": "string",
              "name": "string",
              "privateChannels": [
                {
                  "peerName": "string"
                }
              ]
            }
                        
            """;
    public static final String ADDCAPABILITIESREQUEST = """
            {
              "name": "string",
              "version": "string",
              "capabilities": [
                {
                  "application": {
                    "messageType": "string",
                    "originatingCountry": "string",
                    "protocolVersion": "string",
                    "quadTree": [
                      "string"
                    ]
                  },
                  "metadata": {
                    "shardCount": 0,
                    "infoUrl": "string",
                    "redirectPolicy": "OPTIONAL",
                    "maxBandwidth": 0,
                    "maxMessageRate": 0,
                    "repetitionInterval": 0
                  }
                }
              ]
            }
            """;
    public static final String ADDDELIVERIESREQUEST = """
            
            """;
}
