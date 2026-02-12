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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public void run() throws Exception {
        ExportTransformer exportTransformer = new ExportTransformer();
        ObjectMapper mapper = new ObjectMapper();

        ExportApi exportModel = new ExportApi(
                neighbourRepository.findAll().stream().map(exportTransformer::transformNeighbourToNeighbourExportApi).collect(Collectors.toSet()),
                serviceProviderRepository.findAll().stream().map(exportTransformer::transformServiceProviderToServiceProviderExportApi).collect(Collectors.toSet()),
                privateChannelRepository.findAll().stream().map(exportTransformer::transformPrivateChannelToPrivateChannelExportApi).collect(Collectors.toSet())
        );
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
//        String localPath = "";
//        Path path = Paths.get(localPath, "dump.json");
//        writer.writeValue(path.toFile(), exportModel);

        String outputDir = System.getenv("EXPORT_PATH");

        if (outputDir == null || outputDir.isBlank()) {
            outputDir = "./";
        }

        Path directory = Paths.get(outputDir);

        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        Path filePath = directory.resolve("export-" + timestamp + ".json");

        if (Files.exists(filePath)) {
            throw new RuntimeException("File already exists: " + filePath);
        }
        writer.writeValue(filePath.toFile(), exportModel);
        System.out.println("Export saved to: " + filePath.toAbsolutePath());
    }
}
