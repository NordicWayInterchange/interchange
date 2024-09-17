package no.vegvesen.ixn.napcore;

public class ExampleApiObjects {

    static final String ADDSUBSCRIPTIONRESPONSE = """
            {
              "id" : "3c4c9340-0f75-4bba-93be-8f426e7ce63b",
              "status" : "REQUESTED",
              "selector" : "originatingCountry='NO'",
              "endpoints" : [ ],
              "lastUpdatedTimestamp" : 1726567848
            }
            """;

    static final String LISTSUBSCRIPTIONSRESPONSE = """
            [ {
              "id" : "3c4c9340-0f75-4bba-93be-8f426e7ce63b",
              "status" : "CREATED",
              "selector" : "originatingCountry='NO'",
              "endpoints" : [ {
                "host" : "a.bouvetinterchange.eu",
                "port" : 5671,
                "source" : "loc-6f99295e-1982-4686-bc72-338404b48bc6",
                "maxBandwidth" : null,
                "maxMessageRate" : null
              } ],
              "lastUpdatedTimestamp" : 1726567853
            } ]
            """;

    static final String GETSUBSCRIPTIONRESPONSE = """
            {
              "id" : "3c4c9340-0f75-4bba-93be-8f426e7ce63b",
              "status" : "CREATED",
              "selector" : "originatingCountry='NO'",
              "endpoints" : [ {
                "host" : "a.bouvetinterchange.eu",
                "port" : 5671,
                "source" : "loc-6f99295e-1982-4686-bc72-338404b48bc6",
                "maxBandwidth" : null,
                "maxMessageRate" : null
              } ],
              "lastUpdatedTimestamp" : 1726567853
            }
            """;

    static final String GETSUBSCRIPTIONCAPABILITYRESPONSE = """
            [ {
              "application" : {
                "messageType" : "DENM",
                "publisherId" : "NO00002",
                "publicationId" : "NTO002-pub-1222",
                "originatingCountry" : "NO",
                "protocolVersion" : "DENM:2.3",
                "quadTree" : [ "123" ],
                "causeCode" : [ 6 ]
              },
              "metadata" : {
                "shardCount" : 1,
                "infoUrl" : "https://king_charles.info.no",
                "redirectPolicy" : "OPTIONAL",
                "maxBandwidth" : 0,
                "maxMessageRate" : 0,
                "repetitionInterval" : 0
              }
            } ]
            """;

    static final String ADDDELIVERIESRESPONSE = """
            {
              "id" : "63993467-de1d-448b-8de5-425eab6ce3e4",
              "status" : "REQUESTED",
              "selector" : "originatingCountry='NO'",
              "endpoints" : [ ],
              "lastUpdatedTimestamp" : 1726567454
            }
            """;

    static final String GETDELIVERYRESPONSE = """
            {
              "id" : "63993467-de1d-448b-8de5-425eab6ce3e4",
              "status" : "CREATED",
              "selector" : "originatingCountry='NO'",
              "endpoints" : [ {
                "host" : "a.bouvetinterchange.eu",
                "port" : 5671,
                "target" : "del-d6728909-0f6e-4a6d-9fee-3e1be3eadd63"
              } ],
              "lastUpdatedTimestamp" : 1726567679
            }
            """;

    static final String GETDELIVERIESRESPONSE = """
            [ {
              "id" : "63993467-de1d-448b-8de5-425eab6ce3e4",
              "status" : "CREATED",
              "selector" : "originatingCountry='NO'",
              "endpoints" : [ {
                "host" : "a.bouvetinterchange.eu",
                "port" : 5671,
                "target" : "del-d6728909-0f6e-4a6d-9fee-3e1be3eadd63"
              } ],
              "lastUpdatedTimestamp" : 1726567679
            } ]
            """;

    static final String GETDELIVERYCAPABILITYRESPONSE = """
            [ {
              "application" : {
                "messageType" : "DENM",
                "publisherId" : "NO00002",
                "publicationId" : "NTO002-pub-1222",
                "originatingCountry" : "NO",
                "protocolVersion" : "DENM:2.3",
                "quadTree" : [ "1111111111111111111111111111111111111111" ],
                "causeCode" : [ 6 ]
              },
              "metadata" : {
                "shardCount" : 1,
                "infoUrl" : "https://king_charles.info.no",
                "redirectPolicy" : "OPTIONAL",
                "maxBandwidth" : 0,
                "maxMessageRate" : 0,
                "repetitionInterval" : 0
              }
            } ]
            """;

    static final String ADDCAPABILITYRESPONSE = """
            {
              "id" : "e598a3d7-c3fe-4585-ae56-f44826520ddd",
              "application" : {
                "messageType" : "DENM",
                "publisherId" : "NO00002",
                "publicationId" : "NTO002-pub-1222",
                "originatingCountry" : "NO",
                "protocolVersion" : "DENM:2.3",
                "quadTree" : [ "123" ],
                "causeCode" : [ 6 ]
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
            """;

    static final String LISTCAPABILITIESRESPONSE = """
            [ {
              "id" : "e598a3d7-c3fe-4585-ae56-f44826520ddd",
              "application" : {
                "messageType" : "DENM",
                "publisherId" : "NO00002",
                "publicationId" : "NTO002-pub-1222",
                "originatingCountry" : "NO",
                "protocolVersion" : "DENM:2.3",
                "quadTree" : [ "1111111111111111111111111111111111111111" ],
                "causeCode" : [ 6 ]
              },
              "metadata" : {
                "shardCount" : 1,
                "infoUrl" : "https://king_charles.info.no",
                "redirectPolicy" : "OPTIONAL",
                "maxBandwidth" : 0,
                "maxMessageRate" : 0,
                "repetitionInterval" : 0
              }
            } ]
            """;

    static final String GETCAPABILITYRESPONSE = """
            {
              "id" : "e598a3d7-c3fe-4585-ae56-f44826520ddd",
              "application" : {
                "messageType" : "DENM",
                "publisherId" : "NO00002",
                "publicationId" : "NTO002-pub-1222",
                "originatingCountry" : "NO",
                "protocolVersion" : "DENM:2.3",
                "quadTree" : [ "1111111111111111111111111111111111111111" ],
                "causeCode" : [ 6 ]
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
            """;

    static final String GETPUBLICATIONIDSRESPONSE = """
            [
            "NT0002-pub-1222",
            "Publication-1",
            "Publication-2"]
            """;
}
