package no.vegvesen.ixn.federation.importexportclient.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.service.importmodel.ImportApi;
import no.vegvesen.ixn.federation.service.importmodel.ImportTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Component
@CommandLine.Command(name = "import")
public class Import implements Callable<Integer> {

    @Option(names = {"-p", "--path"}, required = true)
    String localPath;

    @Option(names = {"-n", "--neighbours"})
    boolean includeNeighbours;

    ServiceProviderRepository serviceProviderRepository;

    PrivateChannelRepository privateChannelRepository;

    NeighbourRepository neighbourRepository;

    @Autowired
    public Import(ServiceProviderRepository serviceProviderRepository, PrivateChannelRepository privateChannelRepository, NeighbourRepository neighbourRepository) {
        this.serviceProviderRepository = serviceProviderRepository;
        this.privateChannelRepository = privateChannelRepository;
        this.neighbourRepository = neighbourRepository;
    }

    @Override
    public Integer call() throws Exception {

        if (includeNeighbours) {
            importModelWithNeighbours();
        } else {
            importModel();
        }
        return 0;
    }

    public void importModel() throws Exception {
        ImportTransformer importTransformer = new ImportTransformer();
        ObjectMapper mapper = new ObjectMapper();

        Path path = Paths.get(localPath);
        ImportApi importModel = mapper.readValue(path.toFile(), ImportApi.class);

        serviceProviderRepository.saveAll(importModel.getServiceProviders().stream().map(importTransformer::transformServiceProviderImportApiToServiceProvider).collect(Collectors.toSet()));
        privateChannelRepository.saveAll(importModel.getPrivateChannels().stream().map(importTransformer::transformPrivateChannelImportApiToPrivateChannel).collect(Collectors.toSet()));
    }

    public void importModelWithNeighbours() throws Exception {
        ImportTransformer importTransformer = new ImportTransformer();
        ObjectMapper mapper = new ObjectMapper();

        Path path = Paths.get(localPath);
        ImportApi importModel = mapper.readValue(path.toFile(), ImportApi.class);

        neighbourRepository.saveAll(importModel.getNeighbours().stream().map(importTransformer::transformNeighbourImportApiToNeighbour).collect(Collectors.toSet()));
        serviceProviderRepository.saveAll(importModel.getServiceProviders().stream().map(importTransformer::transformServiceProviderImportApiToServiceProvider).collect(Collectors.toSet()));
        privateChannelRepository.saveAll(importModel.getPrivateChannels().stream().map(importTransformer::transformPrivateChannelImportApiToPrivateChannel).collect(Collectors.toSet()));
    }
}
