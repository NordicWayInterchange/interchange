package no.vegvesen.ixn.federation.service.importmodel;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class Importer {
    private ServiceProviderRepository serviceProviderRepository;
    private PrivateChannelRepository privateChannelRepository;
    private final NeighbourRepository neighbourRepository;

    public Importer(ServiceProviderRepository serviceProviderRepository, PrivateChannelRepository privateChannelRepository, NeighbourRepository neighbourRepository) {
        this.serviceProviderRepository = serviceProviderRepository;
        this.privateChannelRepository = privateChannelRepository;
        this.neighbourRepository = neighbourRepository;
    }


    public void importModel(Path path) throws IOException {
        ImportTransformer importTransformer = new ImportTransformer();
        ObjectMapper mapper = new ObjectMapper();
        ImportApi importModel = mapper.readValue(Files.newInputStream(path), ImportApi.class);
        serviceProviderRepository.saveAll(importModel.getServiceProviders().stream().map(importTransformer::transformServiceProviderImportApiToServiceProvider).collect(Collectors.toSet()));
        privateChannelRepository.saveAll(importModel.getPrivateChannels().stream().map(importTransformer::transformPrivateChannelImportApiToPrivateChannel).collect(Collectors.toSet()));
    }

    public void importModelWithNeighbours(Path path) throws IOException {
        ImportTransformer importTransformer = new ImportTransformer();
        ObjectMapper mapper = new ObjectMapper();

        ImportApi importModel = mapper.readValue(Files.newInputStream(path), ImportApi.class);
        neighbourRepository.saveAll(importModel.getNeighbours().stream().map(importTransformer::transformNeighbourImportApiToNeighbour).collect(Collectors.toSet()));
        serviceProviderRepository.saveAll(importModel.getServiceProviders().stream().map(importTransformer::transformServiceProviderImportApiToServiceProvider).collect(Collectors.toSet()));
        privateChannelRepository.saveAll(importModel.getPrivateChannels().stream().map(importTransformer::transformPrivateChannelImportApiToPrivateChannel).collect(Collectors.toSet()));

    }
}
