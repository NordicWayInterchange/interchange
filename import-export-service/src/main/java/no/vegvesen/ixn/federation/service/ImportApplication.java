package no.vegvesen.ixn.federation.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.service.importmodel.ImportApi;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;

import java.nio.file.Path;
import java.util.stream.Collectors;

public class ImportApplication {

    private NeighbourRepository neighbourRepository;

    private final ServiceProviderRepository serviceProviderRepository;

    private final PrivateChannelRepository privateChannelRepository;
    private final ImportTransformer importTransformer;
    private final ObjectMapper mapper;

    public ImportApplication(NeighbourRepository neighbourRepository, ServiceProviderRepository serviceProviderRepository, PrivateChannelRepository privateChannelRepository) {
        this.neighbourRepository = neighbourRepository;
        this.serviceProviderRepository = serviceProviderRepository;
        this.privateChannelRepository = privateChannelRepository;
        this.importTransformer = new ImportTransformer();
        this.mapper = new ObjectMapper();
    }

    public void run(Path path) throws Exception {

        ImportApi importModel = mapper.readValue(path.toFile(), ImportApi.class);

        serviceProviderRepository.saveAll(importModel.getServiceProviders().stream().map(importTransformer::transformServiceProviderImportApiToServiceProvider).collect(Collectors.toSet()));
        privateChannelRepository.saveAll(importModel.getPrivateChannels().stream().map(importTransformer::transformPrivateChannelImportApiToPrivateChannel).collect(Collectors.toSet()));
    }
}
