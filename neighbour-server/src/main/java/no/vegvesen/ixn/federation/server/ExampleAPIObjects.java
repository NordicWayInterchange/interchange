package no.vegvesen.ixn.federation.server;

public class ExampleAPIObjects {
    public static final String REQUESTSUBSCRIPTIONSREQUEST = """
            {
              "version" : "1.2",
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
              "subscriptions" : [ {
                "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
                "consumerCommonName" : "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no"
              } ]
            }
            """;
    public static final String REQUESTSUBSCRIPTIONSRESPONSE = """
            {
              "version" : "1.2",
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu",
              "subscriptions" : [ {
                "id" : "ddd0c289-ef27-4d0f-9c72-4717513d007f",
                "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
                "consumerCommonName" : "pilotinterchange.eu.bouvet.pilotinterchange.eu",
                "path" : "/subscriptions/ddd0c289-ef27-4d0f-9c72-4717513d007f",
                "status" : "REQUESTED",
                "lastUpdatedTimestamp" : 1633525221175
              } ]
            }
            """;
    public static final String DENM_CAPABILITY_REQUEST = """
            {
              "version" : "2.0",
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu",
             "capabilities" : [ {
                "id" : "ddd0c289-ef27-4d0f-9c72-4717513d007f",
                "path" : "/pilotinterchange.eu.bouvet.pilotinterchange.eu/capabilities/ddd0c289-ef27-4d0f-9c72-4717513d007f",
                  "application" : {
                    "messageType" : "DENM",
                    "publisherId" : "NO00001",
                    "publicationId" : "NO00001:25kkd7g2",
                    "originatingCountry" : "NO",
                    "protocolVersion" : "DENM:1.2.1",
                    "quadTree" : [ "1100023322122", "11011222221123", "1101233333112", "11013222211123", "110122331112", "1100011003222"],
                    "causeCode" : [ 6 ]
                  },
                  "metadata" : {
                 "shardCount" : 1,
                "infoUrl": "https://pub.info.no",
                "redirectPolicy": "OPTIONAL",
                "maxBandwidth": 0,
                "maxMessageRate": 0,
                "repetitionInterval": 0
                }
              } ]
            }
            """;
    public static final String DATEX_CAPABILITY_REQUEST = """
            {
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu",
              "version" : "2.0",
              "capabilities" : [ {
                "application" : {
                  "messageType" : "DATEX2",
                  "publisherId" : "NO00001",
                  "publicationId" : "NO00001:dk224512f",
                  "publicationType" : "SituationPublication",
                  "originatingCountry" : "NO",
                  "protocolVersion" : "DATEX2:3.1",
                  "quadTree" : [ "1100023322122", "11011222221123", "1101233333112", "11013222211123", "110122331112", "1100011003222"]
                },
                "metadata" : {
                "shardCount" : 1,
                "infoUrl": "https://pub.info.no",
                "redirectPolicy": "OPTIONAL",
                "maxBandwidth": 0,
                "maxMessageRate": 0,
                "repetitionInterval": 0
                }
              } ]
            }
            """;
    public static final String IVIM_CAPABILITY_REQUEST = """
           {
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu",
              "version" : "2.0",
              "capabilities" : [ {
                "application" : {
                  "messageType" : "IVIM",
                  "publisherId" : "NO00001",
                  "publicationId" : "NO00001:s21924dk",
                  "originatingCountry" : "NO",
                  "protocolVersion" : "IVIM:1.0",
                  "quadTree" : [ "1100023322122", "11011222221123", "1101233333112", "11013222211123", "110122331112", "1100011003222"]
                },
                "metadata" : {
                "shardCount" : 1,
                "infoUrl": "https://pub.info.no",
                "redirectPolicy": "OPTIONAL",
                "maxBandwidth": 0,
                "maxMessageRate": 0,
                "repetitionInterval": 0
                }
              } ]
            }
            """;
    public static final String SPATEM_CAPABILITY_REQUEST = """
            {
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu",
              "version" : "2.0",
              "capabilities" : [ {
                "application" : {
                  "messageType" : "SPATEM",
                  "publisherId" : "NO00001",
                  "publicationId" : "NO00001:s21924dk",
                  "originatingCountry" : "NO",
                  "protocolVersion" : "SPATEM:1.0",
                   "quadTree" : [ "1100023322122", "11011222221123", "1101233333112", "11013222211123", "110122331112", "1100011003222"]
                },
                "metadata" : {
                 "shardCount" : 1,
                "infoUrl": "https://pub.info.no",
                "redirectPolicy": "OPTIONAL",
                "maxBandwidth": 0,
                "maxMessageRate": 0,
                "repetitionInterval": 0
                }
              } ]
            }
            """;
    public static final String MAPEM_CAPABILITY_REQUEST = """
            {
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu",
              "version" : "2.0",
              "capabilities" : [ {
                "application" : {
                  "messageType" : "MAPEM",
                  "publisherId" : "NO00001",
                  "publicationId" : "NO00001:s21924dk",
                  "originatingCountry" : "NO",
                  "protocolVersion" : "MAPEM:1.0",
                  "quadTree" : [ "1100023322122", "11011222221123", "1101233333112", "11013222211123", "110122331112", "1100011003222"]
                },
                "metadata" : {
                "shardCount" : 1,
                "infoUrl": "https://pub.info.no",
                "redirectPolicy": "OPTIONAL",
                "maxBandwidth": 0,
                "maxMessageRate": 0,
                "repetitionInterval": 0
                }
              } ]
            }
            """;
    public static final String SREM_CAPABILITY_REQUEST = """
            {
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu",
              "version" : "2.0",
              "capabilities" : [ {
                "application" : {
                  "messageType" : "SREM",
                  "publisherId" : "NO00001",
                  "publicationId" : "NO00001:s21924dk",
                  "originatingCountry" : "NO",
                  "protocolVersion" : "SREM:1.2",
                  "quadTree" : [ "1100023322122", "11011222221123", "1101233333112", "11013222211123", "110122331112", "1100011003222"]
                },
                "metadata" : { 
                "shardCount" : 1,
                "infoUrl": "https://pub.info.no",
                "redirectPolicy": "OPTIONAL",
                "maxBandwidth": 0,
                "maxMessageRate": 0,
                "repetitionInterval": 0
                }
              } ]
            }
            """;
    public static final String SSEM_CAPABILITY_REQUEST = """
            {
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu",
              "version" : "2.0",
              "capabilities" : [ {
                "application" : {
                  "messageType" : "SSEM",
                  "publisherId" : "NO00001",
                  "publicationId" : "NO00001:s21924dk",
                  "originatingCountry" : "NO",
                  "protocolVersion" : "SSEM:1.0",
                  "quadTree" : [ "1100023322122", "11011222221123", "1101233333112", "11013222211123", "110122331112", "1100011003222"]
                },
                "metadata" : {
                "shardCount" : 1,
                "infoUrl": "https://pub.info.no",
                "redirectPolicy": "OPTIONAL",
                "maxBandwidth": 0,
                "maxMessageRate": 0,
                "repetitionInterval": 0 }
              } ]
            }
            """;
    public static final String CAM_CAPABILITY_REQUEST = """
            {
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu",
              "version" : "2.0",
              "capabilities" : [ {
                "application" : {
                  "messageType" : "CAM",
                  "publisherId" : "NO00001",
                  "publicationId" : "NO00001:s21924dk",
                  "originatingCountry" : "NO",
                  "protocolVersion" : "1.0",
                 "quadTree" : [ "1100023322122", "11011222221123", "1101233333112", "11013222211123", "110122331112", "1100011003222"]
                },
                "metadata" : {
                "shardCount" : 1,
                "infoUrl": "https://pub.info.no",
                "redirectPolicy": "OPTIONAL",
                "maxBandwidth": 0,
                "maxMessageRate": 0,
                "repetitionInterval": 0
                }
              } ]
            }
            """;
    public static final String UPDATECAPABILITIESRESPONSE = """  
            {
              "version" : "2.0",
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu",
             "capabilities" : [ {
                "id" : "d4626s9b-8583-4739-ae62-bd1dbc97154a",
                "path" : "/pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no/capabilities/d4626s9b-8583-4739-ae62-bd1dbc97154a",
                  "application" : {
                    "messageType" : "DENM",
                    "publisherId" : "NO00001",
                    "publicationId" : "NO00001:s21924dk",
                    "originatingCountry" : "NO",
                    "protocolVersion" : "DENM:2.2.1",
                    "quadTree" : [ "1100023322122", "11011222221123", "1101233333112", "11013222211123", "110122331112", "1100011003222"],
                    "causeCode" : [ 6 ]
                  },
                  "metadata" : { }
              } ]
            }
            """;

