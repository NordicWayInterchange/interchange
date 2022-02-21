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
      HostCsrDetail hostCsrDetail = getHostCsrDetail(imageBaseFolder, intermediateDomain, intermediateNodeCountry);
      assertThat(hostCsrDetail.getCsrOnHost()).exists();
      //TODO this is a really bad name. It's the CA that signs the intermediate ca.
      IntermedateCaDetails intermedateCaDetails = getIntermedateCaDetails(imageBaseFolder, rootCaDetails, intermediateDomain, intermediateNodeCountry, hostCsrDetail);
      assertThat(intermedateCaDetails.getSingleCertOnHost()).exists();
      assertThat(intermedateCaDetails.getChainCertOnHost()).exists();
      //Now we have top ca, intermediate ca.
      //Now we need 1 server ca for messages container, and 2 Service Provider certs
      String serverDomainName = "messages.node-domain.no";
      String serverCountryCode = "NO";
      ServerCertDetails serverCertDetails = getServerCertDetails(imageBaseFolder, hostCsrDetail, intermedateCaDetails, serverDomainName, serverCountryCode);
      assertThat(serverCertDetails.getKeyOnHost()).exists();
      assertThat(serverCertDetails.getCertOnHost()).exists();
      assertThat(serverCertDetails.getCertChainOnHost()).exists();
      String username = "service-provider";
      String serviceProviderCountry = "NO";
      ServiceProviderCSRGenerator serviceProviderCSRGenerator = new ServiceProviderCSRGenerator(
              imageBaseFolder.resolve("serviceprovider/csr"),
              keysPath,
              username,
              serviceProviderCountry
      );
      serviceProviderCSRGenerator.start();
      ServiceProviderCsrDetails serviceProviderCsrDetails = new ServiceProviderCsrDetails(serviceProviderCSRGenerator.getCsrOnHost(),serviceProviderCSRGenerator.getKeyOnHost());
      assertThat(serviceProviderCsrDetails.getCsrOnHost()).exists();
      assertThat(serviceProviderCsrDetails.getKeyOnHost()).exists();
      ServiceProviderCertDetails serviceProviderCertDetails = getServiceProviderCertDetails(imageBaseFolder, hostCsrDetail, intermedateCaDetails, username, serviceProviderCsrDetails);
      assertThat(serviceProviderCertDetails.getCertOnHost()).exists();
      assertThat(serviceProviderCertDetails.getCertChainOnHost()).exists();

   }

   private ServiceProviderCertDetails getServiceProviderCertDetails(Path imageBaseFolder, HostCsrDetail hostCsrDetail, IntermedateCaDetails intermedateCaDetails, String username, ServiceProviderCsrDetails serviceProviderCsrDetails) {
      ServiceProviderCertGenerator serviceProviderCertGenerator = new ServiceProviderCertGenerator(
              imageBaseFolder.resolve("intermediateca/sign_sp"),
              serviceProviderCsrDetails.getCsrOnHost(),
              username,
              intermedateCaDetails.getSingleCertOnHost(),
              hostCsrDetail.getKeyOnHost(),
              intermedateCaDetails.getChainCertOnHost(),
              keysPath
      );
      serviceProviderCertGenerator.start();
      ServiceProviderCertDetails serviceProviderCertDetails = new ServiceProviderCertDetails(serviceProviderCertGenerator.getCertOnHost(),serviceProviderCertGenerator.getCertChainOnHost());
      return serviceProviderCertDetails;
   }

   private ServerCertDetails getServerCertDetails(Path imageBaseFolder, HostCsrDetail hostCsrDetail, IntermedateCaDetails intermedateCaDetails, String serverDomainName, String serverCountryCode) {
      ServerCertGenerator serverCertGenerator = new ServerCertGenerator(
              imageBaseFolder.resolve("server"),
              serverDomainName,
              intermedateCaDetails.getSingleCertOnHost(),
              hostCsrDetail.getKeyOnHost(),
              intermedateCaDetails.getChainCertOnHost(),
              serverCountryCode,
              keysPath
      );
      serverCertGenerator.start();
      ServerCertDetails serverCertDetails = new ServerCertDetails(serverCertGenerator.getKeyOnHost(),serverCertGenerator.getCertOnHost(),serverCertGenerator.getCertChainOnHost());
      return serverCertDetails;
   }

   private IntermedateCaDetails getIntermedateCaDetails(Path imageBaseFolder, RootCaDetails rootCaDetails, String intermediateDomain, String intermediateNodeCountry, HostCsrDetail hostCsrDetail) {
      IntermediateCACertGenerator intermediateCACertGenerator = new IntermediateCACertGenerator(
              imageBaseFolder.resolve("rootca/sign_intermediate"),
              hostCsrDetail.getCsrOnHost(),
              intermediateDomain,
              rootCaDetails.getCaCertOnHost(),
              rootCaDetails.getCaKeyOnHost(),
              intermediateNodeCountry,
              keysPath
      );
      intermediateCACertGenerator.start();
      IntermedateCaDetails intermedateCaDetails = new IntermedateCaDetails(intermediateCACertGenerator.getSingleCertOnHost(),intermediateCACertGenerator.getChainCertOnHost());
      return intermedateCaDetails;
   }

   private HostCsrDetail getHostCsrDetail(Path imageBaseFolder, String intermediateDomain, String intermediateNodeCountry) {
      IntermediateCaCSRGenerator hostCsrGenerator = new IntermediateCaCSRGenerator(
              imageBaseFolder.resolve("intermediateca/csr"),
              keysPath,
              intermediateDomain,
              intermediateNodeCountry
      );
      hostCsrGenerator.start();
      HostCsrDetail hostCsrDetail = new HostCsrDetail(hostCsrGenerator.getCsrOnHost(), hostCsrGenerator.getKeyOnHost());
      return hostCsrDetail;
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

   private class HostCsrDetail {
      private final Path csrOnHost;
      private final Path keyOnHost;

      public HostCsrDetail(Path csrOnHost, Path keyOnHost) {
         this.csrOnHost = csrOnHost;
         this.keyOnHost = keyOnHost;
      }

      public Path getCsrOnHost() {
         return csrOnHost;
      }

      public Path getKeyOnHost() {
         return keyOnHost;
      }
   }

   private class IntermedateCaDetails {
      private final Path singleCertOnHost;
      private final Path chainCertOnHost;

      public IntermedateCaDetails(Path singleCertOnHost, Path chainCertOnHost) {
         this.singleCertOnHost = singleCertOnHost;
         this.chainCertOnHost = chainCertOnHost;
      }

      public Path getSingleCertOnHost() {
         return singleCertOnHost;
      }

      public Path getChainCertOnHost() {
         return chainCertOnHost;
      }
   }

   private class ServerCertDetails {
      private final Path keyOnHost;
      private final Path certOnHost;
      private final Path certChainOnHost;

      public ServerCertDetails(Path keyOnHost, Path certOnHost, Path certChainOnHost) {
         this.keyOnHost = keyOnHost;
         this.certOnHost = certOnHost;
         this.certChainOnHost = certChainOnHost;
      }

      public Path getKeyOnHost() {
         return keyOnHost;
      }

      public Path getCertOnHost() {
         return certOnHost;
      }

      public Path getCertChainOnHost() {
         return certChainOnHost;
      }
   }

   private class ServiceProviderCsrDetails {
      private final Path csrOnHost;
      private final Path keyOnHost;

      public ServiceProviderCsrDetails(Path csrOnHost, Path keyOnHost) {
         this.csrOnHost = csrOnHost;
         this.keyOnHost = keyOnHost;
      }

      public Path getCsrOnHost() {
         return csrOnHost;
      }

      public Path getKeyOnHost() {
         return keyOnHost;
      }
   }

   private class ServiceProviderCertDetails {
      private final Path certOnHost;
      private final Path certChainOnHost;

      public ServiceProviderCertDetails(Path certOnHost, Path certChainOnHost) {
         this.certOnHost = certOnHost;
         this.certChainOnHost = certChainOnHost;
      }

      public Path getCertOnHost() {
         return certOnHost;
      }

      public Path getCertChainOnHost() {
         return certChainOnHost;
      }
   }
}
