<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>User help</title>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/html2pdf.js/0.10.1/html2pdf.bundle.min.js"></script>

 <style>
    .styled-button {
      text-transform: none;
      margin-left: 2px;
      height: 40px;
      width: 200px;
      border: 1px solid #ccc;
      padding-bottom: 5px;
      position: relative;
      background: #444F55;
      color: #f0f1f1;
      cursor: pointer;
    }

    .styled-button:hover::after {
      content: "";
      position: absolute;
      bottom: 0;
      left: 0;
      width: 100%;
      height: 3px;
      background-color: #FF9600;
    }
  </style>

<div
  style="
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 1201;
    background-color: #444F55;
    box-shadow: none;
    padding: 11px 25px;
    color: white;
    font-family: 'Open Sans', sans-serif;
  "
>
<a href="/" style="text-decoration: none; color: inherit; display: flex; align-items: center;">
<img src="/generated/interchange-logo.png" alt="Nordic Way logo" style="width: 40px; margin-right: 8px"/>
  <div
    style="
      font-family: Roboto, Helvetica, Arial, sans-serif;
      font-size: 1.25rem;
      font-weight: 500;
      line-height: 1.6;
      cursor: pointer;
    "
  >
    Interchange Portal
  </div>
</a>
</div>

<div style="padding-top:50px;">

<body style="margin: 40px; background-color: #f0f1f1; font-family: 'Open Sans', sans-serif; color:#444f55; line-height: 1.6;">

<button class="styled-button" onclick="downloadPDF()">Download PDF</button>

  <script>
function sanitizeLinks(container) {
  const links = container.querySelectorAll("a");

  links.forEach(a => {
    const href = a.getAttribute("href");
 if (href && href.startsWith("/generated/")) {
      a.replaceWith(document.createTextNode(a.textContent));
    }
  });
}

function downloadPDF() {
  fetch('/generated/USERHELP.html')  
    .then(res => res.text())
    .then(html => {
      const tempDiv = document.createElement("div");
      tempDiv.innerHTML = html;
        
      sanitizeLinks(tempDiv);

      const options = {
        filename: 'USERHELP.pdf',
        margin: 0.5,
        html2canvas: { scale: 2 },
        jsPDF: { unit: 'in', format: 'a4', orientation: 'portrait' }
      };

      html2pdf().from(tempDiv).set(options).save();
    });
}
</script>

### How to register a Capability
<div style="border-bottom: 2px solid #444f55; width: 100%; margin-top: 8px;"></div>

