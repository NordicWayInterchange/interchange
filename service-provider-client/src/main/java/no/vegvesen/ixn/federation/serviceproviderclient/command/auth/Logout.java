package no.vegvesen.ixn.federation.serviceproviderclient.command.auth;

import no.vegvesen.ixn.federation.serviceproviderclient.token.TokenStore;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(
        name = "logout",
        description = "Remove the locally cached access token",
        mixinStandardHelpOptions = true,
        version = "1.0"
)
public class Logout implements Callable<Integer> {

    @Override
    public Integer call() {
        new TokenStore().clear();
        System.out.println("Logged out. The cached access token has been removed.");
        return 0;
    }
}


