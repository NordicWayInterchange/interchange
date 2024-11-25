package no.vegvesen.ixn.serviceprovider;

public class ExampleAPIObjects {

    public static final String ADDPRIVATECHANNELSREQUEST = """
            {
              "version": "1.0",
              "name": "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
              "privateChannels": [
                {
                  "peers": ["pilotinterchange.eu.bouvet.pilotinterchange.eu.bippe@stankelbein.no",
                            "pilotinterchange.eu.bouvet.pilotinterchange.eu.speedy@gonzales.no"],
                  "description": "private channel for bippe stankelbein and speedy gonzales"
                }
              ]
            }
            """;
    public static final String ADD_DATEX_CAPABILITIESREQUEST = """
            {
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
              "version" : "1.0",
              "capabilities" : [ {
                "application" : {
                  "messageType" : "DATEX",
                  "publisherId" : "NO00001",
                  "publicationId" : "NO00001:g5655b2d",
                  "publicationType" : "SituationPublication",
                  "originatingCountry" : "NO",
                  "protocolVersion" : "DATEX2:3.1",
                  "quadTree" : [ "1100023322122", "11011222221123", "1101233333112", "11013222211123", "110122331112", "1100011003222"]
                },
                "metadata" : {
                "shardCount" : 1,
                "infoUrl": "https://www.vegvesen.no/om-oss/om-organisasjonen/apne-data/et-utvalg-apne-data/datex/",
                "redirectPolicy": "OPTIONAL",
                "maxBandwidth": 0,
                "maxMessageRate": 0,
                "repetitionInterval": 0
                }
              } ]
            }
            """;
    public static final String ADD_DENM_CAPABILITIESREQUEST = """
            {
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
              "version" : "1.0",
              "capabilities" : [ {
                "application" : {
                  "messageType" : "DENM",
                  "publisherId" : "NO00001",
                  "publicationId" : "NO00001:b4382a4c",
                  "originatingCountry" : "NO",
                  "protocolVersion" : "DENM:1.2.1",
                  "quadTree" : [ "1100023322122", "11011222221123", "1101233333112", "11013222211123", "110122331112", "1100011003222"],
                  "causeCode" : [ 6 ]
                },
                "metadata" : {
                "shardCount" : 1,
                "infoUrl": "https://www.vegvesen.no/om-oss/om-organisasjonen/apne-data/et-utvalg-apne-data",
                "redirectPolicy": "OPTIONAL",
                "maxBandwidth": 0,
                "maxMessageRate": 0,
                "repetitionInterval": 0
                }
              } ]
            }
            """;

