package no.vegvesen.ixn.federation.server;

public class ExampleAPIObjects {
    public static final String REQUESTSUBSCRIPTIONSREQUEST = """
            {
              "version" : "1.1NW3",
              "name" : "sp-1",
              "subscriptions" : [ {
                "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
                "consumerCommonName" : "node-1"
              } ]
            }
            """;
    public static final String REQUESTSUBSCRIPTIONSRESPONSE = """
            {
              "version" : "1.1NW3",
              "name" : "sp-1",
              "subscriptions" : [ {
                "id" : "1",
                "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
                "consumerCommonName" : "node-1",
                "path" : "/subscriptions/1",
                "status" : "REQUESTED",
                "lastUpdatedTimestamp" : 1633525221175
              } ]
            }
            """;
    public static final String UPDATECAPABILITIESREQUEST = """
            {
              "version" : "1.1NW3",
              "name" : "sp-1",
             "capabilities" : [ {
                "id" : "1",
                "path" : "/spi-1/capabilities/1",
                "definition" : {
                  "application" : {
                    "messageType" : "DENM",
                    "publisherId" : "NPRA",
                    "publicationId" : "pub-1",
                    "originatingCountry" : "NO",
                    "protocolVersion" : "1.0",
                    "quadTree" : [ "1234" ],
                    "causeCodes" : [ 6 ]
                  },
                  "metadata" : { }
                }
              } ]
            }
            """;
    public static final String UPDATECAPABILITIESRESPONSE = """
            
            {
              "version" : "1.1NW3",
              "name" : "sp-1",
             "capabilities" : [ {
                "id" : "1",
                "path" : "/spi-1/capabilities/1",
                "definition" : {
                  "application" : {
                    "messageType" : "DENM",
                    "publisherId" : "NPRA",
                    "publicationId" : "pub-1",
                    "originatingCountry" : "NO",
                    "protocolVersion" : "1.0",
                    "quadTree" : [ "1234" ],
                    "causeCodes" : [ 6 ]
                  },
                  "metadata" : { }
                }
              } ]
            }
            """;

    public static final String LISTSUBSCRIPTIONSRESPONSE = """
            {
              "version" : "1.1NW3",
              "name" : "sp-1",
              "subscriptions" : [ {
                "id" : "2",
                "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
                "consumerCommonName" : "node-1",
                "path" : "/subscriptions/2",
                "status" : "REQUESTED",
                "lastUpdatedTimestamp" : 1633526284318
              }, {
                "id" : "1",
                "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
                "consumerCommonName" : "node-1",
                "path" : "/subscriptions/1",
                "status" : "REQUESTED",
                "lastUpdatedTimestamp" : 1633526284318
              } ]
            }
            """;

    public static final String POLLSUBSCRIPTIONSRESPONSE = """
            {
              "id" : "1",
              "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
              "consumerCommonName" : "node-1",
              "path" : "/subscriptions/1",
              "status" : "CREATED",
              "lastUpdatedTimestamp" : 0,
               "endpoints" : [ {
                "host" : "amqps://myserver",
                "port" : 5671,
                "source" : "serviceprovider-1",
                "maxBandwidth" : 0,
                "maxMessageRate" : 0
              } ]
            }
            """;

}