    public static final String LISTSUBSCRIPTIONSRESPONSE = """
            {
              "version" : "1.2",
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu",
              "subscriptions" : [ {
                "id" : "d4626s9b-8583-4739-ae62-bd1dbc97154a",
                "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
                "consumerCommonName" : "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
                "path" : "/subscriptions/d4626s9b-8583-4739-ae62-bd1dbc97154a",
                "status" : "REQUESTED",
                "lastUpdatedTimestamp" : 1633526284318
              }, {
                "id" : "d4626s9b-8583-4739-ae62-bd1dbc97154b",
                "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
                "consumerCommonName" : "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
                "path" : "/subscriptions/d4626s9b-8583-4739-ae62-bd1dbc97154b",
                "status" : "REQUESTED",
                "lastUpdatedTimestamp" : 1633526284319
              } ]
            }
            """;

    public static final String POLLSUBSCRIPTIONSRESPONSE = """
            {
              "version" : "1.2",
              "id" : "d4626s9b-8583-4739-ae62-bd1dbc97154a",
              "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
              "consumerCommonName" : "pilotinterchange.eu.bouvet.pilotinterchange.eu",
              "path" : "/subscriptions/d4626s9b-8583-4739-ae62-bd1dbc97154a",
              "status" : "CREATED",
              "lastUpdatedTimestamp" : 0,
               "endpoints" : [ {
                "host" : "pilotinterchange.eu.bouvet.pilotinterchange.eu",
                "port" : 5671,
                "source" : "loc-8f7b4f7c-3286-4fe5-9221-0479006620d1",
                "maxBandwidth" : 0,
                "maxMessageRate" : 0
              } ]
            }
            """;

}
