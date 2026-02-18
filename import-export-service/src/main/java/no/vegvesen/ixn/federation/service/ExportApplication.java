package no.vegvesen.ixn.federation.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.vegvesen.ixn.federation.service.exportmodel.ExportApi;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class ExportApplication {

    private NeighbourRepository neighbourRepository;

    private ServiceProviderRepository serviceProviderRepository;

    private PrivateChannelRepository privateChannelRepository;

    public ExportApplication(NeighbourRepository neighbourRepository, ServiceProviderRepository serviceProviderRepository, PrivateChannelRepository privateChannelRepository) {
        this.neighbourRepository = neighbourRepository;
        this.serviceProviderRepository = serviceProviderRepository;
        this.privateChannelRepository = privateChannelRepository;
    }

    public void run(String... args) throws Exception {
        if (args.length == 0) {
            System.out.println("No output path provided. Exiting.");
            return;
        }

        String outputFilePath = args[0];
        if (outputFilePath == null || outputFilePath.isBlank()) {
            outputFilePath = "./export.json"; // fallback
        }

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

        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

        Path filePath = Paths.get(outputFilePath);
        if (filePath.getParent() != null && !Files.exists(filePath.getParent())) {
            Files.createDirectories(filePath.getParent());
        }

        writer.writeValue(filePath.toFile(), exportModel);
        System.out.println("Export saved to: " + filePath.toAbsolutePath());
    }
}
