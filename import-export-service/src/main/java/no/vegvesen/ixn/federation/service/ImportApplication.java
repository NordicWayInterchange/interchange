package no.vegvesen.ixn.federation.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.service.importmodel.ImportApi;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class ImportApplication {

    private NeighbourRepository neighbourRepository;

    private ServiceProviderRepository serviceProviderRepository;

    private PrivateChannelRepository privateChannelRepository;
    private ImportTransformer importTransformer;
    private ObjectMapper mapper;

    public ImportApplication(NeighbourRepository neighbourRepository, ServiceProviderRepository serviceProviderRepository, PrivateChannelRepository privateChannelRepository, ImportTransformer importTransformer, ObjectMapper mapper) {
        this.neighbourRepository = neighbourRepository;
        this.serviceProviderRepository = serviceProviderRepository;
        this.privateChannelRepository = privateChannelRepository;
        this.importTransformer = importTransformer;
        this.mapper = mapper;
    }

    public void run() throws Exception {
        importTransformer = new ImportTransformer();
        mapper = new ObjectMapper();

        Path path = Paths.get(this.getClass().getClassLoader().getResource("jsonDump.txt").toURI());
        ImportApi importModel = mapper.readValue(path.toFile(), ImportApi.class);

        serviceProviderRepository.saveAll(importModel.getServiceProviders().stream().map(importTransformer::transformServiceProviderImportApiToServiceProvider).collect(Collectors.toSet()));
        privateChannelRepository.saveAll(importModel.getPrivateChannels().stream().map(importTransformer::transformPrivateChannelImportApiToPrivateChannel).collect(Collectors.toSet()));
    }
}
