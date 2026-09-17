package no.vegvesen.ixn.keys;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.keys.generator.CARequest;
import no.vegvesen.ixn.keys.generator.CaResponse;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator;
import no.vegvesen.ixn.keys.generator.PasswordGenerator;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

@SuppressWarnings("InstantiationOfUtilityClass")
@Command(name = "keygenerator",
        description = "Generates keys for Docker Compose tests",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        subcommands = {
                KeysGeneratorApplication.Keys.class
        }
)
public class KeysGeneratorApplication {


    @Command(
            name = "generate",
            description = "Generate keys from input JSON to target folder",
            mixinStandardHelpOptions = true
    )
    public static class Keys implements Callable<Integer> {
        @Option(names = "-f", required = true, description = "Path to input JSON file")
        private Path inputFile;

        @Option(names = "-o", required = true, description = "Folder for created key and truststores")
        private Path outputFolder;

        @Option(names = {"-s","--staticpassword"}, description = "Create a static password for testing. The password generated will be \"password\" for all keystores")
        private Boolean testPassword;



        @Override
        public Integer call() throws Exception {
            PasswordGenerator passwordGenerator;
            if (Objects.equals(testPassword, Boolean.TRUE)) {
                passwordGenerator = PasswordGenerator.staticPassword("password");
            } else {
                passwordGenerator = PasswordGenerator.random(new SecureRandom(),12);
            }
            if (!Files.isDirectory(outputFolder)) {
                throw new IllegalArgumentException("Output folder is not a directory");
            }
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<List<CARequest>> listTypeReference = new TypeReference<>() {};
            List<CARequest> caRequests = mapper.readValue(Files.newInputStream(inputFile), listTypeReference);
            List<CaResponse> caResponses = new ArrayList<>();
            for (CARequest request : caRequests) {
                CaResponse response = ClusterKeyGenerator.generate(request);
                caResponses.add(response);
            }
            for (CaResponse response : caResponses) {
                ClusterKeyGenerator.store(response,outputFolder,passwordGenerator);
            }
            return 0;
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new KeysGeneratorApplication()).execute(args);
        System.exit(exitCode);
    }
}
