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
      ServiceProviderCsrDetails serviceProviderCsrDetails = getServiceProviderCsrDetails(imageBaseFolder, username, serviceProviderCountry);
      assertThat(serviceProviderCsrDetails.getCsrOnHost()).exists();
      assertThat(serviceProviderCsrDetails.getKeyOnHost()).exists();
      ServiceProviderCertDetails serviceProviderCertDetails = getServiceProviderCertDetails(imageBaseFolder, hostCsrDetail, intermedateCaDetails, username, serviceProviderCsrDetails);
      assertThat(serviceProviderCertDetails.getCertOnHost()).exists();
      assertThat(serviceProviderCertDetails.getCertChainOnHost()).exists();

   }

   private ServiceProviderCsrDetails getServiceProviderCsrDetails(Path imageBaseFolder, String username, String serviceProviderCountry) {
      ServiceProviderCSRGenerator serviceProviderCSRGenerator = new ServiceProviderCSRGenerator(
              imageBaseFolder.resolve("serviceprovider/csr"),
              keysPath,
              username,
              serviceProviderCountry
      );
      serviceProviderCSRGenerator.start();
      return serviceProviderCSRGenerator.getServiceProviderCsrDetails();
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
      return serviceProviderCertGenerator.getServiceProviderCertDetails();
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
      return serverCertGenerator.getServiceCertDetails();
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
      return intermediateCACertGenerator.getIntermediateCaDetails();
   }

   private HostCsrDetail getHostCsrDetail(Path imageBaseFolder, String intermediateDomain, String intermediateNodeCountry) {
      IntermediateCaCSRGenerator hostCsrGenerator = new IntermediateCaCSRGenerator(
              imageBaseFolder.resolve("intermediateca/csr"),
              keysPath,
              intermediateDomain,
              intermediateNodeCountry
      );
      hostCsrGenerator.start();
      return hostCsrGenerator.getHostCsrDetails();
   }

   private RootCaDetails getRootCaDetails(Path imageBaseFolder, String caDomain, String caCountry) {
      RootCAKeyGenerator rootCAKeyGenerator = new RootCAKeyGenerator(
              imageBaseFolder.resolve("rootca/newca"),
              keysPath,
              caDomain,
              caCountry
      );
      rootCAKeyGenerator.start();
      return rootCAKeyGenerator.getRootCaDetails();
   }

}
