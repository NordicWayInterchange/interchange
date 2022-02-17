package no.vegvesen.ixn.docker;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class FullChainKeyGeneratorIT {

   Path keysPath = DockerBaseIT.getTestPrefixedOutputPath(FullChainKeyGeneratorIT.class);

   @Test
   public void generateFullChain() {
      Path imageBaseFolder = DockerBaseIT.getFolderPath("keymaster");
      String caDomain = "root-domain.eu";
      String caCountry = "NO";
      RootCaDetails rootCaDetails = getRootCaDetails(imageBaseFolder, caDomain, caCountry);
      assertThat(rootCaDetails.getCaKeyOnHost()).exists();
      assertThat(rootCaDetails.getCaCertOnHost()).exists();
      String intermediateDomain = "node-domain.no";
      String intermediateNodeCountry = "NO";
      IntermediateCaCSRGenerator hostCsrGenerator = new IntermediateCaCSRGenerator(
              imageBaseFolder.resolve("intermediateca/csr"),
              keysPath,
              intermediateDomain,
              intermediateNodeCountry
      );
      hostCsrGenerator.start();
      assertThat(hostCsrGenerator.getCsrOnHost()).exists();
      //TODO this is a really bad name. It's the CA that signs the intermediate ca.
      IntermediateCACertGenerator intermediateCACertGenerator = new IntermediateCACertGenerator(
              imageBaseFolder.resolve("rootca/sign_intermediate"),
              hostCsrGenerator.getCsrOnHost(),
              intermediateDomain,
              rootCaDetails.getCaCertOnHost(),
              rootCaDetails.getCaKeyOnHost(),
              intermediateNodeCountry,
              keysPath
      );
      intermediateCACertGenerator.start();
      assertThat(intermediateCACertGenerator.getSingleCertOnHost()).exists();
      assertThat(intermediateCACertGenerator.getChainCertOnHost()).exists();
      //Now we have top ca, intermediate ca.
      //Now we need 1 server ca for messages container, and 2 Service Provider certs
      String serverDomainName = "messages.node-domain.no";
      String serverCountryCode = "NO";
      ServerCertGenerator serverCertGenerator = new ServerCertGenerator(
              imageBaseFolder.resolve("server"),
              serverDomainName,
              intermediateCACertGenerator.getSingleCertOnHost(),
              hostCsrGenerator.getKeyOnHost(),
              intermediateCACertGenerator.getChainCertOnHost(),
              serverCountryCode,
              keysPath
      );
      serverCertGenerator.start();
      assertThat(serverCertGenerator.getKeyOnHost()).exists();
      assertThat(serverCertGenerator.getCertOnHost()).exists();
      assertThat(serverCertGenerator.getCertChainOnHost()).exists();
      String username = "service-provider";
      String serviceProviderCountry = "NO";
      ServiceProviderCSRGenerator serviceProviderCSRGenerator = new ServiceProviderCSRGenerator(
              imageBaseFolder.resolve("serviceprovider/csr"),
              keysPath,
              username,
              serviceProviderCountry
      );
      serviceProviderCSRGenerator.start();
      assertThat(serviceProviderCSRGenerator.getCsrOnHost()).exists();
      assertThat(serviceProviderCSRGenerator.getKeyOnHost()).exists();
      ServiceProviderCertGenerator serviceProviderCertGenerator = new ServiceProviderCertGenerator(
              imageBaseFolder.resolve("intermediateca/sign_sp"),
              serviceProviderCSRGenerator.getCsrOnHost(),
              username,
              intermediateCACertGenerator.getSingleCertOnHost(),
              hostCsrGenerator.getKeyOnHost(),
              intermediateCACertGenerator.getChainCertOnHost(),
              keysPath
      );
      serviceProviderCertGenerator.start();
      assertThat(serviceProviderCertGenerator.getCertOnHost()).exists();
      assertThat(serviceProviderCertGenerator.getCertChainOnHost()).exists();

   }

   private RootCaDetails getRootCaDetails(Path imageBaseFolder, String caDomain, String caCountry) {
      RootCAKeyGenerator rootCAKeyGenerator = new RootCAKeyGenerator(
              imageBaseFolder.resolve("rootca/newca"),
              keysPath,
              caDomain,
              caCountry
      );
      rootCAKeyGenerator.start();
      RootCaDetails rootCaDetails = new RootCaDetails(rootCAKeyGenerator.getCaKeyOnHost(), rootCAKeyGenerator.getCaCertOnHost());
      return rootCaDetails;
   }

   public static class RootCaDetails {

      private final Path caKeyOnHost;
      private final Path caCertOnHost;

      public RootCaDetails(Path caKeyOnHost, Path caCertOnHost) {
         this.caKeyOnHost = caKeyOnHost;
         this.caCertOnHost = caCertOnHost;
      }

      public Path getCaKeyOnHost() {
         return caKeyOnHost;
      }

      public Path getCaCertOnHost() {
         return caCertOnHost;
      }
   }

}
