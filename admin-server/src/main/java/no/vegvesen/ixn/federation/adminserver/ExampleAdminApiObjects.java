package no.vegvesen.ixn.federation.adminserver;

public class ExampleAdminApiObjects {
    public static final String LISTNEIGHBOURS = """
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

    public static final String LISTSERVICEPROVIDERS = """
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
    static final String GETQUEUES =  """
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
}
