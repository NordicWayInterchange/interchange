package no.vegvesen.ixn.docker.keygen.generator;

import com.fasterxml.jackson.databind.*;
import no.vegvesen.ixn.docker.DockerBaseIT;
import no.vegvesen.ixn.docker.keygen.Cluster;
import no.vegvesen.ixn.docker.keygen.generator.ClusterKeyGenerator.CertificateCertificateChainAndKeys;
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

public class ClusterKeyGeneratorIT {


    public static final List<CARequest> CA_REQUESTS = List.of(
            new CARequest(
                    "bouvetinterchange.eu",
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
                                                    "king_olaf.a.bouvetinterchange.eu",
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
                                                    "king_gustav.b.bouvetinterchange.eu",
                                                    "SE",
                                                    "kungen@slottet.se"
                                            )
                                    )
                            ),
                            new CARequest(
                                    "c.bouvetinterchange.eu",
                                    "FI",
                                    List.of(),
                                    List.of(
                                            new HostRequest(
                                                    "c.bouvetinterchange.eu"
                                            )
                                    ),
                                    List.of(
                                            new ClientRequest(
                                                    "president.c.bouvetinterechange.eu",
                                                    "FI",
                                                    "presidenten@repulic.fi"
                                            )
                                    )
                            )),
                    List.of(),
                    List.of()
            ),
            new CARequest(
                    "a-internal",
                    "NO",
                    List.of(),
                    List.of(),
                    List.of(
                            new ClientRequest(
                                    "a.routing-configurer",
                                    "NO",
                                    "routing-configurer@a.bouvetinterchange.eu"
                            ),
                            new ClientRequest(
                                    "a.nap",
                                    "NO",
                                    "nap@a.bouvetinterchange.eu"
                            )
                    )
            ),
            new CARequest(
                    "b-internal",
                    "SE",
                    List.of(),
                    List.of(),
                    List.of(
                            new ClientRequest(
                                    "b.routing-configurer",
                                    "SE",
                                    "routing-configurer@b.bouvetinterchange.eu"
                            ),
                            new ClientRequest(
                                    "b.nap",
                                    "SE",
                                    "nap@b.bouvetinterchange.eu"
                            )
                    )
            ),
            new CARequest(
                    "c-internal",
                    "FI",
                    List.of(),
                    List.of(),
                    List.of(
                            new ClientRequest(
                                    "c.routing-configurer",
                                    "FI",
                                    "routing-configurer@c.bouvetinterchange.eu"
                            ),
                            new ClientRequest(
                                    "c.nap",
                                    "FI",
                                    "nap@c.bouvetinterchange.eu"
                            )
                    )
            ));
    Path targetPath = DockerBaseIT.getTargetFolderPathForTestClass(ClusterKeyGeneratorIT.class);

    @Test
    public void readClusterFromJson() throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, OperatorCreationException, NoSuchProviderException, SignatureException, InvalidKeyException {
        Path jsonPath = Paths.get("src", "test", "resources", "cluster.json");
        Cluster cluster = new ObjectMapper().readValue(jsonPath.toFile(), Cluster.class);
        assertThat(cluster.getTopDomain().getDomainName()).isEqualTo("top-domain.eu");
        assertThat(cluster.getTopDomain().getIntermediateDomains()).hasSize(1);
        assertThat(cluster.getTopDomain().getIntermediateDomains().get(0).getInterchange().getServiceProviders()).hasSize(2);
        ClusterKeyGenerator.generateKeys(cluster,targetPath);
    }

    @Test
    public void readClusterFromAnotherJson() throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, SignatureException, OperatorCreationException, NoSuchProviderException, InvalidKeyException {
        Path jsonPath = Paths.get("src", "test", "resources", "systemtest-cluster.json");
        Cluster cluster = new ObjectMapper().readValue(jsonPath.toFile(), Cluster.class);
        ClusterKeyGenerator.generateKeys(cluster,targetPath);
    }

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
    public void testCaWithHostCert() throws CertificateException, NoSuchAlgorithmException, OperatorCreationException, IOException, SignatureException, InvalidKeyException, NoSuchProviderException, KeyStoreException {
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
        ClusterKeyGenerator.generateKeystore("password","mydomain.com",host.keyPair().getPrivate(), host.certificateChain(), outputStream);
    }

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
        ClusterKeyGenerator.generateKeystore("password","host.com",host.keyPair().getPrivate(),host.certificateChain() ,new ByteArrayOutputStream());
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
    public void saveKeystores() throws IOException, CertificateException, NoSuchAlgorithmException, SignatureException, OperatorCreationException, InvalidKeyException, NoSuchProviderException, KeyStoreException {
        Path basePath = Files.createTempDirectory("keystoreTest");
        System.out.println(basePath);
        List<CaResponse> responses = new ArrayList<>();
        for (CARequest request : CA_REQUESTS) {
            responses.add(ClusterKeyGenerator.generate(request));
        }
        for(CaResponse response : responses) {
            ClusterKeyGenerator.store(response,basePath);
        }
        try (Stream<Path> list = Files.list(basePath)) {
            assertThat(list.filter(p -> p.toString().endsWith(".jks")).toList()).hasSize(4);
        }
        //TODO check the actual names of the jks and p12's and see if we can load the key from them using the passwords
        //assertThat(Files.list(basePath).filter(p -> !Files.isDirectory(p)).toList()).hasSize(2);
    }

    private static String getCountry(X500Name name) {
        RDN[] rdNs = name.getRDNs(BCStyle.C);
        return IETFUtils.valueToString(rdNs[0].getFirst().getValue());
    }


}

