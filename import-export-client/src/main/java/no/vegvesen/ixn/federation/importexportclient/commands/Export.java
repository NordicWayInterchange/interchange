package no.vegvesen.ixn.federation.importexportclient.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.service.exportmodel.ExportApi;
import no.vegvesen.ixn.federation.service.exportmodel.ExportTransformer;
import no.vegvesen.ixn.federation.service.exportmodel.Exporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Component
@CommandLine.Command(name = "export")
public class Export implements Callable<Integer> {

    private final Exporter exporter;

    @CommandLine.Option(names = {"-p", "--path"})
    Path localPath;

    @Autowired
    public Export(ServiceProviderRepository serviceProviderRepository, NeighbourRepository neighbourRepository, PrivateChannelRepository privateChannelRepository){
        exporter = new Exporter(serviceProviderRepository,privateChannelRepository,neighbourRepository);
    }

    @Override
    public Integer call() throws Exception {
        exporter.exportModel(localPath);
        return 0;
    }
}