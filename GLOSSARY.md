# Glossary

## Service Provider
Service providers are the users of the system, be it a person or an integrated system. Service Providers use the Onboard API to communicate with the interchange in order to create Subscriptions, Capabilities or Deliveries.

## Capability
Capabilities is like a schema for a data stream. It declares the headers of one or more data streams, and values or range or values for those headers.

## Subscription
Subscriptions are what a client uses to obtain a stream or several streams of data. Subscriptions are matched against Capabilities in the cluster, and creates endpoints for the client to fetch messages from.

## Delivery
Deliveries contains a selector that can match to one or more Capabilities, and declares an endpoint for a client to push messages to. The system then routes messages into datastreams dependent on the Capability they match. 

## MessageType 
A messageType defines the payload of a message, as well as a set of requirement for Capabilities. This means that a message of a specific MessageType must
have a certain payload structure, and a certain set of message properties set, as defined in the Capability.
## PublicationId
A publicationId is a unique ID for a stream of data, also known as publication. This must be a network-wide unique ID.
## Endpoint
An endpoint is the host, port and queue-name to connect to, either to send or to receive messages using AMQPS.

