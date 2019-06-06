package no.vegvesen.ixn.federation.forwarding;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

//TODO: generate additional-spring-configuration-metadata.json
//TODO: set default values
@Component
@ConfigurationProperties(prefix ="forwarder")
public class ForwarderProperties {

	private String localIxnDomainName;
	private String localIxnFederationPort;
	//TODO: Use NeighbourSSLProperties
	private String keystorepath;
	private String keystorepassword;
	private String keystoretype;

	private String truststorepath;
	private String truststorepassword;
	private String truststoretype;
	private String remoteWritequeue;

	public String getLocalIxnDomainName() {
		return localIxnDomainName;
	}

	public void setLocalIxnDomainName(String localIxnDomainName) {
		this.localIxnDomainName = localIxnDomainName;
	}

	public String getLocalIxnFederationPort() {
		return localIxnFederationPort;
	}

	public void setLocalIxnFederationPort(String localIxnFederationPort) {
		this.localIxnFederationPort = localIxnFederationPort;
	}

	public String getKeystorepath() {
		return keystorepath;
	}

	public void setKeystorepath(String keystorepath) {
		this.keystorepath = keystorepath;
	}

	public String getKeystorepassword() {
		return keystorepassword;
	}

	public void setKeystorepassword(String keystorepassword) {
		this.keystorepassword = keystorepassword;
	}

	public String getKeystoretype() {
		return keystoretype;
	}

	public void setKeystoretype(String keystoretype) {
		this.keystoretype = keystoretype;
	}

	public String getTruststorepath() {
		return truststorepath;
	}

	public void setTruststorepath(String truststorepath) {
		this.truststorepath = truststorepath;
	}

	public String getTruststorepassword() {
		return truststorepassword;
	}

	public void setTruststorepassword(String truststorepassword) {
		this.truststorepassword = truststorepassword;
	}

	public String getTruststoretype() {
		return truststoretype;
	}

	public void setTruststoretype(String truststoretype) {
		this.truststoretype = truststoretype;
	}

	public String getRemoteWritequeue() {
		return remoteWritequeue;
	}

	public void setRemoteWritequeue(String remoteWritequeue) {
		this.remoteWritequeue = remoteWritequeue;
	}

	@Override
	public String toString() {
		return "ForwarderProperties{" +
				"localIxnDomainName='" + localIxnDomainName + '\'' +
				", localIxnFederationPort='" + localIxnFederationPort + '\'' +
				", keystorepath='" + keystorepath + '\'' +
				", keystorepassword='" + keystorepassword + '\'' +
				", keystoretype='" + keystoretype + '\'' +
				", truststorepath='" + truststorepath + '\'' +
				", truststorepassword='" + truststorepassword + '\'' +
				", truststoretype='" + truststoretype + '\'' +
				", remoteWritequeue='" + remoteWritequeue + '\'' +
				'}';
	}
}
