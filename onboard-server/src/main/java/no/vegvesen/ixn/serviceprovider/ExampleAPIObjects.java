package no.vegvesen.ixn.serviceprovider;

public class ExampleAPIObjects {
    public static final String ADDPRIVATECHANNELSREQUEST = """
            {
              "version": "1.0",
              "name": "sp-1",
              "privateChannels": [
                {
                  "peerName": "sp-2"
                }
              ]
            }
            """;
    public static final String ADDCAPABILITIESREQUEST = """
            {
              "name" : "sp-1",
              "version" : "1.0",
              "capabilities" : [ {
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
              } ]
            }
            """;
    public static final String ADDDELIVERIESREQUEST = """
            {
              "version" : "1.0",
              "name" : "sp-1",
              "deliveries" : [ {
                "selector" : "originatingCountry = 'NO' and messageType = 'DENM'"
              } ]
            }
            """;

    public static final String ADDCAPABILITIESRESPONSE = """
            {
              "name" : "sp-1",
              "capabilities" : [ {
                "id" : "1",
                "path" : "/sp-1/capabilities/1",
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
    public static final String ADDSUBSCRIPTIONSRESPONSE = """
            {
              "version" : "1.0",
              "name" : "serviceprovider1",
              "subscriptions" : [ {
                "id" : "2",
                "path" : "/serviceprovider1/subscriptions/2",
                "selector" : "originatingCountry = 'SE' and messageType = 'DENM'",
                "consumerCommonName" : "serviceprovider1",
                "status" : "REQUESTED"
              }, {
                "id" : "1",
                "path" : "/serviceprovider1/subscriptions/1",
                "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
                "consumerCommonName" : "serviceprovider1",
                "status" : "REQUESTED"
              } ]
            }
            """;
    public static final String ADDSUBSCRIPTIONREQUEST = """
            
            {
              "name" : "serviceprovider1",
              "version" : "1.0",
              "subscriptions" : [ {
                "selector" : "originatingCountry = 'SE' and messageType = 'DENM'"
              }, {
                "selector" : "originatingCountry = 'NO' and messageType = 'DENM'"
              } ]
            }
            """;

    public static final String LISTSUBSCRIPTIONSRESPONSE = """
            
            {
              "name" : "serviceprovider1",
              "version" : "1.0",
              "subscriptions" : [ {
                "id" : "1",
                "path" : "/serviceprovider1/subscriptions/1",
                "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
                "consumerCommonName" : "serviceprovider1",
                "status" : "CREATED"
              }, {
                "id" : "2",
                "path" : "/serviceprovider1/subscriptions/2",
                "selector" : "originatingCountry = 'SE' and messageType = 'DENM'",
                "consumerCommonName" : "serviceprovider1",
                "status" : "CREATED"
              } ]
            }

            """;

    public static final String GETSUBSCRIPTIONRESPONSE = """
            {
              "id" : "1",
              "path" : "/serviceprovider1/subscriptions/1",
              "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
              "consumerCommonName" : "serviceprovider1",
              "lastUpdatedTimestamp" : 1684846131664,
              "status" : "CREATED",
              "endpoints" : [ {
                "host" : "amqps://myserver",
                "port" : 5671,
                "source" : "serviceprovider-1",
                "maxBandwidth" : 0,
                "maxMessageRate" : 0
              } ]
            }
            """;


    public static final String LISTPRIVATECHANNELSRESPONSE = """
            {
              "version" : "1.0",
              "name" : "serviceprovider-1",
              "privateChannels" : [ {
                "id" : 2,
                "peerName" : "serviceprovider-2",
                "status" : "CREATED"
              } ]
            }
            """;

    public static final String ADDPRIVATECHANNELSRESPONSE = """
            {
              "version" : "1.0",
              "name" : "serviceprovider-1",
              "privateChannels" : [ {
                "id" : 52,
                "peerName" : "serviceprovider-2",
                "status" : "REQUESTED"
              }, {
                "id" : 53,
                "peerName" : "serviceprovider-3",
                "status" : "REQUESTED"
              } ]
            }
            """;

    public static final String GETPRIVATECHANNELRESPONSE = """
            {
              "id" : 52,
              "peerName" : "serviceprovider-2",
              "serviceProviderName" : "serviceprovider-1",
              "status" : "CREATED",
              "endpoint" : {
                "host" : "example.stminterchange.com",
                "port" : 5671,
                "queueName" : "priv-bf71c182-dfd8-4543-a644-4dddd36751bd"
              }
            }
            """;

    public static final String GETPEERPRIVATECHANNELSRESPONSE = """

            """;
    public static final String LISTDELIVERIESRESPONSE = """
            {
              "version" : "1.0",
              "name" : "sp-1",
              "deliveries" : [ {
                "id" : "1",
                "path" : "/sp-1/deliveries/1",
                "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
                "lastUpdatedTimestamp" : 1684846428079,
                "status" : "CREATED"
              } ]
            }
            """;

    public static final String ADDDELIVERIESRESPONSE = """
            {
              "version" : "1.0",
              "name" : "sp-1",
              "deliveries" : [ {
                "id" : "1",
                "path" : "/sp-1/deliveries/1",
                "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
                "lastUpdatedTimestamp" : 1684934126952,
                "status" : "REQUESTED"
              } ]
            }
            """;

    public static final String GETDELIVERYRESPONSE = """
            {
              "id" : "1",
              "endpoints" : [ {
                "host" : "amqps://sp-1",
                "port" : 5671,
                "target" : "sp1-1",
                "maxBandwidth" : 0,
                "maxMessageRate" : 0
              } ],
              "path" : "/sp-1/deliveries/1",
              "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
              "lastUpdatedTimestamp" : 1684934230041,
              "status" : "CREATED"
            }
            """;
    public static final String LISTCAPABILITIESRESPONSE = """
            {
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
    public static final String GETCAPABILITYRESPONSE = """
            {
              "id" : "1",
              "path" : "/sp-1/capabilities/1",
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
            }
            """;
}