    public static final String ADD_IVIM_CAPABILITIESREQUEST = """
            {
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
              "version" : "1.0",
              "capabilities" : [ {
                "application" : {
                  "messageType" : "IVIM",
                  "publisherId" : "NO00001",
                  "publicationId" : "NO00001:e4171b9d",
                  "originatingCountry" : "NO",
                  "protocolVersion" : "IVIM:1.2.1",
                  "quadTree" : [ "1100023322122", "11011222221123", "1101233333112", "11013222211123", "110122331112", "1100011003222"]
                },
                "metadata" : {
                 "shardCount" : 1,
                "infoUrl": "https://www.vegvesen.no/om-oss/om-organisasjonen/apne-data/et-utvalg-apne-data",
                "redirectPolicy": "OPTIONAL",
                "maxBandwidth": 0,
                "maxMessageRate": 0,
                "repetitionInterval": 0
                }
              } ]
            }
            """;
    public static final String ADD_SPATEM_CAPABILITIESREQUEST = """
            {
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
              "version" : "1.0",
              "capabilities" : [ {
                "application" : {
                  "messageType" : "SPATEM",
                  "publisherId" : "NO00001",
                  "publicationId" : "NO00001:22dddd41",
                  "originatingCountry" : "NO",
                  "protocolVersion" : "SPATEM:1.3.1",
                  "quadTree" : [ "1100023322122", "11011222221123", "1101233333112", "11013222211123", "110122331112", "1100011003222"]
                },
                "metadata" : {
                 "shardCount" : 1,
                "infoUrl": "https://www.vegvesen.no/om-oss/om-organisasjonen/apne-data/et-utvalg-apne-data",
                "redirectPolicy": "OPTIONAL",
                "maxBandwidth": 0,
                "maxMessageRate": 0,
                "repetitionInterval": 0
                }
              } ]
            }
            """;
    public static final String ADD_MAPEM_CAPABILITIESREQUEST = """
            {
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
              "version" : "1.0",
              "capabilities" : [ {
                "application" : {
                  "messageType" : "MAPEM",
                  "publisherId" : "NO00001",
                  "publicationId" : "NO00001:22dddd41",
                  "originatingCountry" : "NO",
                  "protocolVersion" : "MAPEM:1.2.2",
                  "quadTree" : [ "1100023322122", "11011222221123", "1101233333112", "11013222211123", "110122331112", "1100011003222"]
                },
                "metadata" : {
                "shardCount" : 1,
                "infoUrl": "https://www.vegvesen.no/om-oss/om-organisasjonen/apne-data/et-utvalg-apne-data",
                "redirectPolicy": "OPTIONAL",
                "maxBandwidth": 0,
                "maxMessageRate": 0,
                "repetitionInterval": 0
                }
              } ]
            }
            """;
    public static final String ADD_SREM_CAPABILITIESREQUEST = """
            {
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
              "version" : "1.0",
              "capabilities" : [ {
                "application" : {
                  "messageType" : "SREM",
                  "publisherId" : "NO00001",
                  "publicationId" : "NO00001:22dddd41",
                  "originatingCountry" : "NO",
                  "protocolVersion" : "SREM:2.1.1",
                  "quadTree" : [ "1100023322122", "11011222221123", "1101233333112", "11013222211123", "110122331112", "1100011003222"]
                },
                "metadata" : {
                "shardCount" : 1,
                "infoUrl": "https://www.vegvesen.no/om-oss/om-organisasjonen/apne-data/et-utvalg-apne-data",
                "redirectPolicy": "OPTIONAL",
                "maxBandwidth": 0,
                "maxMessageRate": 0,
                "repetitionInterval": 0
                }
              } ]
            }
            """;
    public static final String ADD_SSEM_CAPABILITIESREQUEST = """
            {
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
              "version" : "1.0",
              "capabilities" : [ {
                "application" : {
                  "messageType" : "SSEM",
                  "publisherId" : "NO00001",
                  "publicationId" : "NO00001:22dddd41",
                  "originatingCountry" : "NO",
                  "protocolVersion" : "SSEM:2.1.2",
                  "quadTree" : [ "1100023322122", "11011222221123", "1101233333112", "11013222211123", "110122331112", "1100011003222"]
                },
                "metadata" : {
                "shardCount" : 1,
                "infoUrl": "https://www.vegvesen.no/om-oss/om-organisasjonen/apne-data/et-utvalg-apne-data",
                "redirectPolicy": "OPTIONAL",
                "maxBandwidth": 0,
                "maxMessageRate": 0,
                "repetitionInterval": 0 }
              } ]
            }
            """;
    public static final String ADD_CAM_CAPABILITIESREQUEST = """
            {
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
              "version" : "1.0",
              "capabilities" : [ {
                "application" : {
                  "messageType" : "CAM",
                  "publisherId" : "NO00001",
                  "publicationId" : "NO00001:22dddd41",
                  "originatingCountry" : "NO",
                  "protocolVersion" : "CAM:1.2.1",
                  "quadTree" : [ "1100023322122", "11011222221123", "1101233333112", "11013222211123", "110122331112", "1100011003222"]
                },
                "metadata" : {
                "shardCount" : 1,
                "infoUrl": "https://www.vegvesen.no/om-oss/om-organisasjonen/apne-data/et-utvalg-apne-data",
                "redirectPolicy": "OPTIONAL",
                "maxBandwidth": 0,
                "maxMessageRate": 0,
                "repetitionInterval": 0
                }
              } ]
            }
            """;
    public static final String ADDDELIVERIESREQUEST = """
            {
              "version" : "1.0",
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
              "deliveries" : [ {
                "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
                "description" : "DENM delivery"
              } ]
            }
            """;

