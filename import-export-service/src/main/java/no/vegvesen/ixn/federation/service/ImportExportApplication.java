package no.vegvesen.ixn.federation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "no.vegvesen.ixn")
public class ImportExportApplication implements CommandLineRunner {

    @Autowired
    private NeighbourRepository neighbourRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private PrivateChannelRepository privateChannelRepository;

    public static void main(String[] args) {
        SpringApplication.run(ImportExportApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) {
            System.out.println("usage ...");
            System.exit(1);
        }
        if (args[1].equals("import")) {
            ImportApplication importApplication = new ImportApplication(
                    neighbourRepository,
                    serviceProviderRepository,
                    privateChannelRepository,
                    new ImportTransformer(),
                    new ObjectMapper());
            importApplication.run();
        } else if (args[1].equals("export")) {
            ExportApplication exportApplication = new ExportApplication(
                    neighbourRepository,
                    serviceProviderRepository,
                    privateChannelRepository
            );
            exportApplication.run();
        }
    }
}
