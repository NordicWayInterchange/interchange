# Changes for Service Providers' Messaging

From the next release, the way a Service Provider can push data to the platform has changed. We have now introduced the 
concept of Deliveries in order for a Service Provider (SP) to have more fine-grained control over the data produced.

Previously, an SP could register a Capability, and just publish on the special publishing queue, onramp. The onramp
endpoint is now gone, replaced by separate endpoints for a concept called Delivery.

## Delivery

Deliveries are loosely connected to Capabilities, with a selector representing the mapping.
So, if an SP has a Capability that specifies several possible values, like quad trees, it is possible to create separate
Deliveries, say one per quad tree, which in turn will make one delivery endpoint per delivery. This gives an SP the 
possibility of having one connection per delivery, thus having a physical separation of the data. This could be useful if
e.g. the data for each quad tree comes from different backend systems.


## Creating deliveries

The Onboard API has gained a new set of URL endpoints:

### GET /{sp-name/deliveries
Lists the deliveries available for the Service Provider

### POST /{sp-name}/deliveries
Post example:
```
{
  "version" : "1.0",
  "name" : "sp-1",
  "deliveries" : [ {
    "selector" : "messageType = 'DENM'"
  } ]
}
```
This will register a Delivery in the system, and provide an endpoint for the Service Provider client to connect
and publish messages on. As with the other concepts in the Onboard Api, a user must GET the Id of the returned entity
to get the full info, including endpoint details.

### GET /{sp-name}/deliveries/{delivery-id}
```
{
  "id" : "1",
  "endpoints" : [ {
    "host" : "myinterchange",
    "port" : 5671,
    "target" : "sp1-1",
    "selector" : "messageType = 'DENM'",
    "maxBandwidth" : 0,
    "maxMessageRate" : 0
  } ],
  "path" : "/sp-1/deliveries/1",
  "selector" : "messageType = 'DENM'",
  "lastUpdatedTimestamp" : 1660741152111,
  "status" : "CREATED"
}
```
This specifies that the endpoint to publish data on, is `amqps://myinterchange:5671` with endpoint name `sp1-1`
Messages arriving on the endpoint is validated to match both the headers of the Capability it is matched with, and 
the selector of the Delivery.