    public static final String ADDCAPABILITIESRESPONSE = """
            {
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
              "capabilities" : [ {
                "id" : "550e8400-e29b-41d4-a716-446655440000",
                "path" : "/pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no/capabilities/550e8400-e29b-41d4-a716-446655440000",
                "definition" : {
                  "application" : {
                    "messageType" : "DENM",
                    "publisherId" : "NO00001",
                    "publicationId" : "NO00001:22dddd41",
                    "originatingCountry" : "NO",
                    "protocolVersion" : "DENM:1.2.1",
                    "quadTree" : [ "1100023322122", "11011222221123", "1101233333112", "11013222211123", "110122331112", "1100011003222"],
                    "causeCode" : [ 6 ]
                  },
                "metadata" : {
                "shardCount" : 1,
                "infoUrl": "https://www.vegvesen.no/om-oss/om-organisasjonen/apne-data/et-utvalg-apne-data",
                "redirectPolicy": "OPTIONAL",
                "maxBandwidth": 0,
                "maxMessageRate": 0,
                "repetitionInterval": 0
                }
                }
              } ]
            }
            """;
    public static final String ADDSUBSCRIPTIONSRESPONSE = """
            {
              "version" : "1.0",
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
              "subscriptions" : [ {
                "id" : "550e8400-e29b-41d4-a716-446655440000",
                "path" : "/pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no/subscriptions/550e8400-e29b-41d4-a716-446655440000",
                "selector" : "originatingCountry = 'SE' and messageType = 'DENM'",
                "consumerCommonName" : "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
                "status" : "REQUESTED",
                "description" : "SE subscription"
              }, {
                "id" : "550e8400-e29b-41d4-a716-446655440000",
                "path" : "/pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no/subscriptions/550e8400-e29b-41d4-a716-446655440000",
                "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
                "consumerCommonName" : "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
                "status" : "REQUESTED"
              } ]
            }
            """;
    public static final String ADDSUBSCRIPTIONREQUEST = """
            
            {
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
              "version" : "1.0",
              "subscriptions" : [ {
                "selector" : "originatingCountry = 'SE' and messageType = 'DENM'",
                "description" : "SE subscription"
              }, {
                "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
                "description" : "NO subscription"
              } ]
            }
            """;

    public static final String LISTSUBSCRIPTIONSRESPONSE = """
            
            {
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
              "version" : "1.0",
              "subscriptions" : [ {
                "id" : "550e8400-e29b-41d4-a716-446655440000",
                "path" : "/pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no/subscriptions/550e8400-e29b-41d4-a716-446655440000",
                "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
                "consumerCommonName" : "pilotinterchange.eu.bouvet.pilotinterchange.eu",
                "status" : "CREATED"
              }, {
                "id" : "550e8400-e29b-41d4-a716-446655440000",
                "path" : "/serviceprovider1/subscriptions/550e8400-e29b-41d4-a716-446655440000",
                "selector" : "originatingCountry = 'SE' and messageType = 'DENM'",
                "consumerCommonName" : "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
                "status" : "CREATED",
                "description" : "SE subscription"
              } ]
            }

            """;

    public static final String GETSUBSCRIPTIONRESPONSE = """
            {
                "id": "550e8400-e29b-41d4-a716-446655440000",
                "path": "/pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no/subscriptions/550e8400-e29b-41d4-a716-446655440000",
                "selector": "originatingCountry = 'NO' and messageType = 'DENM'",
                "consumerCommonName": "pilotinterchange.eu.bouvet.pilotinterchange.eu",
                "lastUpdatedTimestamp": 1684846131664,
                "status": "CREATED",
                "description": "DENM subscription",
                "endpoints": [
                    {
                        "host": "pilotinterchange.eu.bouvet.pilotinterchange.eu",
                        "port": 5671,
                        "source": "loc-8f7b4f7c-3286-4fe5-9221-0479006620d1",
                        "maxBandwidth": 0,
                        "maxMessageRate": 0
                    }
                ]
            }
            """;


    public static final String LISTPRIVATECHANNELSRESPONSE = """
            {
              "version" : "1.0",
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
              "privateChannels" : [ {
                "id" : "550e8400-e29b-41d4-a716-446655440000",
                "peers": [
                "pilotinterchange.eu.bouvet.pilotinterchange.eu.bippe@stankelbein.no",
                "pilotinterchange.eu.bouvet.pilotinterchange.eu.speedy@gonzales.no"
                ],
                "status" : "CREATED",
                "description": "private channel for bouvet and friends.",
                "lastUpdated": 1729840858
              } ]
            }
            """;

    public static final String LISTPEERPRIVATECHANNELSRESPONSE = """
            {
                "version": "1.0",
                "name": "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
                "privateChannels": [
                    {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "serviceproviderName": "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
                        "status": "CREATED",
                        "endpoint": {
                        "host": "bouvet.itsinterchange.eu",
                        "port": 5671,
                        "queueName": "priv-0c6e7d5f-bea8-444e-97aa-2da688eaf7b9"
                        },
                        "lastUpdated": 1729840858
                    }
                ]
            }
            """;
    public static final String ADDPRIVATECHANNELSRESPONSE = """
            {
              "version" : "1.0",
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
              "privateChannels" : [ {
                "id" : "550e8400-e29b-41d4-a716-446655440000",
                "peers": [
                "pilotinterchange.eu.bouvet.pilotinterchange.eu.bippe@stankelbein.no",
                "pilotinterchange.eu.bouvet.pilotinterchange.eu.speedy@gonzales.no"
                ],
                "status" : "REQUESTED",
                "description": "private channel for bippe stankelbein and speedy gonzales",
                "lastUpdated": 1729840858
              }]
            }
            """;

