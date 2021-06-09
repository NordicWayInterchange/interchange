package no.vegvesen.ixn.docker;

import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class ServiceProviderCertGeneratorIT {

    static Path resourcePath = Paths.get("src/test/resources");
    static Path intermediateCaPath = resourcePath.resolve("intermediateca");
    static Path testPrefixedOutputPath = DockerBaseIT.getTestPrefixedOutputPath(ServiceProviderCertGeneratorIT.class);

    static Logger logger = LoggerFactory.getLogger(ServiceProviderCertGeneratorIT.class);
    static Slf4jLogConsumer consumer = new Slf4jLogConsumer(logger);

    @Container
    static ServiceProviderCertGenerator generator = new ServiceProviderCertGenerator(
        DockerBaseIT.getFolderPath("keymaster").resolve("intermediateca/sign_sp"),
            resourcePath.resolve("serviceprovider/testprovider.csr.pem"),
            "testprovider",
            intermediateCaPath.resolve("int.test2.no.crt.pem"),
            intermediateCaPath.resolve("int.test2.no.key.pem"),
            intermediateCaPath.resolve("chain.test2.no.crt.pem"),
            testPrefixedOutputPath
    );

    @BeforeAll
    public static void setupLogging() {
        System.out.println("----------------------------------------------------------" + Files.exists(resourcePath.resolve("serviceprovider/testprovider.csr.pem")));
        generator.followOutput(consumer);
    }


    @Test
    public void testCertificateGenerated() {
        assertThat(testPrefixedOutputPath.resolve("chain.testprovider.crt.pem")).exists();
        assertThat(testPrefixedOutputPath.resolve("testprovider.crt.pem")).exists();

    }

}
