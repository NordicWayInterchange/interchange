package no.vegvesen.ixn.federation.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.service.importmodel.ImportApi;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@SpringBootApplication(scanBasePackages = "no.vegvesen.ixn")
public class ImportApplication implements CommandLineRunner {

    private NeighbourRepository neighbourRepository;

    private final ServiceProviderRepository serviceProviderRepository;

    private final PrivateChannelRepository privateChannelRepository;
    private final ImportTransformer importTransformer;
    private final ObjectMapper mapper;

    public static void main(String[] args) {
        SpringApplication.run(ImportApplication.class, args);
    }

    public ImportApplication(NeighbourRepository neighbourRepository, ServiceProviderRepository serviceProviderRepository, PrivateChannelRepository privateChannelRepository) {
        this.neighbourRepository = neighbourRepository;
        this.serviceProviderRepository = serviceProviderRepository;
        this.privateChannelRepository = privateChannelRepository;
        this.importTransformer = new ImportTransformer();
        this.mapper = new ObjectMapper();
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) {
            System.out.println("usage ...");
            System.exit(1);
        }

        if (args[0].equals("import")) {
            if (args.length != 2) {
                System.out.println("usage ...");
                System.exit(2);
            }
        }

        String inputFilePath = args[1];
        Path filePath = Paths.get(inputFilePath);

        ImportApi importModel = mapper.readValue(filePath.toFile(), ImportApi.class);

        serviceProviderRepository.saveAll(importModel.getServiceProviders().stream().map(importTransformer::transformServiceProviderImportApiToServiceProvider).collect(Collectors.toSet()));
        privateChannelRepository.saveAll(importModel.getPrivateChannels().stream().map(importTransformer::transformPrivateChannelImportApiToPrivateChannel).collect(Collectors.toSet()));

        System.out.println("Import completed successfully from file: " + inputFilePath);

    }
}
