package no.vegvesen.ixn.keys;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.keys.generator.CARequest;
import no.vegvesen.ixn.keys.generator.CaResponse;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.PasswordGenerator;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@SuppressWarnings("InstantiationOfUtilityClass")
@Command(name = "keygenerator",
        description = "Generates keys for Docker Comapose tests",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        subcommands = {
           KeysGeneratorApplication.Keys.class
        }
)
public class KeysGeneratorApplication {


    @Command(name = "generate", description = "Generate keys from input JSON to target folder")
    public static class Keys implements Callable<Integer> {
        @Option(names = "-f", required = true, description = "Path to intput JSON file")
        private Path intputFile;

        @Option(names = "-o", required = true, description = "Folder for created key and truststores")
        private Path outputFolder;

        //TODO option to use RandomPasswordGenerator
        private final PasswordGenerator passwordGenerator = () -> "password";

        @Override
        public Integer call() throws Exception {
            if (!Files.isDirectory(outputFolder)) {
                throw new IllegalArgumentException("Output folder is not a directory");
            }
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<List<CARequest>> listTypeReference = new TypeReference<>() {};
            List<CARequest> caRequests = mapper.readValue(Files.newInputStream(intputFile), listTypeReference);
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
