package no.vegvesen.ixn.federation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Paths;

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
            // Just start normally (used for DB initialization)
            return;
        }
        if (args[0].equals("import")) {
            if (args.length != 2) {
                throw new IllegalArgumentException("usage: import <file>");
            }
            ImportApplication importApplication = new ImportApplication(
                    neighbourRepository,
                    serviceProviderRepository,
                    privateChannelRepository
            );
            importApplication.run(Paths.get(args[1]));

            System.exit(0);
        } else if (args[0].equals("export")) {
            ExportApplication exportApplication = new ExportApplication(
                    neighbourRepository,
                    serviceProviderRepository,
                    privateChannelRepository
            );
            exportApplication.run();

            System.exit(0);
        }
    }
}
