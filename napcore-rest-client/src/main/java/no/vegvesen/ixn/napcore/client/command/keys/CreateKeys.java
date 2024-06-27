package no.vegvesen.ixn.napcore.client.command.keys;

import no.vegvesen.ixn.napcore.client.NapRESTClient;
import no.vegvesen.ixn.napcore.model.CertificateSignRequest;
import no.vegvesen.ixn.napcore.model.CertificateSignResponse;
import picocli.CommandLine;

import java.util.Base64;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@CommandLine.Command(
        name = "create",
        description = "create keys",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class CreateKeys implements Callable<Integer> {

    @CommandLine.ParentCommand
    KeysCommand  parentCommand;

    @CommandLine.Parameters(index = "0", paramLabel = "SERVICE_PROVIDER_NAME",description = "Service Provider name")
    String spName;

    @CommandLine.Parameters(index = "1", paramLabel = "SERVICE_PROVIDER_COUNTRY",description = "Service Provider country code")
    String countryCode;

    @Override
    public Integer call() throws Exception {
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        NapRESTClient.KeyAndCSR keyAndCSR = client.generateKeyAndCSR(spName, countryCode);
        CertificateSignResponse certificateSignResponse = client.requestCertificate(new CertificateSignRequest(Base64.getEncoder().encodeToString(keyAndCSR.getCsr().getBytes())));
        System.out.println(keyAndCSR.getKey());
        List<String> decodedChain = certificateSignResponse.getChain().stream().map(s -> new String(Base64.getDecoder().decode(s.getBytes()))).collect(Collectors.toList());
        System.out.println(String.join("",decodedChain));
        return 0;
    }
}