    public static final String GETPRIVATECHANNELRESPONSE = """
            {
                "id": "550e8400-e29b-41d4-a716-446655440000",
                "peers": [
                    "serviceprovider-1",
                    "serviceproivder-2"
                ],
                "serviceProviderName": "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
                "status": "CREATED",
                "endpoint": {
                    "host": "bouvet.itsinterchange.eu",
                    "port": 5671,
                    "queueName": "priv-bf71c182-dfd8-4543-a644-4dddd36751bd"
                },
                "lastUpdated": 1729840858
            }
            """;

    public static final String LISTDELIVERIESRESPONSE = """
            {
              "version" : "1.0",
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
              "deliveries" : [ {
                "id" : "550e8400-e29b-41d4-a716-446655440000",
                "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
                "lastUpdatedTimestamp" : 1684846428079,
                "status" : "CREATED",
                "description" : "DENM delivery"
              } ]
            }
            """;

    public static final String ADDDELIVERIESRESPONSE = """
            {
              "version" : "1.0",
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
              "deliveries" : [ {
                "id" : "550e8400-e29b-41d4-a716-446655440000",
                "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
                "lastUpdatedTimestamp" : 1684934126952,
                "status" : "REQUESTED",
                "description" : "DENM delivery"
              } ]
            }
            """;

    public static final String GETDELIVERYRESPONSE = """
            {
              "id" : "550e8400-e29b-41d4-a716-446655440000",
              "endpoints" : [ {
                "host" : "pilotinterchange.eu.bouvet.pilotinterchange.eu",
                "port" : 5671,
                "target" : "del-bfae4c14-566e-4713-aa99-ef24d4928005",
                "maxBandwidth" : 0,
                "maxMessageRate" : 0
              } ],
              "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
              "lastUpdatedTimestamp" : 1684934230041,
              "status" : "CREATED",
              "description" : "DENM delivery"
            }
            """;
    public static final String LISTCAPABILITIESRESPONSE = """
            {
              "name" : "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
              "capabilities" : [ {
                "id" : "550e8400-e29b-41d4-a716-446655440000",
                "path" : "/pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no/capabilities/550e8400-e29b-41d4-a716-446655440000",
                "definition" : {
                  "application" : {
                    "messageType" : "DENM",
                    "publisherId" : "NO00001",
                    "publicationId" : "NO00001:22dddd41",
                    "originatingCountry" : "NO",
                    "protocolVersion" : "DENM:1.2.1",
                    "quadTree" : [ "1100023322122", "11011222221123", "1101233333112", "11013222211123", "110122331112", "1100011003222"],
                    "causeCode" : [ 6 ]
                  },
                "metadata" : {
                "shardCount" : 1,
                "infoUrl": "https://www.vegvesen.no/om-oss/om-organisasjonen/apne-data/et-utvalg-apne-data",
                "redirectPolicy": "OPTIONAL",
                "maxBandwidth": 0,
                "maxMessageRate": 0,
                "repetitionInterval": 0
                }
                }
              } ]
            }
            """;
    public static final String GETCAPABILITYRESPONSE = """
            {
              "id" : "550e8400-e29b-41d4-a716-446655440000",
              "path" : "/pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no/capabilities/550e8400-e29b-41d4-a716-446655440000",
              "definition" : {
                "application" : {
                  "messageType" : "DENM",
                  "publisherId" : "NO00001",
                  "publicationId" : "NO00001:22dddd41",
                  "originatingCountry" : "NO",
                  "protocolVersion" : "DENM:1.2.1",
                  "quadTree" : [ "1100023322122", "11011222221123", "1101233333112", "11013222211123", "110122331112", "1100011003222"],
                  "causeCode" : [ 6 ]
                },
                "metadata" : {
                "shardCount" : 1,
                "infoUrl": "https://www.vegvesen.no/om-oss/om-organisasjonen/apne-data/et-utvalg-apne-data",
                "redirectPolicy": "OPTIONAL",
                "maxBandwidth": 0,
                "maxMessageRate": 0,
                "repetitionInterval": 0
                }
              }
            }
            """;
}
