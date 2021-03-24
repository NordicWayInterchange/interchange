package no.vegvesen.ixn.federation.discoverer.facade;

import no.vegvesen.ixn.docker.DockerBaseIT;
import no.vegvesen.ixn.docker.KeysContainer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

//@Disabled
@Testcontainers
public class NeighbourRESTClientIT {

    public static final String NEIGHBOUR_SERVER = "neighbour-server";
    public static final String DB_CONTAINER = "db-container";
    public static final String NEIGHBOUR_CLIENT = "neighbour-client";

    private static Network network = Network.newNetwork();

    @Container
    private static KeysContainer keysContainer = DockerBaseIT.getKeysContainer(NeighbourRESTClientIT.class,
            "top-ca",
            NEIGHBOUR_SERVER,
            NEIGHBOUR_CLIENT);

    @Container
    private static PostgreSQLContainer<?> databaseContainer = DockerBaseIT.getDbContainer()
            .dependsOn(keysContainer)
            .withNetwork(network)
            .withNetworkAliases(DB_CONTAINER);




    @Container
    private static GenericContainer neighbourServer = createNeighbourContainer()
            .dependsOn(keysContainer)
            .dependsOn(databaseContainer)
            .withNetwork(network)
            .withNetworkAliases(NEIGHBOUR_SERVER);

    @Test
    public void testFoo() throws IOException, InterruptedException {
        assertThat(neighbourServer.isRunning()).isTrue();
        assertThat(databaseContainer.isRunning()).isTrue();
        System.out.println(databaseContainer.getJdbcUrl());

        GenericContainer<?> commandContainer = new GenericContainer<>(new ImageFromDockerfile()
                .withDockerfileFromBuilder( builder ->
                        builder
                                .from("alpine:latest")
                                .run("apk add --no-cache curl")
                                .build()))
                .withClasspathResourceMapping(Paths.get("unknown-json").toString(),
                        "/files",
                        BindMode.READ_ONLY)
                .withFileSystemBind(keysContainer.getKeyFolderOnHost().toString(),"/jks",BindMode.READ_ONLY)
                .withNetwork(network)
                .withNetworkAliases(NEIGHBOUR_CLIENT)
                .withCommand("top")
                .dependsOn(neighbourServer);
        commandContainer.start();
        System.out.println("Wait here....");
        ExecResult result = commandContainer.execInContainer("curl", "-X",
                "POST",
                "-d",
                "@/files/sample.json",
                "-H",
                "Content-Type: application/json",
                "--cacert",
                "/jks/top-ca.crt",
                "--cert",
                "/jks/" + NEIGHBOUR_CLIENT + ".crt",
                "--key",
                "/jks/" + NEIGHBOUR_CLIENT + ".key",
                "--insecure",
                "https://" + NEIGHBOUR_SERVER + "/subscriptions");
        System.out.println(result.getExitCode());
        System.out.println(result.getStderr());
        System.out.println(result.getStdout());
        commandContainer.stop();

    }



     private static GenericContainer createNeighbourContainer() {
         return new NeighbourServerContainer(keysContainer.getKeyFolderOnHost());
     }


     private static class NeighbourServerContainer extends GenericContainer<NeighbourServerContainer> {
         private Path keysFolderOnHost;

         public NeighbourServerContainer(Path keysFolderOnHost) {
            super(new ImageFromDockerfile("neighbour-server-app")
                    .withFileFromPath(".",DockerBaseIT.getProjectRelativePath("neighbour-server-app")));
             this.keysFolderOnHost = keysFolderOnHost;
         }


         @Override
         protected void configure() {
             this.withFileSystemBind(keysFolderOnHost.toString(),"/jks", BindMode.READ_ONLY)
                     .withEnv("POSTGRES_URI","jdbc:postgresql://" + DB_CONTAINER + ":" + PostgreSQLContainer.POSTGRESQL_PORT + "/federation")
                     .withEnv("DOMAIN_NAME",NEIGHBOUR_SERVER)
                     .withEnv("CTRL_CHNL_PORT","443")
                     .withEnv("KEY_STORE","/jks/" + NEIGHBOUR_SERVER + ".p12")
                     .withEnv("KEY_STORE_PASSWORD","password")
                     .withEnv("SERVER_NAME",NEIGHBOUR_SERVER)
                     .withEnv("TRUST_STORE","/jks/truststore.jks")
                     .withEnv("TRUST_STORE_PASSWORD","password");
         }
     }
}
