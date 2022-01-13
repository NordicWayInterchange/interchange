package no.vegvesen.ixn.serviceprovider;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ParseServiceProviderFileTest {

    @Test
    public void importServiceProviders() throws IOException {
        Path path = Paths.get("/pre_deliveries_database_dump/jsonDump.txt");
        ServiceProviderApi[] serviceProviders = ServiceProviderImport.getServiceProviderApis(path);
        for (ServiceProviderApi serviceProvider : serviceProviders) {
            System.out.println(serviceProvider.getName());
        }

    }


}
