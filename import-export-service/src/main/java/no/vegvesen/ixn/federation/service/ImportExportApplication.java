package no.vegvesen.ixn.federation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.service.exportmodel.ExportApi;
import no.vegvesen.ixn.federation.service.exportmodel.ExportTransformer;
import no.vegvesen.ixn.federation.service.importmodel.ImportApi;
import no.vegvesen.ixn.federation.service.importmodel.ImportTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@SpringBootApplication(scanBasePackages = "no.vegvesen.ixn")
public class ImportExportApplication implements CommandLineRunner {

    @Autowired
    private NeighbourRepository neighbourRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private PrivateChannelRepository privateChannelRepository;

    @Value("${localPath}")
    String localPath;

    public static void main(String[] args) {
        SpringApplication.run(ImportExportApplication.class);
    }

    @Override
    public void run(String... args) throws Exception {
        //importModel();
        //exportModel;
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

    public void exportModel() throws Exception {
        ExportTransformer exportTransformer = new ExportTransformer();
        ObjectMapper mapper = new ObjectMapper();

        ExportApi exportModel = new ExportApi(
                neighbourRepository.findAll().stream().map(exportTransformer::transformNeighbourToNeighbourExportApi).collect(Collectors.toSet()),
                serviceProviderRepository.findAll().stream().map(exportTransformer::transformServiceProviderToServiceProviderExportApi).collect(Collectors.toSet()),
                privateChannelRepository.findAll().stream().map(exportTransformer::transformPrivateChannelToPrivateChannelExportApi).collect(Collectors.toSet())
        );

        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        Path path = Paths.get(localPath);
        writer.writeValue(path.toFile(), exportModel);
    }
}
