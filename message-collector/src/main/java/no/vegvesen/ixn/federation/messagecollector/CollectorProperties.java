package no.vegvesen.ixn.federation.messagecollector;

/*-
 * #%L
 * message-collector
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix ="collector")
public class CollectorProperties {

	private String localIxnDomainName;
	private String localIxnFederationPort;
	private String keystorepath;
	private String keystorepassword;
	private String keystoretype;

	private String truststorepath;
	private String truststorepassword;
	private String truststoretype;
	private String writequeue = "fedEx";
	private String fixeddelay = "30000";

	public CollectorProperties() {
	}

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

	public String getWritequeue() {
		return writequeue;
	}

	public void setWritequeue(String writequeue) {
		this.writequeue = writequeue;
	}

	public String getFixeddelay() {
		return fixeddelay;
	}

	public void setFixeddelay(String fixeddelay) {
		this.fixeddelay = fixeddelay;
	}

	@Override
	public String toString() {
		return "CollectorProperties{" +
				"localIxnDomainName='" + localIxnDomainName + '\'' +
				", localIxnFederationPort='" + localIxnFederationPort + '\'' +
				", keystorepath='" + keystorepath + '\'' +
				", keystorepassword='" + keystorepassword + '\'' +
				", keystoretype='" + keystoretype + '\'' +
				", truststorepath='" + truststorepath + '\'' +
				", truststorepassword='" + truststorepassword + '\'' +
				", truststoretype='" + truststoretype + '\'' +
				", writequeue='" + writequeue + '\'' +
				", fixeddelay='" + fixeddelay + '\'' +
				'}';
	}
}
