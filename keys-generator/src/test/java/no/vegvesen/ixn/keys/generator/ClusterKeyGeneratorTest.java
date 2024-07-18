package no.vegvesen.ixn.keys.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.CaStores;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.CertificateCertificateChainAndKeys;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.PasswordGenerator;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ClusterKeyGeneratorTest {


    public static final CARequest INTERNAL_A = new CARequest(
            "internal_a",
            "NO",
            List.of(),
            List.of(new HostRequest(
                    "a-qpid"
            )),
            List.of(
                    new ClientRequest(
                            "a_routing_configurer",
                            "NO",
                            "routing-configurer@a.bouvetinterchange.eu"
                    ),
                    new ClientRequest(
                            "a.nap",
                            "NO",
                            "nap@a.bouvetinterchange.eu"
                    ),
                    new ClientRequest(
                            "a_message_collector",
                            "NO",
                            "message-collector@a.bouvetinterchange.eu"
                    )
            )
    );
    public static final CARequest INTERNAL_B = new CARequest(
            "internal_b",
            "SE",
            List.of(),
            List.of(new HostRequest(
                    "b-qpid"
            )),
            List.of(
                    new ClientRequest(
                            "b_routing_configurer",
                            "SE",
                            "routing-configurer@b.bouvetinterchange.eu"
                    ),
                    new ClientRequest(
                            "b.nap",
                            "SE",
                            "nap@b.bouvetinterchange.eu"
                    ),
                    new ClientRequest(
                            "b_message_collector",
                            "SE",
                            "message-collector@b.bouvetinterchange.eu"
                    )
            )
    );
    //TODO denne vil bruke samme cert for host cert og for client cert mot nabo. Dette burde skilles!
    public static final List<CARequest> CA_REQUESTS = List.of(
            new CARequest(
                    "ca.bouvetinterchange.eu",
                    "NO",
                    List.of(
                            new CARequest(
                                    "ca.a.bouvetinterchange.eu",
                                    "NO",
                                    List.of(),
                                    List.of(
                                            new HostRequest(
                                                    "a.bouvetinterchange.eu"
                                            )
                                    ),
                                    List.of(
                                            new ClientRequest(
                                                    "king_olav.bouvetinterchange.eu",
                                                    "NO",
                                                    "post@slottet.no"
                                            )
                                    )
                            ),
                            new CARequest(
                                    "ca.b.bouvetinterchange.eu",
                                    "SE",
                                    List.of(),
                                    List.of(
                                            new HostRequest(
                                                    "b.bouvetinterchange.eu"
                                            )
                                    ),
                                    List.of(
                                            new ClientRequest(
                                                    "king_gustaf.bouvetinterchange.eu",
                                                    "SE",
                                                    "kungen@slottet.se"
                                            )
                                    )
                            )),
                    List.of(),
                    List.of()
            ),
            INTERNAL_A,
            INTERNAL_B
    );

    @Test
    public void testGenerateTopCaCountryWithDefaultCountryCode() throws CertificateException, NoSuchAlgorithmException, OperatorCreationException, CertIOException, SignatureException, InvalidKeyException, NoSuchProviderException {
        CaResponse reponse = ClusterKeyGenerator.generate(new CARequest("mydomain.com", null, List.of(), List.of(), List.of()));
        CertificateCertificateChainAndKeys ca = reponse.details();
        X500Name issuerName = new X500Name(ca.certificate().getIssuerX500Principal().getName());
        X500Name subjectName = new X500Name(ca.certificate().getSubjectX500Principal().getName());
        assertThat(getCountry(issuerName)).isEqualTo("NO");
        assertThat(getCountry(subjectName)).isEqualTo("NO");
    }

    @Test
    public void testGenerateIntermediateCaWithDefaultCountryCode() throws CertificateException, NoSuchAlgorithmException, OperatorCreationException, CertIOException, SignatureException, InvalidKeyException, NoSuchProviderException {
        //TODO a better way of generating these
        CaResponse response = ClusterKeyGenerator.generate(new CARequest(
                "mydomain.com",
                null,
                List.of(
                        new CARequest(
                                "childCa",
                                null,
                                List.of(),
                                List.of(),
                                List.of()
                        )
                ),
                List.of(),
                List.of()
        ));
        CertificateCertificateChainAndKeys topCa = response.details();
        CertificateCertificateChainAndKeys intermediateCa = response.caResponses().get(0).details();
        List<X509Certificate> certificateChain = intermediateCa.certificateChain();
        assertThat(certificateChain).hasSize(2);
        X509Certificate certificate = intermediateCa.certificate();
        assertThat(certificateChain.get(0)).isEqualTo(certificate);
        assertThat(certificateChain.get(1)).isEqualTo(topCa.certificate());
        X500Name subjectName = new X500Name(certificate.getSubjectX500Principal().getName());
        assertThat(getCountry(subjectName)).isEqualTo("NO");
    }


    @Test
    public void testSeveralLayersOfCa() throws CertificateException, NoSuchAlgorithmException, OperatorCreationException, CertIOException, SignatureException, InvalidKeyException, NoSuchProviderException {
        CaResponse response = ClusterKeyGenerator.generate(
                new CARequest(
                        "mydomain.com",
                        null,
                        List.of(
                                new CARequest(
                                        "clildCa",
                                        null,
                                        List.of(
                                                new CARequest(
                                                        "subCa",
                                                        null,
                                                        List.of(),
                                                        List.of(),
                                                        List.of()
                                                )
                                        ),
                                        List.of(),
                                        List.of()
                                )
                        ),
                        List.of(),
                        List.of()

                )
        );
        CertificateCertificateChainAndKeys topCa = response.details();
        CertificateCertificateChainAndKeys intermediateCa = response.caResponses().get(0).details();
        CertificateCertificateChainAndKeys subCa = response.caResponses().get(0).caResponses().get(0).details();
        List<X509Certificate> chain = subCa.certificateChain();
        assertThat(chain).hasSize(3);
        assertThat(chain.get(2)).isEqualTo(topCa.certificate());
        assertThat(chain.get(1)).isEqualTo(intermediateCa.certificate());
        assertThat(chain.get(0)).isEqualTo(subCa.certificate());
        intermediateCa.certificate().verify(topCa.keyPair().getPublic());
        subCa.certificate().verify(intermediateCa.keyPair().getPublic());
    }

    @Test
    public void testCaWithHostCert() throws CertificateException, NoSuchAlgorithmException, OperatorCreationException, IOException, SignatureException, InvalidKeyException, NoSuchProviderException {
        CaResponse response = ClusterKeyGenerator.generate(
                new CARequest(
                        "topdomain",
                        "no",
                        List.of(),
                        List.of(
                                new HostRequest("myhost.com")
                        ),
                        List.of()
                )
        );
        CertificateCertificateChainAndKeys topCa = response.details();
        CertificateCertificateChainAndKeys host = response.hostResponses().get(0).keyDetails();
        Collection<List<?>> subjectAlternativeNames = host.certificate().getSubjectAlternativeNames();
        assertThat(subjectAlternativeNames).isNotNull().hasSize(1);
        List<?> san = subjectAlternativeNames.iterator().next();
        assertThat(san).hasSize(2);
        assertThat(san.get(0)).isEqualTo(GeneralName.dNSName);
        assertThat(san.get(1)).isEqualTo("myhost.com");
        assertThat(host.certificateChain()).hasSize(2);
        assertThat(host.certificateChain().get(1)).isEqualTo(topCa.certificate());
        assertThat(host.certificateChain().get(0)).isEqualTo(host.certificate());
        host.certificate().verify(topCa.keyPair().getPublic());
    }


    //TODO Load the keystore and assert on the contents
    @Test
    public void makeHostKeystore() throws CertificateException, NoSuchAlgorithmException, OperatorCreationException, IOException, SignatureException, InvalidKeyException, NoSuchProviderException, KeyStoreException {
        CaResponse response = ClusterKeyGenerator.generate(
                new CARequest(
                        "topdomain",
                        "NO",
                        List.of(),
                        List.of( new HostRequest("myhost.com") ),
                        List.of()
                )
        );
        CertificateCertificateChainAndKeys topCa = response.details();
        CertificateCertificateChainAndKeys host = response.hostResponses().get(0).keyDetails();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        assertThat(host.certificateChain()).hasSize(2);
        host.certificate().verify(topCa.keyPair().getPublic());
        ClusterKeyGenerator.makeKeystore("mydomain.com","password",outputStream,host.certificateChain(),host.keyPair().getPrivate());
    }

    //TODO Load the keystore and assert on the contents
    @Test
    public void makeHostKeystoreWithTwoLayersOfCa() throws CertificateException, NoSuchAlgorithmException, SignatureException, OperatorCreationException, InvalidKeyException, NoSuchProviderException, IOException, KeyStoreException {
        CaResponse response = ClusterKeyGenerator.generate(
                new CARequest(
                        "topdomain",
                        "NO",
                        List.of(
                                new CARequest(
                                        "intermediate",
                                        "NO",
                                        List.of(),
                                        List.of( new HostRequest("domain.com")),
                                        List.of()
                                )
                        ),
                        List.of(),
                        List.of()
                )
        );
        CertificateCertificateChainAndKeys intermediate = response.caResponses().get(0).details();
        CertificateCertificateChainAndKeys host = response.caResponses().get(0).hostResponses().get(0).keyDetails();
        assertThat(host.certificateChain()).hasSize(3);
        assertThat(host.certificateChain().get(0)).isEqualTo(host.certificate());
        assertThat(host.certificateChain().get(1)).isEqualTo(intermediate.certificate());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ClusterKeyGenerator.makeKeystore("host.com","password",outputStream,host.certificateChain(),host.keyPair().getPrivate());
    }


    @Test
    public void testServiceProviderWithOneLevelOfCa() throws CertificateException, NoSuchAlgorithmException, OperatorCreationException, CertIOException, SignatureException, InvalidKeyException, NoSuchProviderException {
        CaResponse response = ClusterKeyGenerator.generate(new CARequest(
                "topdomain",
                "NO",
                List.of(),
                List.of(),
                List.of(
                        new ClientRequest(
                                "sp",
                                "NO",
                                "sp@test.com"
                        )
                ))
        );
        CertificateCertificateChainAndKeys topCa = response.details();
        CertificateCertificateChainAndKeys sp = response.clientResponses().get(0).clientDetails();
        sp.certificate().verify(topCa.keyPair().getPublic());
    }


    @Test
    public void generateKeys() throws IOException, CertificateException, NoSuchAlgorithmException, SignatureException, OperatorCreationException, InvalidKeyException, NoSuchProviderException {
        List<CaResponse> responses = new ArrayList<>();
        for (CARequest request : CA_REQUESTS) {
            responses.add(ClusterKeyGenerator.generate(request));
        }
        Writer responseWriter = new StringWriter();
        ClusterKeyGenerator.writeCaReponsesToJson(responseWriter, responses);
        String responseJson = responseWriter.toString();
        Reader responseReader = new StringReader(responseJson);
        List<CaResponse> result = ClusterKeyGenerator.readCaResponsesFromJson(responseReader);
        assertThat(result).hasSize(CA_REQUESTS.size());
    }


    @Test
    public void requests() throws IOException {
        Path outPath = Paths.get("").toAbsolutePath().getParent().resolve("test-keys").resolve(ClusterKeyGeneratorTest.class.getSimpleName()).resolve("systemtest-keys.json");
        Files.createDirectories(outPath);
        ObjectWriter writer = new ObjectMapper().writerWithDefaultPrettyPrinter();
        writer.writeValue(Files.newOutputStream(outPath),CA_REQUESTS);
    }

    @Test
    public void saveKeystores() throws IOException, CertificateException, NoSuchAlgorithmException, SignatureException, OperatorCreationException, InvalidKeyException, NoSuchProviderException, KeyStoreException {
        Path absolutePath = Path.of("").toAbsolutePath();
        Path parent = absolutePath.getParent();
        Path additionalPath = Path.of("target","test-keys",ClusterKeyGeneratorTest.class.getSimpleName());
        Path target = parent.resolve(additionalPath);
        Files.createDirectories(target);

        List<CaResponse> responses = new ArrayList<>();
        for (CARequest request : CA_REQUESTS) {
            responses.add(ClusterKeyGenerator.generate(request));
        }
        PasswordGenerator passwordGenerator = new ClusterKeyGenerator.RandomPasswordGenerator(new SecureRandom(),12);
        List<CaStores> caStores = new ArrayList<>();
        for(CaResponse response : responses) {
            CaStores stores = ClusterKeyGenerator.store(response, target, passwordGenerator);
            caStores.add(stores);
        }
        assertThat(caStores).hasSize(3);
        //TODO some functionality to traverse the tree and select the nodes we want
        try (Stream<Path> list = Files.list(target)) {
            assertThat(list.filter(p -> p.toString().endsWith(".jks")).toList()).hasSize(5);
        }
    }

    private static String getCountry(X500Name name) {
        RDN[] rdNs = name.getRDNs(BCStyle.C);
        return IETFUtils.valueToString(rdNs[0].getFirst().getValue());
    }


}

