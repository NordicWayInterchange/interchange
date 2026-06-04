package no.vegvesen.ixn.federation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.service.exportmodel.ExportApi;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;

@SpringBootApplication(scanBasePackages = "no.vegvesen.ixn")
public class ExportApplication implements CommandLineRunner {

    private final NeighbourRepository neighbourRepository;

    private final ServiceProviderRepository serviceProviderRepository;

    private final PrivateChannelRepository privateChannelRepository;

    public ExportApplication(NeighbourRepository neighbourRepository, ServiceProviderRepository serviceProviderRepository, PrivateChannelRepository privateChannelRepository) {
        this.neighbourRepository = neighbourRepository;
        this.serviceProviderRepository = serviceProviderRepository;
        this.privateChannelRepository = privateChannelRepository;
    }


    public static void main(String[] args) {
        SpringApplication.run(ExportApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) {
            System.out.println("No output path provided. Exiting.");
            return;
        }

        if (args[0].equals("export")) {
            if (args.length != 2) {
                System.out.println("No output path provided. Exiting.");
            }

            String outputFilePath = args[1];
            exportData(outputFilePath);
        }
    }

    private void exportData(String outputFilePath) throws Exception {
        ExportTransformer exportTransformer = new ExportTransformer();
        ObjectMapper mapper = new ObjectMapper();

        ExportApi exportModel = new ExportApi(
                neighbourRepository.findAll().stream()
                        .map(exportTransformer::transformNeighbourToNeighbourExportApi)
                        .collect(Collectors.toSet()),
                serviceProviderRepository.findAll().stream()
                        .map(exportTransformer::transformServiceProviderToServiceProviderExportApi)
                        .collect(Collectors.toSet()),
                privateChannelRepository.findAll().stream()
                        .map(exportTransformer::transformPrivateChannelToPrivateChannelExportApi)
                        .collect(Collectors.toSet())
        );

        Path filePath = Paths.get(outputFilePath);
        if (filePath.getParent() != null && !Files.exists(filePath.getParent())) {
            Files.createDirectories(filePath.getParent());
        }

        mapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), exportModel);

        System.out.println("Export saved to: " + filePath.toAbsolutePath());
    }
}
