package no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels.messages;

import java.util.Map;

public record PrivateTextMessage(String messageText, Map<String, Object> messageProperties) { }
