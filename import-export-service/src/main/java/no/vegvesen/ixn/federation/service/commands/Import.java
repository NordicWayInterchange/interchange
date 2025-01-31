package no.vegvesen.ixn.federation.service.commands;


import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.service.ImportTransformer;
import no.vegvesen.ixn.federation.service.importmodel.ImportApi;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Component
@CommandLine.Command(
        name = "import"
)
public class Import implements Callable<Integer> {

    private NeighbourRepository neighbourRepository;

    private final ServiceProviderRepository serviceProviderRepository;

    private final PrivateChannelRepository privateChannelRepository;
    private final ImportTransformer importTransformer;
    private final ObjectMapper mapper;

    @CommandLine.Option(names = {"-p", "--path"}, description = "File to import from", required = true)
    private Path path;

    @CommandLine.Option(names = {"-n", "--include-neighbours"}, description = "include neighbour data in import")
    private boolean neighbours;

    @Autowired
    public Import(NeighbourRepository neighbourRepository, ServiceProviderRepository serviceProviderRepository, PrivateChannelRepository privateChannelRepository) {
        this.neighbourRepository = neighbourRepository;
        this.serviceProviderRepository = serviceProviderRepository;
        this.privateChannelRepository = privateChannelRepository;
        this.importTransformer = new ImportTransformer();
        this.mapper = new ObjectMapper();
    }


    @Override
    public Integer call() throws Exception {

        ImportApi importModel = mapper.readValue(path.toFile(), ImportApi.class);

        serviceProviderRepository.saveAll(importModel.getServiceProviders().stream().map(importTransformer::transformServiceProviderImportApiToServiceProvider).collect(Collectors.toSet()));
        privateChannelRepository.saveAll(importModel.getPrivateChannels().stream().map(importTransformer::transformPrivateChannelImportApiToPrivateChannel).collect(Collectors.toSet()));
         if (neighbours) {
             neighbourRepository.saveAll(importModel.getNeighbours().stream().map(importTransformer::transformNeighbourImportApiToNeighbour).collect(Collectors.toSet()));
         }
        return 0;
    }
}
