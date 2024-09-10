package no.vegvesen.ixn.federation.serviceproviderclient.command.jms;


import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClientApplication;
import picocli.CommandLine;
import picocli.CommandLine.*;

import java.nio.file.Path;

@Command(name="messages",
        description = "Send and receive messages",
        subcommands = {
                CountMessages.class,
                DrainMessages.class,
                ReceiveMessages.class,
                SendMessage.class,
                SendPredefinedMessage.class
        },
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
        )
public class MessagesCommand implements JmsTopCommand {

    @ParentCommand
    ServiceProviderClientApplication parent;

    public ServiceProviderClientApplication getParent() {
        return parent;
    }

    @Override
    public String getUrl() {
        return parent.getUrl();
    }

    @Override
    public Path getKeystorePath() {
        return parent.getKeystorePath();
    }

    @Override
    public String getKeystorePassword() {
        return parent.getKeystorePassword();
    }

    @Override
    public Path getTrustStorePath() {
        return parent.getTrustStorePath();
    }

    @Override
    public String getTrustStorePassword() {
        return parent.getTrustStorePassword();
    }
}
