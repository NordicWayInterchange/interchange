package no.vegvesen.ixn.federation.service.exportmodel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class Exporter {
    private final ServiceProviderRepository serviceProviderRepository;
    private final PrivateChannelRepository privateChannelRepository;
    private final NeighbourRepository neighbourRepository;

    public Exporter(ServiceProviderRepository serviceProviderRepository, PrivateChannelRepository privateChannelRepository, NeighbourRepository neighbourRepository) {
        this.serviceProviderRepository = serviceProviderRepository;
        this.privateChannelRepository = privateChannelRepository;
        this.neighbourRepository = neighbourRepository;
    }

    public void exportModel(Path path) throws IOException {

        ExportTransformer exportTransformer = new ExportTransformer();
        ObjectMapper mapper = new ObjectMapper();

        ExportApi exportModel = new ExportApi(
                neighbourRepository.findAll().stream().map(exportTransformer::transformNeighbourToNeighbourExportApi).collect(Collectors.toSet()),
                serviceProviderRepository.findAll().stream().map(exportTransformer::transformServiceProviderToServiceProviderExportApi).collect(Collectors.toSet()),
                privateChannelRepository.findAll().stream().map(exportTransformer::transformPrivateChannelToPrivateChannelExportApi).collect(Collectors.toSet())
        );

        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        writer.writeValue(Files.newOutputStream(path), exportModel);    }
}
