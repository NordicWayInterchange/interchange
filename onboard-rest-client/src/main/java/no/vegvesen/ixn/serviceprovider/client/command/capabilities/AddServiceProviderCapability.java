package no.vegvesen.ixn.serviceprovider.client.command.capabilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.serviceprovider.client.OnboardRESTClient;
import no.vegvesen.ixn.serviceprovider.client.OnboardRestClientApplication;
import no.vegvesen.ixn.serviceprovider.model.AddCapabilitiesRequest;
import no.vegvesen.ixn.serviceprovider.model.AddCapabilitiesResponse;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "addcapability", description = "Add service provider capability from file")
public class AddServiceProviderCapability implements Callable<Integer> {

    @CommandLine.ParentCommand
    OnboardRestClientApplication parentCommand;

    @CommandLine.Option(names = {"-f", "--filename"}, description = "The capability json file")
    File file;

    @Override
    public Integer call() throws IOException {
        OnboardRESTClient client = parentCommand.createClient();
        ObjectMapper mapper = new ObjectMapper();
        AddCapabilitiesRequest capability = mapper.readValue(file, AddCapabilitiesRequest.class);
        AddCapabilitiesResponse result = client.addCapability(capability);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        return 0;
    }
}