You can create a new [Capability](/generated/GLOSSARY.html#capability) by clicking on the `Add capability` button in
`My capabilities` tab.

<img src="/generated/capabilities.png" alt="Add capabilities" />

The following fields need to be filled out:

- `Publisher ID` A two-letter country code (e.g. NO or SE or DK) and a numerical identifier (value between 0 and 16383
  including leading zeroes) based on ISO 14816. If you do not have a ISO 14816 identifier you can contact your local
  interchange provider to get a temporary code to be used.
- `Publication ID` Alpha numeric (Allowed characters (no space): a-z A-Z 0-9 - _ ) identifier of the dataset chosen by
  you (the publisher of the dataset). Each dataset/publication identifier needs to be unique for the given publisher.
  PublicationId shall uniquely identify a single capability entry. E.g. npra_DENM_nor1
- `Protocol version` Represent the version of standard used to create the message. E.g. DENM:1.2.1 or DATEX2:3.1
- `Originating country` A two-letter country code (e.g. NO or SE or DK) representing the country where the data
  originates.
- `Message type` Type of message for the publication. E.g. DENM, DATEX2, IVIM
    - **Additional fields for DATEX2 publications**
        - `Publisher name` This is the identifier for the datex message distributer. Obtained from the
          nationalIdentifier section of the datex document.
        - `Publication type` Publication type (only one) E.g: SituationPublication or MeasuredDataPublication or
          VmsPublication
    - **Additional fields for DENM publications**
        - `Cause codes` select the cause codes this publication supports
- `Quadtree` Quadtree tiles representing the coverage area of the publication, comma separated without spaces with a
  leading and trailing comma. E.g. ,01223,102332,012322, If you click on the `Show map` button it will open a tool to
  help you create the tiles. Zoom in and click on the tiles to add them to the list. Click on the tile again to remove
  it.

Following is an example of how the fields can be filled out.

<img src="/generated/add_capability.png" alt="Capability details">

<table style="border-collapse: collapse; width: 50%; border: 1px solid #444f55;">
    <thead>
        <tr style="border-collapse: collapse; width: 50%; border: 1px solid #444f55;">
            <th style="border-collapse: collapse; width: 50%; border: 1px solid #444f55;">Name</th>
            <th style="border-collapse: collapse; width: 50%; border: 1px solid #444f55;">Value</th>
        </tr>
    </thead>
    <tbody>
        <tr style="border-collapse: collapse; width: 50%; border: 1px solid #444f55;">
            <td style="border-collapse: collapse; width: 50%; border: 1px solid #444f55;">Publisher ID</td>
            <td style="border-collapse: collapse; width: 50%; border: 1px solid #444f55;">NO00000</td>
        </tr>
        <tr style="border-collapse: collapse; width: 50%; border: 1px solid #444f55;">
            <td style="border-collapse: collapse; width: 50%; border: 1px solid #444f55;">Publication ID</td>
            <td style="border-collapse: collapse; width: 50%; border: 1px solid #444f55;">NO00000:pub-1</td>
        </tr>
        <tr style="border-collapse: collapse; width: 50%; border: 1px solid #444f55;">
            <td style="border-collapse: collapse; width: 50%; border: 1px solid #444f55;">Protocol version</td>
            <td style="border-collapse: collapse; width: 50%; border: 1px solid #444f55;"> DENM:1.2.2</td>
        </tr>
        <tr style="border-collapse: collapse; width: 50%; border: 1px solid #444f55;">
            <td style="border-collapse: collapse; width: 50%; border: 1px solid #444f55;">Originating country</td>
            <td style="border-collapse: collapse; width: 50%; border: 1px solid #444f55;">NO</td>
        </tr>
        <tr style="border-collapse: collapse; width: 50%; border: 1px solid #444f55;">
            <td style="border-collapse: collapse; width: 50%; border: 1px solid #444f55;">Message type</td>
            <td style="border-collapse: collapse; width: 50%; border: 1px solid #444f55;">DENM</td>
        </tr>
        <tr style="border-collapse: collapse; width: 50%; border: 1px solid #444f55;">
            <td style="border-collapse: collapse; width: 50%; border: 1px solid #444f55;">Cause codes</td>
            <td style="border-collapse: collapse; width: 50%; border: 1px solid #444f55;">5,6</td>
        </tr>
        <tr style="border-collapse: collapse; width: 50%; border: 1px solid #444f55;">
            <td style="border-collapse: collapse; width: 50%; border: 1px solid #444f55;">Quadtree</td>
            <td style="border-collapse: collapse; width: 50%; border: 1px solid #444f55;">12003</td>
        </tr>
    </tbody>
</table>

Once the capability properties have been filled out, click on `Create my capability` button. You should see the newly
created capability in the list of
capabilities, either in `My capabilities` or ` Network capabilities` tab.

<img src="/generated/capability_list.png" alt="Created capability">

Click on the three dots on the far right

<img src="/generated/dot_dot_dot.png" alt="Dot dot dot">

Or simply click on a capability row to view its details in the side window.

<img src="/generated/capability_details.png" alt="Capability details">

In `Network capabilities` tab you can see neighbour capabilities and local capabilities with deliveries.
Capabilities without associated deliveries will not be shown in `Network capabilities` tab, nor reported to other
interchanges in the network. As you can see in the image, capabilities without associated deliveries are marked with an
information icon.

<img src="/generated/capabilities_without_deliveries.png" alt="Capability without delivery">

Please review the instructions on how to register a delivery section. Once the associated delivery's status receive
`CREATED` status label, the information icon next to the capability
row will disappear and the capability will be shown in the list of `Network capabilities`.

### How to register a Delivery
<div style="border-bottom: 2px solid #444f55; width: 100%; margin-top: 8px;"></div>

To create a [Delivery](/generated/GLOSSARY.html#delivery) that will be associated with a capability, go to `Deliveries` tab and click `Create delivery`. Then,
select one of the listed capabilities on the right-hand side. Once you click a capability row, the Publication ID will
be automatically pre-filled in the Publication ID field of the form. You can fill in the other fields in the form or leave them as they are.

The [Quadtree](/generated/GLOSSARY.html#quadtree) field can also be populated by clicking `Show map`. Each area on the map is associated with a
number between 0-3, and by clicking different areas, a comma-separated value will be generated. Click `Save` and this value can then be used as the
`Quadtree` input in the form.

<img src="/generated/show_map.png" alt="quadtree">

While creating a delivery you also have the option to
enable dead letter queue (dlq) for the delivery you are creating which is displayed at bottom of the form. Messages that cannot be delivered are moved to dlq.

<img src="/generated/create_delivery.png" alt="Create_delivery">

Once you switch to **Advanced Mode** a selector based on the values entered in the form will be automatically generated and displayed in the **Selector** section.
You can also write your own selector using the provided cheat sheet. 

<img src="/generated/delivery_cheatsheet.png" alt="Delivery_cheatsheet">

After you click on `Save delivery`, you should see the created delivery in the table. The status might be `REQUESTED` for a short time, while
the endpoint is being provisioned on the broker, but should end up in a `CREATED` state after a few seconds.

Click on the three dots on the far right to see the details of the delivery including the endpoint. 
You also have the option to remove the delivery you just created from the delivery details side panel.

There is also another way of creating a delivery. In `My capabilities` tab, click on a capability row, or click the three
dots on the far right of the capability row. This shows the details of the created capability.

<img src="/generated/capability_details.png" alt="Capability details">

In this side window, at the bottom, you can create a delivery by clicking on `Deliver` button. You can add a description
for the new Delivery. This field is optional. You have also the option to
enable dead letter queue (dlq) for the delivery you are creating. You can also remove
the selected capability from the capability details side window.

<img src="/generated/deliver.png" alt="Deliver">


### How to register a Subscription
<div style="border-bottom: 2px solid #444f55; width: 100%; margin-top: 8px;"></div>

In order to create a [Subscription](/generated/GLOSSARY.html#subscription), click on `Network capabilities` on the left-hand
menu, and you should see your
capability, as well as capabilities created by
other users both on your instance, or any other interchanges in the cluster.

<img src="/generated/network_capabilities.png" alt="Network capabilities">

Click on the three dots on the far right, and you should see the details of this capability. Enter a description for
your new subscription on the bottom of the
page. This field is optional. Then click `Subscribe`

<img src="/generated/subscribe.png" alt="Subscribe">

Click `Subscriptions` on the left-hand menu, and you should see the newly created subscription in the list. The status
might be `REQUESTED` for a short time, while
the endpoint is being provisioned on the broker, but should end up in a `CREATED` state after a few seconds.

<img src="/generated/subscription_list.png" alt="Subscription_list">

Click on the created subscription row or the three dots on the far right side to see the details of the subscription including
the endpoint.

There is also another way of creating a subscription. Go to `Subscriptions` tab and click on `Add subscription`. Then
select one of the listed capabilities on the right-hand side. Once you click a capability row, the Publication ID will
be automatically pre-filled in the Publication ID field of the form. You can fill in the other fields in the form or leave them as they are.
The [Quadtree](/generated/GLOSSARY.html#quadtree) field is the same as described in `Delivery` section.

Once you switch to **Advanced Mode** a selector based on the values entered in the form will be automatically generated and displayed in the **Selector** section.
You can also write your own selector using the provided cheat sheet. Click on
`Save subscription` at the bottom. You will be redirected to the `Subscriptions` section where you can see your newly added subscription.
The status might be `REQUESTED` for a short time, while
the endpoint is being provisioned on the broker, but should end up in a `CREATED` state after a few seconds.

<img src="/generated/cheatsheet.png" alt="Subscription_cheatsheet">

Click on the three dots on the far right to see the details of the subscription including the endpoint. 
You also have the option to remove the subscription just created by clicking `Remove subscription`.

### How to register a Private channel
<div style="border-bottom: 2px solid #444f55; width: 100%; margin-top: 8px;"></div>

In [Private channel](/generated/GLOSSARY.html#private-channel) tab you can display, create and
delete [Peers](/generated/GLOSSARY.html#private-channel-peer) from private channels they own
or remove them from other private channels that are subscribed to them.

A private channel can be added by clicking on the `Create private channel` button where you can add a peer name and
description.
Name of the private channels' peer is optional but description is mandatory. After a private channel is shown up in the
list you can click on the three dots on the far right side to see
the private channel details. The status might be `REQUESTED` for a short time but should end up in a `CREATED` state
after a few seconds.
You can also add or remove peers or even remove the private channel from the side window.
The endpoint for the private channel consists of the hostname, port and the private channels' queue name.

<img src="/generated/privateChannel_details.png" alt="Private channels">

If another [Service provider](/generated/GLOSSARY.html#service-provider) has created a private channel subscribed to yours, it will appear in the
`My private channel subscriptions`
list.

<img src="/generated/privateChannel_subscription.png" alt="My private channel subscriptions">

You can use the [Common name](/generated/GLOSSARY.html#common-name) of the other service provider as the peer name while creating
a private channel.
The `Common name` can be copied from `My common name` section in `Home` tab.

<img src="/generated/commonName.png" alt="Common name">


## Bi-queues
<div style="border-bottom: 2px solid #444f55; width: 100%; margin-top: 8px;"></div>

In [Bi-queues](/generated/GLOSSARY.html#bi-queues) tab you can see the list of bi-queues
per [message type](/generated/GLOSSARY.html#messagetype-).
You can also add or remove access to subscribe to bi-queues.

<img src="/generated/bi-queues.png" alt="Bi-queues">

By clicking on each bi-queue from the list you can see the bi-queue endpoint details.

<img src="/generated/bi-queue_details.png" alt="Bi-queue details">


### How to create a Certificate
<div style="border-bottom: 2px solid #444f55; width: 100%; margin-top: 8px;"></div>

You can generate the key and certificate in the portal in order to generate the key and trust stores for using the
Interchange.

Enter the [Country code](/generated/GLOSSARY.html#originating-country-) and the organisation name, and click `Generate certificate` and download the private key, chain
certificate and root certificate.

<img src="/generated/certificate.png" alt="Certificate">
</body>
</div>
</html>


