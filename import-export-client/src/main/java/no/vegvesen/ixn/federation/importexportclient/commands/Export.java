package no.vegvesen.ixn.federation.importexportclient.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.service.exportmodel.ExportApi;
import no.vegvesen.ixn.federation.service.exportmodel.ExportTransformer;
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

    @CommandLine.Option(names = {"-p", "--path"})
    String localPath;

    ServiceProviderRepository serviceProviderRepository;

    NeighbourRepository neighbourRepository;

    PrivateChannelRepository privateChannelRepository;

    @Autowired
    public Export(ServiceProviderRepository serviceProviderRepository, NeighbourRepository neighbourRepository, PrivateChannelRepository privateChannelRepository){
        this.serviceProviderRepository = serviceProviderRepository;
        this.neighbourRepository = neighbourRepository;
        this.privateChannelRepository = privateChannelRepository;
    }

    @Override
    public Integer call() throws Exception {
        ExportTransformer exportTransformer = new ExportTransformer();
        ObjectMapper mapper = new ObjectMapper();

        ExportApi exportModel = new ExportApi(
                neighbourRepository.findAll().stream().map(exportTransformer::transformNeighbourToNeighbourExportApi).collect(Collectors.toSet()),
                serviceProviderRepository.findAll().stream().map(exportTransformer::transformServiceProviderToServiceProviderExportApi).collect(Collectors.toSet()),
                privateChannelRepository.findAll().stream().map(exportTransformer::transformPrivateChannelToPrivateChannelExportApi).collect(Collectors.toSet())
        );

        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        Path path = Paths.get(localPath);
        writer.writeValue(path.toFile(), exportModel);
        return 0;
    }
}