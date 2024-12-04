package no.vegvesen.ixn.federation.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.vegvesen.ixn.federation.service.exportmodel.ExportApi;
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
        String localPath = "";
        Path path = Paths.get(localPath, "dump.json");
        writer.writeValue(path.toFile(), exportModel);
    }
}
