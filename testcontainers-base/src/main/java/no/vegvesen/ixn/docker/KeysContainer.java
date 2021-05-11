package no.vegvesen.ixn.docker;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.startupcheck.IndefiniteWaitOneShotStartupCheckStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;
import java.time.Duration;

public class KeysContainer extends GenericContainer<KeysContainer> {

    private Path keyFolder;
    private String caName;
    private String[] serverOrUserCns;

    public KeysContainer(Path dockerFilePath, Path keyFolder, String caName, String ... serverOrUserCns) {
        super(new ImageFromDockerfile("key-gen",false)
                .withFileFromPath(".", dockerFilePath));
        this.keyFolder = keyFolder;
        this.caName = caName;
        this.serverOrUserCns = serverOrUserCns;
    }

    @Override
    public void configure() {
        this.withFileSystemBind(keyFolder.toString(),"/jks/keys", BindMode.READ_WRITE);
        this.withEnv("CA_CN",caName);
        this.withEnv("KEY_CNS",String.join(" ",serverOrUserCns));
        this.withEnv("KEYS_DIR","/jks/keys");
        this.withStartupCheckStrategy(new IndefiniteWaitOneShotStartupCheckStrategy().withTimeout(Duration.ofSeconds(5)));
    }

    public Path getKeyFolderOnHost() {
        return keyFolder;
    }
}
