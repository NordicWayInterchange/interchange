package no.vegvesen.ixn.docker;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

public class QpidContainer extends GenericContainer<QpidContainer> {

    public static final int HTTPS_PORT = 443;
    public static final int HTTP_PORT = 8080;
    public static final int AMQPS_PORT = 5671;
    public static final int AMQP_PORT = 5672;
    private final Path configPathFromClasspath;
    private final Path keysBasePath;
    private final String keyStore;
    private final String keyStorePassword;
    private final String trustStore;
    private final String trustStorePassword;
    private final String vHostName;


    public QpidContainer(String imageName,
                         Path dockerFilePath,
                         Path configPathFromClasspath,
                         Path keysBasePath,
                         String keyStore,
                         String keyStorePassword,
                         String trustStore,
                         String trustStorePassword,
                         String vHostName) {
        super(new ImageFromDockerfile(imageName,false)
                .withFileFromPath(".",dockerFilePath));
        this.configPathFromClasspath = configPathFromClasspath;
        this.keysBasePath = keysBasePath;
        this.keyStore = keyStore;
        this.keyStorePassword = keyStorePassword;
        this.trustStore = trustStore;
        this.trustStorePassword = trustStorePassword;
        this.vHostName = vHostName;
        this.waitingFor(Wait.forLogMessage(".*BRK-1004.*\\n",1));
        this.withExposedPorts(AMQP_PORT,AMQPS_PORT,HTTP_PORT,HTTPS_PORT);
    }


    @Override
    protected void configure() {
        this.withClasspathResourceMapping(configPathFromClasspath.toString(),"/config", BindMode.READ_ONLY);
        this.withFileSystemBind(keysBasePath.toString(),"/jks",BindMode.READ_ONLY);
        this.withEnv("KEY_STORE", "/jks/" + keyStore);
        this.withEnv("KEY_STORE_PASSWORD", keyStorePassword);
        this.withEnv("TRUST_STORE", "/jks/" + trustStore);
        this.withEnv("TRUST_STORE_PASSWORD", trustStorePassword);
        this.withEnv("VHOST_NAME",vHostName);
    }

    @Override
    public Set<Integer> getLivenessCheckPortNumbers() {
        return Collections.singleton(getMappedPort(AMQP_PORT));
    }

    public Integer getAmqpsPort() {
        return getMappedPort(AMQPS_PORT);
    }

    public Integer getHttpsPort() {
        return getMappedPort(HTTPS_PORT);
    }

    public Integer getHttpPort() {
        return getMappedPort(HTTP_PORT);
    }

    public String getAmqpsUrl() {
        return String.format("amqps://%s:%d",getHost(),getMappedPort(AMQPS_PORT));
    }

    public String getAmqpUrl() {
        return String.format("amqp://%s:%d",getHost(),getMappedPort(AMQP_PORT));
    }


    public String getHttpsUrl() {
        return String.format("https://%s:%d",getHost(),getMappedPort(HTTPS_PORT));
    }

    public String getHttpUrl() {
        return String.format("http://%s:%d",getHost(),getMappedPort(HTTP_PORT));
    }

    public String getvHostName() {
        return vHostName;
    }
}