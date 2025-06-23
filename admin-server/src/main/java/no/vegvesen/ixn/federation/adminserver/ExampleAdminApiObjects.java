package no.vegvesen.ixn.federation.adminserver;

public class ExampleAdminApiObjects {
    public static final String LISTNEIGHBOURSRESPONSE = """
            [
                   {
                       "neighbour_id": 1,
                       "name": "b.bouvetinterchange.eu",
                       "capabilities": {
                           "id": 1,
                           "status": "KNOWN",
                           "capabilities": [],
                           "lastUpdated": 1745824476218,
                           "lastCapabilityExchange": 1745824476217
                       },
                       "neighbourRequestedSubscriptions": {
                           "id": 1,
                           "subscriptions": [],
                           "successfulRequest": null
                       },
                       "ourRequestedSubscriptions": {
                           "subreq_id": 1,
                           "subscriptions": [],
                           "successfulRequest": null
                       },
                       "connectionStatus": "CONNECTED",
                       "lastFailedConnectionAttempt": null,
                       "controlConnection": {
                           "id": 1,
                           "backoffStart": null,
                           "backoffAttempts": 0,
                           "connectionStatus": "CONNECTED",
                           "unreachableTime": null,
                           "lastFailedConnectionAttempt": null
                       },
                       "lastUpdated": 1745321032973,
                       "ignore": false
                   }
               ]
            """;

    public static final String LISTSERVICEPROVIDERSRESPONSE = """
            [
                   {
                       "id": 1,
                       "name": "king_olav.bouvetinterchange.eu",
                       "subscriptions": [
                           {
                               "id": "1",
                               "status": "CREATED",
                               "selector": "originatingCountry = 'NO'",
                               "endpoints": [
                                   {
                                       "id": 1,
                                       "source": "loc-7e132a59-16db-4cf5-a7ca-eb6e3dc50d06",
                                       "host": "a.qpid.bouvetinterchange.eu",
                                       "port": 5671,
                                       "maxBandwidth": null,
                                       "maxMessageRate": null
                                   }
                               ],
                               "lastUpdated": 1745501167148,
                               "consumerCommonName": "a.bouvetinterchange.eu",
                               "connections": [],
                               "description": null,
                               "errorMessage": null
                           }
                       ],
                       "capabilities": [
                           {
                               "id": 1,
                               "createdTimestamp": 1745321315458,
                               "application": {
                                   "messageType": "DENM",
                                   "publisherId": "NO00002",
                                   "publicationId": "NO00002:test",
                                   "originatingCountry": "NO",
                                   "protocolVersion": "DENM:1.2.2",
                                   "quadTree": [
                                       "1203"
                                   ],
                                   "causeCode": [
                                       5
                                   ]
                               },
                               "metadata": {
                                   "shardCount": 1,
                                   "infoUrl": "https://victoria@blomst.stminterchange.com.info.no",
                                   "redirectPolicy": "OPTIONAL",
                                   "maxBandwidth": 0,
                                   "maxMessageRate": 0,
                                   "repetitionInterval": 0
                               },
                               "status": "CREATED",
                               "shards": [
                                   {
                                       "shardId": 2,
                                       "exchangeName": "cap-c917c046-a98e-44c6-bc5d-7ca473509fac-1",
                                       "selector": "(quadTree like '%,1203%') AND (originatingCountry = 'AA') AND (causeCode = 5) AND (publicationId = 'NO00002:test') AND (messageType = 'DENM') AND (publisherId = 'NO00002') AND (protocolVersion = 'DENM:1.2.2')"
                                   }
                               ]
                           }
                       ],
                       "deliveries": [
                          {
                               "id": "1",
                               "status": "CREATED",
                               "selector": "originatingCountry = 'NO'",
                               "endpoints": [
                                   {
                                       "host": "a.qpid.bouvetinterchange.eu",
                                       "port": 5671,
                                       "target": "del-efe7b96f-dd7d-4f68-bf0f-4585689403f6",
                                       "maxBandwidth": null,
                                       "maxMessageRate": null
                                   }
                               ],
                               "lastUpdatedTimestamp": 1745321332926,
                               "description": null
                           }
                       ]
                   }
               ]
            """;

