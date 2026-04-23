# User help

## Using the portal

### Log in to the portal

In a browser, go to [the login page](https://localhost:3000/)

![The login page](../images/login.png)

And log in to the portal with the provided username and password. 

## Service Provider
Service providers are the users of the system, be it a person or an integrated system. Service Providers use the
Onboard API to communicate with the interchange in order to create Subscriptions, Capabilities or Deliveries.

## Capability
Capabilities is like a schema for a data stream. It declares the headers of one or more data streams, and values or
range or values for those headers.

### How to register a Capability?

In order to be able to publish messages on the node, a [Capability](../../GLOSSARY.md#capability) and a [Delivery](../../GLOSSARY.md#delivery) has to be created.
We'll start by adding a Capability.

Click on `My capabilities`, `Add capability`

![Add capability](../images/capabilities.png)

In the form, enter the data as shown (it's important that the data is entered as shown, or else the messages being sent in the later stages may not be routed correctly)

![Capability details](../images/add_capability.png)


| Name                | Value         |
|---------------------|---------------|
| Publisher ID        | NO00000       |
| Publication ID      | NO00000:pub-1 |
| Protocol version    | DENM:1.2.2    |
| Originating country | NO            |
| Message type        | DENM          |
| Cause codes         | 5,6           |
| Quadtree            | 12003         |

Click the button `Create my capability`, and the capability should appear in the list of capabilities.

![The newly created capability in list](../images/capability_list.png)

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
Subscriptions are what a client uses to obtain a stream or several streams of data. Subscriptions are matched against
Capabilities in the cluster, and creates endpoints for the client to fetch messages from. The Capabilities subscribed
to can belong to the Service Provider, other Service Providers on the interchange, or to other interchanges, replicated
over the Improved Interface described in the [specification](https://www.c-roads.eu/).

### How to register a Subscription?
In order to see messages flowing through the system, we can create a [Subscription](../../GLOSSARY.md#subscription) to the data stream, and listen to the associated queue.

In order to create a subscription, click on `Network capabilities` on the right-hand menu, and you should see your capability, as well as capabilities created by
other users both on your instance, or any other interchanges in the cluster. In this demo, however, only your own capability will be listed.

![Network capabilities](../images/network_capabilities.png)

Click on the three dots on the far right, and you should see the details of this capability. Enter a description for your new subscription on the bottom of the
page, and click `Subscribe`

![Subscribe](../images/subscribe.png)

Click `Subscriptions` on the right-hand menu, and you should see the newly created subscription in the list. The status might be `REQUESTED` for a short time, while
the endpoint is being provisioned on the broker, but should end up in a `CREATED` state after a few seconds.

Click on the three dots on the fat right to see the details of the delivery including the [Endpoint](../../GLOSSARY.md#endpoint) to connect to
in order to receive messages.

You have to use the service provider client to receive messages, as described in [Listening to messages](#listening-to-messages).

## Delivery
Deliveries contains a selector that can match to one or more Capabilities thar belong to the same Service Provider,
and declares an endpoint for the a client to push messages to. The system then routes messages into datastreams
dependent on the Capability they match.

### How to register a Delivery
In order to create a [Delivery](../../GLOSSARY.md#delivery), click on the three dots to the far right in the table.

![Dot dot dot](../images/dot_dot_dot.png)

This shows the details of the newly created delivery.

![Capability Details](../images/capability_details.png)

and all the way at the bottom, you can potentially create a description of the the new Delivery, and click `Deliver`

![Deliver](../images/deliver.png)

Click `Deliveries` on the left-hand menu, and you should see a single row in the table. The status might be `REQUESTED` for a short time, while
the endpoint is being provisioned on the broker, but should end up in a `CREATED` state after a few seconds.

Click on the three dots on the fat right to see the details of the delivery including the [Endpoint](../../GLOSSARY.md#endpoint) to connect to
in order to send messages.

You have to use the service provider client to send messages, as described in [Publishing your first message](#publishing-your-first-message). But first, we need to
register a subscription and listen to messages.


