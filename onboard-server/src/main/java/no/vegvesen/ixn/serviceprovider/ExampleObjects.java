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

    public static final String ADDCAPABILITIESRESPONSE = """
            {
              "name" : "king_olav.bouvetinterchange.eu",
              "capabilities" : [ {
                "id" : "1",
                "path" : "/king_olav.bouvetinterchange.eu/capabilities/1",
                "definition" : {
                  "application" : {
                    "messageType" : "DENM",
                    "publisherId" : "NO00002",
                    "publicationId" : "NO00002-pub-1",
                    "originatingCountry" : "NO",
                    "protocolVersion" : "DENM:1.2.2",
                    "quadTree" : [ "12004" ],
                    "causeCodes" : [ 6 ]
                  },
                  "metadata" : {
                    "shardCount" : 1,
                    "infoUrl" : "https://king_charles.info.no",
                    "redirectPolicy" : "OPTIONAL",
                    "maxBandwidth" : 0,
                    "maxMessageRate" : 0,
                    "repetitionInterval" : 0
                  }
                }
              } ]
            }
            """;
    public static final String LISTCAPABILITIESRESPONSE = """
            {
              "name" : "king_olav.bouvetinterchange.eu",
              "capabilities" : [ {
                "id" : "1",
                "path" : "/king_olav.bouvetinterchange.eu/capabilities/1",
                "definition" : {
                  "application" : {
                    "messageType" : "DENM",
                    "publisherId" : "NO00002",
                    "publicationId" : "NO00002-pub-1",
                    "originatingCountry" : "NO",
                    "protocolVersion" : "DENM:1.2.2",
                    "quadTree" : [ "12004" ],
                    "causeCodes" : [ 6 ]
                  },
                  "metadata" : {
                    "shardCount" : 1,
                    "infoUrl" : "https://king_charles.info.no",
                    "redirectPolicy" : "OPTIONAL",
                    "maxBandwidth" : 0,
                    "maxMessageRate" : 0,
                    "repetitionInterval" : 0
                  }
                }
              } ]
            }
            """;
}