    static final String GETSUBSCRIPTIONCAPABILITYRESPONSE = """
            [ {
              "application" : {
                "messageType" : "DENM",
                "publisherId" : "NO00002",
                "publicationId" : "NOO0002:st5421f2",
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

    static final String GETDELIVERYCAPABILITYRESPONSE = """
            [ {
              "application" : {
                "messageType" : "DENM",
                "publisherId" : "NO00002",
                "publicationId" : "NO00002:tt2125os",
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

    static final String GETPRIVATECHANNELSRESPONSE = """
              [
                  {
                      "id": "e598a3d7-c3fe-4585-ae56-f44826520ddd",
                      "peers": [
                        "pilotinterchange.eu.bouvet.pilotinterchange.eu.per@ulv.no",
                        "pilotinterchange.eu.bouvet.pilotinterchange.eu.bippe@stankelbein.no"
                      ],
                      "status": "CREATED",
                      "description": "Private channel for bouvet and friends",
                      "endpoint": {
                          "host": "bouvet.itsinterchange.eu",
                          "port": 1337,
                          "queueName": "priv-c807bc78-36ee-4cb2-b8aa-8c133644fe4a"
                      },
                      "lastUpdated": 1729840858
                  }
              ]
            """;

    static final String GETPEERPRIVATECHANNELS = """
            [
                {
                    "id": "e598a3d7-c3fe-4585-ae56-f44826520ddd",
                    "owner": "king_olav.bouvetinterchange.eu",
                    "status": "CREATED",
                    "endpoint": {
                        "host": "bouvet.itsinterchange.eu",
                        "port": 1337,
                        "queueName": "priv-86651278-add2-4286-bcdf-bcdb69dc72a1"
                    },
                    "lastUpdated": 1729840858,
                    "description": "private channel between king olav and king gustaf"
                }
            ]
            """;

    static final String GETEXCHANGESRESPONSE = """
            [
                {
                    "id": "8cc9fc08-0517-4d94-8416-0abbc1d1cf25",
                    "name": "cap-2ba65f1c-6999-428a-869b-89aea7c1bd6a",
                    "durable": true,
                    "type": "headers",
                    "bindings": [
                        {
                            "bindingKey": "cap-2ba65f1c-6999-428a-869b-89aea7c1bd6a",
                            "destination": "bi-queue",
                            "arguments": {
                                "x-filter-jms-selector": "(quadTree like '%,1203%') AND (causeCode = 5) AND (publicationId = 'NO00002:test') AND (messageType = 'DENM') AND (publisherId = 'NO00002') AND (protocolVersion = 'DENM:1.2.2') AND (originatingCountry = 'SE')"
                            }
                        }
                    ]
                }]
            """;

    static final String GETEXCHANGEEXISTSSRESPONSE = """
           true
           \s""";

    static final String GETQUEUESRESPONSE =  """
            [
            {
                "id": "575fa3d2-69f7-4126-9a1e-264a8ebab2b4",
                "name": "bi-queue",
                "durable": true,
                "maximumMessageTtl": 900000,
                "ensureNondestructiveConsumers": true
            }
            ]
            """;

    static final String GETQUEUEEXISTSRESPONSE = """
           true
           \s""";

    static final String GETBINDINGEXISTSRESPONSE = """
           true
           \s""";

    static final String GETDELIVERYIDSRESPONSE =  """
            [
                "63993467-de1d-448b-8de5-425eab6ce3e4"
            ]
            """;

    static final String GETDELIVERIESRESPONSE = """
            [ {
              "id" : "63993467-de1d-448b-8de5-425eab6ce3e4",
              "status" : "CREATED",
              "selector" : "originatingCountry='NO'",
              "endpoints" : [ {
                "host" : "bouvet.itsinterchange.eu",
                "port" : 5671,
                "target" : "del-d6728909-0f6e-4a6d-9fee-3e1be3eadd63"
              } ],
              "lastUpdatedTimestamp" : 1726567679,
              "description": "Deliver messages from Norway"
            } ]
            """;

    static final String GETENDPOINTSRESPONSE = """
            [
                   {
                       "localDeliveryEndpointApi": {
                           "host": "a.qpid.bouvetinterchange.eu",
                           "port": 5671,
                           "target": "del-1b39c7b9-f27d-4149-9422-54360333be33"
                       },
                       "exists": true
                   }
               ]
            """;

    static final String GETCAPABILITIESMATCHRESPONSE = """
            {
                     "deliveryId": "5090213f-9c2f-40a0-972c-4a730a5c0317",
                     "capabilityMatchApi": [
                         {
                             "capabilityId": "e49df956-bfb9-4849-bc07-903f30e9c4ec",
                             "shardId": 2,
                             "binding": {
                                 "bindingKey": "del-1b39c7b9-f27d-4149-9422-54360333be33",
                                 "destination": "cap-53bf21ce-0034-46c8-a0e5-60ad5716b6ba",
                                 "arguments": {
                                     "x-filter-jms-selector": "((quadTree like '%,1203%') AND (causeCode = 5) AND (messageType = 'DENM') AND (publicationId = 'NO00002:testqaw') AND (publisherId = 'NO00002') AND (protocolVersion = 'DENM:1.2.2') AND (originatingCountry = 'NO')) AND (originatingCountry = 'NO')"
                                 }
                             },
                             "exists": true
                         }
                     ]
                 }
            """;

    static final String GETCAPABILITYMATCHRESPONSE = """
            {
                "application": {
                    "messageType": "DENM",
                    "publisherId": "NO00002",
                    "publicationId": "NO00002:test",
                    "originatingCountry": "NO",
                    "protocolVersion": "DENM:1.2.2",
                    "quadTree": [
                        "1203"
                    ],
                    "causeCode": [
                        5
                    ]
                },
                "metadata": {
                    "shardCount": 1,
                    "infoUrl": "",
                    "redirectPolicy": "OPTIONAL",
                    "maxBandwidth": 0,
                    "maxMessageRate": 0,
                    "repetitionInterval": 0
                },
                "shardId": [
                    2
                ]
            }
            """;

    static final String GETCAPABILITYSHARDMATCHRESPONSE = """
            {
                  "capabilityShardApi": {
                      "shardId": 2,
                      "exchangeName": "cap-53bf21ce-0034-46c8-a0e5-60ad5716b6ba",
                      "selector": "(quadTree like '%,1203%') AND (causeCode = 5) AND (messageType = 'DENM') AND (publicationId = 'NO00002:test') AND (publisherId = 'NO00002') AND (protocolVersion = 'DENM:1.2.2') AND (originatingCountry = 'NO')"
                  },
                  "exchangeNameExists": true
              }
            """;

    static final String GETSUBSCRIPTIONIDSRESPONSE =  """
            [
                "63993467-de1d-448b-8de5-425eab6ce3e4"
            ]
            """;

}
