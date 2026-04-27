# Glossary

## Service Provider
A Service provider is a user of the system, be it a person or an integrated system. Service Providers use the 
Onboard API to communicate with the interchange in order to create Subscriptions, Capabilities or Deliveries.

## Capability
A Capability is like a schema for a data stream. It declares what the headers of messages on one or more data streams 
must contain, and values or range or values for those headers.

## Subscription
A Subscription is what a client uses to obtain a stream or several streams of data. A Subscription contains a selector,
and are matched against Capabilities in the cluster. Based on this matching, endpoints are created for the client to 
fetch messages from. The Capabilities subscribed to can belong to the Service Provider, other Service Providers on the 
interchange, or to other interchanges, replicated over the Improved Interface described in the [specification](https://www.c-roads.eu/). 

## Delivery
A Delivery contains a selector that can match to one or more Capabilities that belong to the same Service Provider, 
and declares an endpoint for the client to push messages to. The system then routes messages into data streams 
dependent on the Capability they match.

## Endpoint
An Endpoint describes the host, port and queue-name to connect to, either for sending or receiving messages using AMQPS.

## Quadtree 
A quadtree tile is a string representing repeated splitting the world map into 4 sections (0-3), each number in the 
string representing ever smaller sections as read from left to right. This gives a compact and convenient way to 
reference areas on a map without specific geometry operations.

## Publisher ID 
A two-letter country code (e.g. NO or SE or DK) and a numerical identifier (value between 0 and 16383 including leading 
zeroes) based on ISO 14816. If you do not have a ISO 14816 identifier you can contact your local interchange provider 
to get a temporary code to be used.

## Publication ID
Alphanumeric (Allowed characters (no space): a-z A-Z 0-9 - _ ) identifier of the dataset chosen by you (the publisher of
the dataset). Each dataset/publication identifier needs to be unique for the given publisher. PublicationId uniquely 
identifies a single capability entry. E.g. npra_DENM_nor1

## Protocol version 
Represents the version of the message payload standard used. E.g. DENM:1.2.1 or DATEX2:3.1

## Originating country 
A two-letter country code (e.g. NO or SE or DK) representing the country where the data originates.

## MessageType 
A messageType defines the payload of a message, as well as a set of requirement for Capabilities. This means that a 
message of a specific MessageType must have a certain payload structure, and a certain set of message properties set, as
defined in the Capability.
E.g. DENM, DATEX2, IVIM

## Publisher name
Only applies for DATEX2 publications. The identifier for the datex message distributer. Obtained from the 
nationalIdentifier section of the datex document.

## Publication type
Only applies for DATEX2 publications. Publication type (only one) E.g: SituationPublication or MeasuredDataPublication
or VmsPublication

## Cause code
Only applies for DENM publications.

## Private channels
A Private channel is a point-to point communication channel between Service Providers on the same interchange. Private 
Channels do not enforce the constraints of Message Types like Capability-based streams do. 

## Bi-queues
A Bi-queue provides an unfiltered view of all data for a message type produced on a single interchange. A BI-queue 
allows Service Providers to connect and receive messages over the Basic Interface as described in the [specification](https://www.c-roads.eu/).


