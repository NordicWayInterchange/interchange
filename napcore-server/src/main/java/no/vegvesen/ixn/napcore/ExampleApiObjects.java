package no.vegvesen.ixn.napcore;

public class ExampleApiObjects {

    static final String CSR = """
            {
            "csr": LS0tLS1CRUdJTiBDRVJUSUZJQ0FURSBSRVFVRVNULS0tLS0KTUlJQ3ZUQ0NBYVVDQVFBd2VERWdNQjRHQ1NxR1NJYjNEUUVKQVJZUmJHVmhibVJsY2tCdU0zSmtMbXhwWm1VeApNekF4QmdOVkJBTU1LbkJwYkc5MGFXNTBaWEpqYUdGdVoyVXVaWFV1ZEd4bGVDNXpaUzV1TTNKa0xteHBabVV1CmRHVnpkREVTTUJBR0ExVUVDZ3dKVGpOeVpDQk1hV1psTVFzd0NRWURWUVFHRXdKT1REQ0NBU0l3RFFZSktvWkkKaHZjTkFRRUJCUUFEZ2dFUEFEQ0NBUW9DZ2dFQkFMT0FsZ2hVR1dBY0tCS1ZxYXZXQnZsUHZTaURvdVhNV01oZwp0eGZFQ1pWNDlsYWxINjZLTTRlRExDMCtqWjlBWmpaNGg4TEYyQS9tZENkeE9UK1hlc3N0YjViSjZiRVZxTUkxCkp3RG1ITVZUaHlYWVhialIvU1JkaTUzS0xhcENGcjMrWC8wQi9aZ1F5Y29FNzMySVJMN2NlOVprQ2tuWTRyUEwKVkhrTVFLZVZ6RVEraTlaSGNmNkNmbTVMY1FJU0RrODB3MndTelBXcjF1b3JnTFJuYXN2N1pCenAyWXVjTHlkdAovMHBBa3BKeUJnejRra1Ixck4yOE5IeUM0RXQxdzdzOHFjcVZSMDFsRWRxdUlmUjVjMHJMdzdIZFZzT0EwOHR4ClN3NndlcmpMQndYV3FrT2tuUVdCd2lRZ0FXQmtRVE9pZHZaZ3RQaVZFMkNoSGVDQzVDY0NBd0VBQWFBQU1BMEcKQ1NxR1NJYjNEUUVCQ3dVQUE0SUJBUUI0YnZOVG8vVjA3dlcrYnlGRFNoV1pOK05wR3hVbzVXV2N1Nzk1L2lLUgpjK2g5OXUvUnFWY1BPNXkvckJoTVovTUp6VEZZMko2Y3YyK0dZYkJqa1VhTm45MjE2RDhOQU1wRFR6bHEyVFR4CnROYWJ5eE5ubXJYVnZjUXhFZzRDdzlJN3ZXR0VvQzlCN1I1OGxiVGRDNG95M0VBc2J1dHJJT21PRGMvRWR1SnoKMnRSZ3pEeG9OVWRrWWREcFhyRkthTEczRXlTSjFsVEpuN3MzZEl6UCtkdElPOXBjVjJtMUZKQlpsanRWbmpUTgptM2h0UTZtN2JkbXBUTCtTdEEwTUtJZGFmWFE5R2xBS0cxdWNHNCs2RkR3RVhIZmJ4U2drQ3JyQzg0dWt5TGR1CjBJUWs1NUlqTjFnUy84TVZWN0cycm9uN01KSHBNeWFKaWhYMWNGRTd1TjhYCi0tLS0tRU5EIENFUlRJRklDQVRFIFJFUVVFU1QtLS0tLQo=
            }
            """;
    static final String ADDSUBSCRIPTIONRESPONSE = """
            {
              "id" : "3c4c9340-0f75-4bba-93be-8f426e7ce63b",
              "status" : "REQUESTED",
              "selector" : "originatingCountry='NO'",
              "endpoints" : [ ],
              "lastUpdatedTimestamp" : 1726567848,
              "comment": "subscribe to DATEX messages"
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
              "lastUpdatedTimestamp" : 1726567853,
              "comment": "listen to DENM messages"
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
              "lastUpdatedTimestamp" : 1726567853,
              "comment": "listen to DENM messages"
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
              "lastUpdatedTimestamp" : 1726567454,
              "comment": "Delivery for sending datex messages"
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
              "lastUpdatedTimestamp" : 1726567679,
              "comment": "Deliver messages from Norway"
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
              "lastUpdatedTimestamp" : 1726567679,
              "comment": "Deliver messages from Norway"
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
