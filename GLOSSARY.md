# Glossary

## Service Provider
Service providers are the users of the system, be it a person or an integrated system. Service Providers use the Onboard API to communicate with the interchange in order to create Subscriptions, Capabilities or Deliveries.

## Capability
Capabilities is like a schema for a data stream. It declares the headers of one or more data streams, and values or range or values for those headers.
### Creating a new capability
You can create a new capability by click on the "Add capability" button in the capability tab, or by going to this page: [Add capability](https://napcore.npra.io/capabilities/new-user-capability) 
The following fields needs to be filled out:
- `Publisher ID` A two-letter country code (e.g. NO or SE or DK) and a numerical identifier (value between 0 and 16383 including leading zeroes) based on ISO 14816. If you do not have a ISO 14816 identifier you can contact your local interchange provider to get a temporary code to be used.
- `Publication ID` Alpha numeric (Allowed characters (no space): a-z A-Z 0-9 - _ ) identifier of the dataset chosen by you (the publisher of the dataset). Each dataset/publication identifier needs to be unique for the given publisher. PublicationId shall uniquely identify a single capability entry. E.g. npra_DENM_nor1
- `Protocol version` Represent the version of standard used to create the message. E.g. DENM:1.2.1 or DATEX2:3.1
- `Originating country` A two-letter country code (e.g. NO or SE or DK) representing the country where the data originates.
- `Message type` Type of message for the publication. E.g. DENM, DATEX2, IVIM
  - **Additional fields for DATEX2 publications**
    - `Publisher name` This is the identifier for the datex message distributer. Obtained from the nationalIdentifier section of the datex document.
    - `Publication type` Publication type (only one) E.g: SituationPublication or MeasuredDataPublication or VmsPublication
  - **Additional fields for DENM publications**
    - `Cause codes` select the cause codes this publication supports
- `Quadtree` Quadtree tiles representing the coverage area of the publication, comma sparated without spaces with a leading and trailing comma. E.g. ,01223,102332,012322, If you click on the "Show map" button it will open a tool to help you create the tiles. Zoom in and click on the tiles to add them to the list. click on the tile again to remove it. click save to return to the "Add capability" screen.
Once a capability has been created by clicking the "Create my capability" button you should see it in the Capability tab. If you open the capability by clicking on it in the list in the Capability tab, you can use the "Deliver" button to quickly create a [delivery](#delivery) for this specific capability.


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

