package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.service.exportmodel.Exporter;
import no.vegvesen.ixn.federation.service.importmodel.Importer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;

@SpringBootApplication(scanBasePackages = "no.vegvesen.ixn")
public class ImportExportApplication implements CommandLineRunner {

    @Autowired
    private NeighbourRepository neighbourRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private PrivateChannelRepository privateChannelRepository;


    public static void main(String[] args) {
        SpringApplication.run(ImportExportApplication.class);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length != 2) {
            System.out.println("Needs two arguments, <import | export> <filename>");
        } else if (args[0].equals("import")) {
            Importer importer = new Importer(serviceProviderRepository, privateChannelRepository, neighbourRepository);
            importer.importModel(Path.of(args[1]));

        } else if (args[0].equals("export")) {
            Exporter exporter = new Exporter(serviceProviderRepository, privateChannelRepository, neighbourRepository);
            exporter.exportModel(Path.of(args[1]));
        }
    }

}
