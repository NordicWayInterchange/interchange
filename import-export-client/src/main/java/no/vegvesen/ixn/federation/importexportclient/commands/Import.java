package no.vegvesen.ixn.federation.importexportclient.commands;

import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.service.importmodel.Importer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Component
@CommandLine.Command(
        name = "import",
        mixinStandardHelpOptions = true
)
public class Import implements Callable<Integer> {

    private final Importer importer;

    @Option(names = {"-p", "--path"}, required = true)
    Path localPath;

    @Option(names = {"-n", "--neighbours"})
    boolean includeNeighbours;


    @Autowired
    public Import(ServiceProviderRepository serviceProviderRepository, PrivateChannelRepository privateChannelRepository, NeighbourRepository neighbourRepository) {
        importer = new Importer(serviceProviderRepository, privateChannelRepository, neighbourRepository);

    }

    @Override
    public Integer call() throws Exception {

        if (includeNeighbours) {
            importer.importModelWithNeighbours(localPath);
        } else {
            importer.importModel(localPath);
        }
        return 0;
    }

}
