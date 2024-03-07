# Glossary

## Service Provider
Service providers are the users of the system, be it a person or an integrated system. Service Providers use the Onboard API to communicate with the interchange in order to create Subscriptions, Capabilities or Deliveries.

## Capability
Capabilities is like a schema for a data stream. It declares the headers of one or more data streams, and values or range or values for those headers.

## Subscription
Subscriptions are what a client uses to obtain a stream or several streams of data. Subscriptions are matched against Capabilities in the cluster, and creates endpoints for the client to fetch messages from.
## Delivery
Deliveries contains a selector that can match to one or more Capabilities, and declares an endpoint for a client to push messages to. The system then routes messages into datastreams dependent on the Capability they match. 


