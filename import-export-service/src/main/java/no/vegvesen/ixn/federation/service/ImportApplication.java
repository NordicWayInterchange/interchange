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

@SpringBootApplication(scanBasePackages = "no.vegvesen.ixn")
public class ImportApplication implements CommandLineRunner {

    @Autowired
    private NeighbourRepository neighbourRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private PrivateChannelRepository privateChannelRepository;

    public static void main(String[] args) {
        SpringApplication.run(ImportApplication.class);
    }

    @Override
    public void run(String... args) throws Exception {
        ImportTransformer importTransformer = new ImportTransformer();
        ObjectMapper mapper = new ObjectMapper();

        Path path = Paths.get(this.getClass().getClassLoader().getResource("jsonDump.txt").toURI());
        ImportApi importModel = mapper.readValue(path.toFile(), ImportApi.class);

        serviceProviderRepository.saveAll(importModel.getServiceProviders().stream().map(importTransformer::transformServiceProviderImportApiToServiceProvider).collect(Collectors.toSet()));
        privateChannelRepository.saveAll(importModel.getPrivateChannels().stream().map(importTransformer::transformPrivateChannelImportApiToPrivateChannel).collect(Collectors.toSet()));
    }
}
