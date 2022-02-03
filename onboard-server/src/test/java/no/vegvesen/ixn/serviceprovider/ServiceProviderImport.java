package no.vegvesen.ixn.serviceprovider;

import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;

public class ServiceProviderImport {
    public static ServiceProviderApi[] getServiceProviderApis(Path path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ServiceProviderApi[] serviceProviders = mapper.readValue(path.toFile(), ServiceProviderApi[].class);
        return serviceProviders;
    }

    public static OldServiceProviderApi[] getOldServiceProviderApis(Path path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        OldServiceProviderApi[] serviceProviders = mapper.readValue(path.toFile(),OldServiceProviderApi[].class);
        return serviceProviders;
    }
}
