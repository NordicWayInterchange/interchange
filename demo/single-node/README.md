# Single node cluster

*Note that running the examples in this demo requires a bash-enabled terminal (Linux, Mac or WSL on Windows) and Docker/Docker compose installed.*

The `single-node.yml` file in this folder contains a Docker Compose dummy deployment of a single-node cluster.

## Run the cluster

In a terminal, go to the `demo/single-node` directory, and run `./single-node.sh`.
This will start the node, called `a.interchangedomain.com`.

## Use the onboard client
The script `./a_onboard_client.sh` runs the onboard client, a test client we provide for Service Providers (make link to thesaurus).
Try running `./a_onboard_client.sh --help` to see the different options.

## Register a Capability

In order to be able to publish messages on the node, a Capability (link) and a Delivery (link) has to be created.
We'll start by adding a Capability.

The file `cap_king_olav_denm_no.json` contains the json structure for a request to create a Capability with the publicationId `NO00000-pub-1` with the messageType DENM.

Run the command `./a_onboard_client.sh capabilities add -f cap_king_olav_denm_no.json` to add the capability declared in the json file to the cluster.
The capability is now registered. Check using the command `./a_onboard_client.sh capabilities list`. This lists your capabilities in the system.

## Register a Delivery

We have now declared what type of messages we want to publish, and now we have to create somewhere to actually do the publishing.
In order to do this, we need to create a Delivery.

The file `del_king_olav_denm_no.json` declares a Delivery to the already registered Capability. 
Run the command `./a_onboard_client.sh deliveries add -f del_king_olav_denm_no.json`, and make note of the id of the added delivery.
To get the actual endpoint to deliver messages on, run the command `./a_onboard_client.sh deliveries get <id>` using the id from earlier.
You might have to do this a few times until the delivery has changed from status `REQUESTED` to status `CREATED`.
When the delivery has reached status `CREATED`, the delivery should have one item in the `endpoints` list, and the entry `target` specifies the name
of the actual endpoint to publish messages on.

## Register a Subscription

In order to see messages flowing through the system, we can create a Subscription to the data stream, and listen to the associated queue.
The file `sub_king_olav_denm_no.json` declares a Subscription to listen for messages using the publicationId of `NO00000-pub-1`
Run the command `./a_onboard_client.sh subscriptions add -f sub_king_olav_denm_no.json`, and make note of the id of the added subscription.
To get the actual endpoint do receive messages on, run `./a_onboard_client.sh subscriptions get <id>`, using the id from earlier.
You might have to do this a few times until the subscription has changed from status `REQUESTED` to status `CREATED`.
When the subscription has reached the status `CREATED`, the subscription should have one item in the `endpoints` list, and the entry `target` specifies the name
of the actual endpoint to receive messages from.

## Use the jms client
The script `./a_jms_client.sh` runs the JMS client, a test client we provide for Service Providers for sending and receiving messages (make link to thesaurus).
Try running `./a_jms_client.sh --help` to see the different options.

## Listening to messages

To listen for messages, use the `./a_jms_sink.sh receivemessages <endpoint>` command with the endpoint from the registered subscription. The command will 
block, waiting for messages to arrive. Keep it running, and switch to a new console to publish messages.

## Publish your first message

Publishing messages is done using the command `./a_jms_source.sh sendmessage -f message_king_olav.json`. This will send a single message, defined in the json file used
as an argument. 

You should now see a message logged on the console of the sink command. 
Congratulations! You have now registered a Capability with an associated Delivery, and a Subscription to receive the messages published.

This is all done on one interchange, and with a single user. Of course, this being a clustered system, it is fully possible to send data on one node, 
and receive data on another node.

